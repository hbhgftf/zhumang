package zysy.iflytek.zhumang.call.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import zysy.iflytek.zhumang.call.model.CallMessage;
import zysy.iflytek.zhumang.call.model.CallMessageType;
import zysy.iflytek.zhumang.call.service.CallService;
import zysy.iflytek.zhumang.call.entity.CallRecord;
import zysy.iflytek.zhumang.common.model.Result;
import zysy.iflytek.zhumang.utils.JwtUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/call")
@Api(tags = "通话管理接口")
public class CallController {

    @Autowired
    private CallService callService;

    @PostMapping("/request")
    @ApiOperation("发起通话请求")
    public Result<Map<String, Object>> requestCall(HttpServletRequest request) {
        try {
            Long userId = JwtUtils.getUserIdFromToken(request.getHeader("Authorization").substring(7));
            CallMessage message = new CallMessage();
            message.setType(CallMessageType.CALL_REQUEST);
            callService.handleCallRequest(userId, message);

            Map<String, Object> data = new HashMap<>();
            data.put("message", "通话请求已发送");
            return Result.success("通话请求已发送", data);
        } catch (Exception e) {
            log.error("发起通话请求失败: ", e);
            return Result.error("发起通话请求失败: " + e.getMessage());
        }
    }

    @PostMapping("/accept")
    @ApiOperation("接受通话请求")
    public Result<Map<String, Object>> acceptCall(@RequestBody CallMessage message, HttpServletRequest request) {
        log.info("Received /api/call/accept request. URI: {}, roomId: {}", request.getRequestURI(), message.getRoomId());
        try {
            Long userId = JwtUtils.getUserIdFromToken(request.getHeader("Authorization").substring(7));
            message.setType(CallMessageType.CALL_ACCEPT);
            callService.handleCallAccept(userId, message);

            Map<String, Object> data = new HashMap<>();
            data.put("message", "已接受通话请求");
            return Result.success("已接受通话请求", data);
        } catch (Exception e) {
            log.error("接受通话请求失败: ", e);
            return Result.error("接受通话请求失败: " + e.getMessage());
        }
    }

    @PostMapping("/reject")
    @ApiOperation("拒绝通话请求")
    public Result<Map<String, Object>> rejectCall(@RequestBody CallMessage message, HttpServletRequest request) {
        log.info("Received /api/call/reject request. URI: {}, roomId: {}", request.getRequestURI(), message.getRoomId());
        try {
            Long userId = JwtUtils.getUserIdFromToken(request.getHeader("Authorization").substring(7));
            message.setType(CallMessageType.CALL_REJECT);
            callService.handleCallReject(userId, message);

            Map<String, Object> data = new HashMap<>();
            data.put("message", "已拒绝通话请求");
            return Result.success("已拒绝通话请求", data);
        } catch (Exception e) {
            log.error("拒绝通话请求失败: ", e);
            return Result.error("拒绝通话请求失败: " + e.getMessage());
        }
    }

    @PostMapping("/end")
    @ApiOperation("结束通话")
    public Result<Map<String, Object>> endCall(@RequestParam String roomId, HttpServletRequest request) {
        try {
            Long userId = JwtUtils.getUserIdFromToken(request.getHeader("Authorization").substring(7));
            CallMessage message = new CallMessage();
            message.setType(CallMessageType.CALL_END);
            message.setRoomId(roomId);
            callService.handleCallEnd(userId, message);

            Map<String, Object> data = new HashMap<>();
            data.put("message", "通话已结束");
            return Result.success("通话已结束", data);
        } catch (Exception e) {
            log.error("结束通话失败: ", e);
            return Result.error("结束通话失败: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    @ApiOperation("获取通话状态")
    public Result<Map<String, Object>> getCallStatus(@RequestParam String roomId) {
        try {
            CallRecord callRecord = callService.getCallRecord(roomId);
            if (callRecord == null) {
                return Result.error("通话记录不存在");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("roomId", callRecord.getRoomId());
            data.put("status", callRecord.getStatus());
            data.put("startTime", callRecord.getStartTime());
            data.put("endTime", callRecord.getEndTime());
            data.put("duration", callRecord.getDuration());
            return Result.success("获取通话状态成功", data);
        } catch (Exception e) {
            log.error("获取通话状态失败: ", e);
            return Result.error("获取通话状态失败: " + e.getMessage());
        }
    }

    @GetMapping("/volunteers")
    @ApiOperation("获取在线志愿者列表")
    public Result<Map<String, Object>> getOnlineVolunteers() {
        try {
            List<Long> volunteers = callService.getOnlineVolunteers();
            Map<String, Object> data = new HashMap<>();
            data.put("volunteers", volunteers);
            data.put("count", volunteers.size());
            return Result.success("获取在线志愿者列表成功", data);
        } catch (Exception e) {
            log.error("获取在线志愿者列表失败: ", e);
            return Result.error("获取在线志愿者列表失败: " + e.getMessage());
        }
    }
} 