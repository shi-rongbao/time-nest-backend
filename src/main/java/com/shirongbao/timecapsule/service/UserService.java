package com.shirongbao.timecapsule.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shirongbao.timecapsule.common.Result;
import com.shirongbao.timecapsule.pojo.entity.Users;
import com.shirongbao.timecapsule.pojo.request.UserRequestObject;
import com.shirongbao.timecapsule.pojo.response.UserResponseObject;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-15
 * @description:
 */
public interface UserService  extends IService<Users> {

    // 注册
    Result<String> register(UserRequestObject request);

    // 登录
    String login(UserRequestObject request);

    // 用户上传头像
    Result<String> uploadAvatar(MultipartFile file) throws IOException;

    // 获取用户信息
    UserResponseObject getUserInfo();

    // 修改用户信息
    Result<Boolean> updateUserInfo(UserRequestObject request);

    // 登出
    void logout();

    // 用户申请注销
    void deactivateRequest();

    // 查询全部提交注销申请的用户
    List<Users> queryAllDeactivationRequested();

    // 逻辑删除注销的用户
    void doLogicDelete(List<Users> usersList);
}
