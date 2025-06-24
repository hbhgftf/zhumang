package zysy.iflytek.zhumang.redis.volunteer.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import zysy.iflytek.zhumang.redis.volunteer.service.VolunteerStatusService;
import zysy.iflytek.zhumang.common.Result;

@Api(tags = "志愿者状态管理")
@RestController
@RequestMapping("/api/volunteer")
public class VolunteerController {

    @Autowired
    private VolunteerStatusService volunteerStatusService;

    @ApiOperation("设置志愿者在线状态")
    @PostMapping("/status")
    public Result<Void> setVolunteerStatus(
            @RequestParam Long userId,
            @RequestParam(required = false, defaultValue = "true") String isOnline) {
        boolean online = "true".equalsIgnoreCase(isOnline) || "online".equalsIgnoreCase(isOnline);
        volunteerStatusService.setVolunteerStatus(userId, online);
        return Result.success();
    }

    @ApiOperation("获取志愿者在线状态")
    @GetMapping("/status/{userId}")
    public Result<Boolean> getVolunteerStatus(@PathVariable Long userId) {
        boolean isOnline = volunteerStatusService.isVolunteerOnline(userId);
        return Result.success(isOnline);
    }

    @ApiOperation("刷新志愿者状态")
    @PostMapping("/status/refresh/{userId}")
    public Result<Void> refreshVolunteerStatus(@PathVariable Long userId) {
        volunteerStatusService.refreshVolunteerStatus(userId);
        return Result.success();
    }

    @ApiOperation("获取在线志愿者数量")
    @GetMapping("/status/count")
    public Result<Long> getOnlineVolunteerCount() {
        long count = volunteerStatusService.getOnlineVolunteerCount();
        return Result.success(count);
    }
} 