package com.shirongbao.timenest.service.auth;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shirongbao.timenest.common.entity.Result;
import com.shirongbao.timenest.pojo.bo.UsersBo;
import com.shirongbao.timenest.pojo.entity.Users;
import com.shirongbao.timenest.pojo.dto.UsersDto;
import com.shirongbao.timenest.pojo.vo.UsersVo;
import com.shirongbao.timenest.pojo.vo.VerifyCodeVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-15
 * @description: 用户服务接口
 */
public interface UserService  extends IService<Users> {

    // 注册
    Result<String> register(UsersDto request);

    // 登录
    String login(UsersDto request);

    // 用户上传头像
    Result<String> uploadAvatar(MultipartFile file) throws IOException;

    // 获取用户信息
    UsersVo getUserInfo();

    // 修改用户信息
    Result<Boolean> updateUserInfo(UsersDto request);

    // 登出
    void logout();

    // 用户申请注销
    void deactivateRequest();

    // 查询全部提交注销申请的用户
    List<Users> queryAllDeactivationRequested();

    // 逻辑删除注销的用户
    void doLogicDelete(List<Users> usersList);

    // 获取用户Bo列表
    List<UsersBo> getUsersBoList(List<Long> userIdList);

    // 获取验证码(微信登录，简单4位数字验证码，并缓存)
    Result<VerifyCodeVo> getVerifyCode();

    // 微信登录
    String wxLogin(String openId);

    // 根据stringUserId从缓存获取用户
    Users getUsersByCache(String userIdAsString);
}
