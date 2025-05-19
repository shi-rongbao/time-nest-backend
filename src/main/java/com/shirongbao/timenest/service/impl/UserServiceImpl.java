package com.shirongbao.timenest.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.timenest.common.Result;
import com.shirongbao.timenest.common.constant.RedisConstant;
import com.shirongbao.timenest.common.enums.DeactivationRequestedEnum;
import com.shirongbao.timenest.common.enums.IsDeletedEnum;
import com.shirongbao.timenest.converter.UserConverter;
import com.shirongbao.timenest.dao.UserMapper;
import com.shirongbao.timenest.pojo.entity.Users;
import com.shirongbao.timenest.pojo.dto.UsersDto;
import com.shirongbao.timenest.pojo.vo.UsersVo;
import com.shirongbao.timenest.service.UserService;
import com.shirongbao.timenest.service.oss.OssService;
import com.shirongbao.timenest.utils.RedisUtil;
import com.shirongbao.timenest.utils.SecurityUtil;
import com.shirongbao.timenest.utils.VerificationCodeUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-15
 * @description: 用户服务实现类
 */
@Service("userService")
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, Users> implements UserService {

    // 递归最多重试 3 次
    private static final int MAX_RETRY_TIMES = 3;

    private final RedisUtil redisUtil;

    private final OssService ossService;

    @Override
    public Result<String> register(UsersDto request) {
        // 先校验验证码是否正确
        String email = request.getEmail();
        String requestVerifyCode = request.getVerifyCode();
        String key = redisUtil.buildKey(RedisConstant.EMAIL_PREFIX, email);
        if (StringUtils.isBlank(key)) {
            return Result.fail("还未获取验证码或验证码已过期，请重新获取验证码！");
        }

        String verifyCode = redisUtil.get(key).toString();
        if (StringUtils.isBlank(verifyCode)) {
            return Result.fail("验证码已过期，请重新获取验证码！");
        }

        // 验证码不同返回失败
        if (!verifyCode.equals(VerificationCodeUtil.convertToUpperCase(requestVerifyCode))) {
            return Result.fail("验证码错误！");
        }

        // 要校验当前用户名是否已经存在
        String userAccount = request.getUserAccount();
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getUserAccount, userAccount);
        Users users = getOne(wrapper);
        if (ObjectUtils.isNotEmpty(users)) {
            return Result.fail("当前用户名已经存在！");
        }

        // 要校验当前邮箱是否已经为其他用户注册过
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getEmail, email);
        users = getOne(wrapper);
        if (ObjectUtils.isNotEmpty(users)) {
            return Result.fail("当前邮箱已为其他用户注册过！");
        }

        // 加密密码
        String password = request.getPassword();
        String encryptPassword = SecurityUtil.encryptPassword(password);

        // 按规则生成用户默认昵称(tc_nickname_${date})
        String nickName = "tc_nickname_" + System.currentTimeMillis();
        users = new Users();
        users.setNickName(nickName);
        users.setEmail(email);
        users.setPassword(encryptPassword);
        users.setUserAccount(userAccount);
        save(users);
        return Result.success("注册成功！");
    }

    @Override
    public String login(UsersDto request) {
        // 查询这个用户是否存在，不存在直接返回
        String userAccount = request.getUserAccount();
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getUserAccount, userAccount);
        wrapper.eq(Users::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        Users users = getOne(wrapper);
        if (ObjectUtils.isEmpty(users)) {
            return null;
        }

        // 校验密码是否正确
        String password = request.getPassword();
        // 从数据库中获取存储的包含盐值的哈希值
        String storedPasswordWithSalt = users.getPassword();
        String[] parts = storedPasswordWithSalt.split("\\$");
        if (parts.length != 2) {
            // 处理数据库中密码格式错误的情况，例如没有盐值或者格式不正确
            return null; // 或者抛出异常
        }
        String storedSalt = parts[0];
        String storedHash = parts[1];

        if (SecurityUtil.verifyPassword(password, storedHash, storedSalt)) {
            // 校验成功后登录并返回token
            StpUtil.login(users.getId());
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            String tokenValue = tokenInfo.getTokenValue();
            // 把用户基础信息放到缓存里
            setUserCache(tokenValue);
            // 判断是否取消注销
            cancelDeactivateRequest(users);
            return tokenValue;
        }

        return null;
    }

    @Override
    public Result<String> uploadAvatar(MultipartFile file) throws IOException {
        String url = ossService.uploadAvatar(file);
        if (StringUtils.isBlank(url)) {
            return Result.fail("头像上传失败,请稍后重试!");
        }

        // 更新用户数据库头像
        long userId = StpUtil.getLoginIdAsLong();
        Users users = getById(userId);
        users.setAvatarUrl(url);
        updateById(users);

        // 删掉用户缓存
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        String tokenValue = tokenInfo.getTokenValue();
        removeUserCache(tokenValue);
        return Result.success(url);
    }

    @Override
    public UsersVo getUserInfo() {
        return getUserInfoWithRetry(0);
    }

    @Override
    public Result<Boolean> updateUserInfo(UsersDto request) {
        long userId = StpUtil.getLoginIdAsLong();
        Users users = UserConverter.INSTANCE.requestObjectToEntity(request);
        users.setId(userId);

        // 如果手机号不为空，加密后存储
        String phone = request.getPhone();
        if (StringUtils.isNotBlank(phone)) {
            String encryptedPhone = SecurityUtil.encryptPhoneNumber(phone);
            users.setPhone(encryptedPhone);
        }

        // 如果密码不为空，加密后存储
        String password = request.getPassword();
        if (StringUtils.isNotBlank(password)) {
            String encryptedPassword = SecurityUtil.encryptPassword(password);
            users.setPassword(encryptedPassword);
        }

        // 更新用户信息
        updateById(users);

        // 删掉用户缓存
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        String tokenValue = tokenInfo.getTokenValue();
        removeUserCache(tokenValue);
        return Result.success();
    }

    @Override
    public void logout() {
        StpUtil.logout();
        // 把当前token的缓存移除
        removeUserCache(StpUtil.getTokenInfo().getTokenValue());
    }

    @Override
    public void deactivateRequest() {
        long userId = StpUtil.getLoginIdAsLong();
        Users users = getById(userId);
        // 设置15天的注销冷静期
        users.setDeactivationRequested(DeactivationRequestedEnum.YES.getCode());
        users.setDeactivationRequestedTime(DateUtils.addDays(new Date(), 15));
        updateById(users);

        // 做登出
        logout();
    }

    @Override
    public List<Users> queryAllDeactivationRequested() {
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getDeactivationRequested, DeactivationRequestedEnum.YES.getCode());
        wrapper.eq(Users::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        return list(wrapper);
    }

    // 批量更新是带事物的，所以这里用@Transactional（会失效，所以手动加上）
    @Transactional
    @Override
    public void doLogicDelete(List<Users> usersList) {
        for (Users users : usersList) {
            users.setIsDeleted(IsDeletedEnum.DELETED.getCode());
        }
        updateBatchById(usersList);
    }

    // 获取用户信息，最多执行三次递归
    private UsersVo getUserInfoWithRetry(int retryTimes) {
        if (retryTimes > MAX_RETRY_TIMES) {
            throw new RuntimeException("获取用户信息失败，请稍后再试或联系管理员！");
        }

        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        String tokenValue = tokenInfo.getTokenValue();
        Users users = getUserCache(tokenValue);

        // 缓存里没有，尝试重新加载缓存并重试
        if (ObjectUtils.isEmpty(users)) {
            try {
                setUserCache(tokenValue);
            } catch (Exception e) {
                // 可以记录日志，如 log.warn("设置用户缓存失败", e);
                return getUserInfoWithRetry(retryTimes + 1);
            }

            return getUserInfoWithRetry(retryTimes + 1);
        }

        if (ObjectUtils.isEmpty(users)) {
            throw new RuntimeException("当前登录状态异常，请重新登录！");
        }

        // 解密手机号返回
        String encryptPhone = users.getPhone();
        if (StringUtils.isNotBlank(encryptPhone)) {
            users.setPhone(SecurityUtil.decryptPhoneNumber(encryptPhone));
        }

        // 转换之后返回
        return UserConverter.INSTANCE.entityToResponseObject(users);
    }

    // 将用户基础信息放到缓存里
    private void setUserCache(String tokenValue) {
        String key = redisUtil.buildKey(RedisConstant.USER_CACHE_PREFIX, tokenValue);
        // 查到这个用户信息
        Users users = getById(StpUtil.getLoginIdAsLong());
        // 序列化后缓存
        String usersString = JSON.toJSONString(users);
        // 缓存30天
        redisUtil.set(key, usersString, 30 * 24 * 60 * 60);
    }

    // 将用户基础信息缓存移除
    private void removeUserCache(String tokenValue) {
        String key = redisUtil.buildKey(RedisConstant.USER_CACHE_PREFIX, tokenValue);
        redisUtil.del(key);
    }

    // 从缓存中获取用户对象
    private Users getUserCache(String tokenValue) {
        String key = redisUtil.buildKey(RedisConstant.USER_CACHE_PREFIX, tokenValue);
        Object usersObject = redisUtil.get(key);
        if (ObjectUtils.isEmpty(usersObject)) {
            return null;
        }
        String usersString = usersObject.toString();
        if (StringUtils.isNotBlank(usersString)) {
            return JSON.parseObject(usersString, Users.class);
        }
        return null;
    }


    // 取消注销申请
    private void cancelDeactivateRequest(Users users) {
        Integer deactivationRequested = users.getDeactivationRequested();
        if (deactivationRequested == null) {
            return;
        }
        if (DeactivationRequestedEnum.YES.getCode() == deactivationRequested) {
            users.setDeactivationRequested(DeactivationRequestedEnum.NO.getCode());
            updateById(users);
        }
    }

}
