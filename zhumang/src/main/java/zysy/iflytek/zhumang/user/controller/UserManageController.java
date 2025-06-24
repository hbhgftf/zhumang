package zysy.iflytek.zhumang.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import zysy.iflytek.zhumang.common.model.Result;
import zysy.iflytek.zhumang.user.dto.UserManageDto;
import zysy.iflytek.zhumang.user.entity.User;
import zysy.iflytek.zhumang.user.service.IUserService;
import zysy.iflytek.zhumang.common.exception.BusinessException;
import zysy.iflytek.zhumang.user.entity.Feedback;
import zysy.iflytek.zhumang.user.service.FeedbackService;

import javax.annotation.Resource;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/user/manage")
public class UserManageController {

    private static final Logger logger = LoggerFactory.getLogger(UserManageController.class);

    @Resource
    private IUserService userService;

    @Resource
    private FeedbackService feedbackService;

    @ApiOperation("创建用户")
    @PostMapping
    public Result<User> createUser(@RequestBody @Valid UserManageDto dto,
                                 @RequestAttribute Long userId) {
        // 检查当前用户是否为管理员
        User currentUser = userService.getById(userId);
        if (!"管理员".equals(currentUser.getRole())) {
            throw new BusinessException("无权限执行此操作");
        }
        return Result.success(userService.createUser(dto));
    }

    @ApiOperation("更新用户")
    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Long id,
                                 @RequestBody @Valid UserManageDto dto,
                                 @RequestAttribute Long userId) {
        // 检查当前用户是否为管理员
        User currentUser = userService.getById(userId);
        if (!"管理员".equals(currentUser.getRole())) {
            throw new BusinessException("无权限执行此操作");
        }
        dto.setId(id);
        return Result.success(userService.updateUser(dto));
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id,
                                 @RequestAttribute Long userId) {
        // 检查当前用户是否为管理员
        User currentUser = userService.getById(userId);
        if (!"管理员".equals(currentUser.getRole())) {
            throw new BusinessException("无权限执行此操作");
        }
        userService.deleteUser(id);
        return Result.success();
    }

    @ApiOperation("获取用户详情")
    @GetMapping("/{id}")
    public Result<User> getUserDetail(@PathVariable Long id,
                                    @RequestAttribute Long userId) {
        // 检查当前用户是否为管理员
        User currentUser = userService.getById(userId);
        if (!"管理员".equals(currentUser.getRole())) {
            throw new BusinessException("无权限执行此操作");
        }
        return Result.success(userService.getById(id));
    }

    @ApiOperation("分页查询用户")
    @GetMapping("/page")
    public Result<IPage<User>> pageUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status,
            @RequestAttribute Long userId) {
        // 检查当前用户是否为管理员
        User currentUser = userService.getById(userId);
        if (!"管理员".equals(currentUser.getRole())) {
            throw new BusinessException("无权限执行此操作");
        }
        return Result.success(userService.pageUsers(pageNum, pageSize, email, role, status));
    }

    @ApiOperation("我要反馈")
    @PostMapping("/feedback")
    public Result<Void> submitFeedback(@RequestBody Feedback feedback, @RequestAttribute Long userId) {
        feedback.setUserId(userId);
        feedbackService.submitFeedback(feedback);
        return Result.success();
    }

    @ApiOperation("分页查询用户反馈")
    @GetMapping("/feedback/page")
    public Result<IPage<Feedback>> pageFeedback(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return Result.success(feedbackService.pageFeedback(pageNum, pageSize, userId, startTime, endTime));
    }

    @ApiOperation("查询反馈详情")
    @GetMapping("/feedback/{id}")
    public Result<Feedback> getFeedbackDetail(@PathVariable Long id) {
        return Result.success(feedbackService.getFeedbackById(id));
    }
} 