package com.shirongbao.timecapsule.task;

import com.shirongbao.timecapsule.pojo.entity.Users;
import com.shirongbao.timecapsule.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-17
 * @description:
 */
@Component
@RequiredArgsConstructor
public class DeactivateSchedule {

    private final UserService userService;

    // 定时任务扫描，每1分钟扫描一次，逻辑删除所有已提交注销请求，且注销时间超过当前时间的用户
    @Scheduled(cron = "0 0/1 * * * ?")
    public void deactivationSchedule() {
        List<Users> usersList = userService.queryAllDeactivationRequested();
        // 留下AuthUserBo.deactivationRequestedTime超过当前时间的Bo
        usersList.removeIf(users -> users.getDeactivationRequestedTime().after(new java.util.Date()));
        if (usersList.isEmpty()) {
            return;
        }
        userService.doLogicDelete(usersList);
    }

}
