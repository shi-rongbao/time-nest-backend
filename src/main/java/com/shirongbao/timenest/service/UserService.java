package com.shirongbao.timenest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shirongbao.timenest.common.entity.Result;
import com.shirongbao.timenest.pojo.bo.NotificationBo;
import com.shirongbao.timenest.pojo.bo.UsersBo;
import com.shirongbao.timenest.pojo.entity.Notification;
import com.shirongbao.timenest.pojo.entity.Users;
import com.shirongbao.timenest.pojo.dto.UsersDto;
import com.shirongbao.timenest.pojo.vo.UsersVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

}
