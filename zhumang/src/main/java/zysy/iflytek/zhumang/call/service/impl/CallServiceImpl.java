package zysy.iflytek.zhumang.call.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zysy.iflytek.zhumang.call.entity.CallRecord;
import zysy.iflytek.zhumang.call.mapper.CallRecordMapper;
import zysy.iflytek.zhumang.call.model.CallMessage;
import zysy.iflytek.zhumang.call.model.CallMessageType;
import zysy.iflytek.zhumang.call.service.CallService;
import zysy.iflytek.zhumang.call.websocket.CallWebSocketHandler;
import zysy.iflytek.zhumang.common.exception.BusinessException;
import zysy.iflytek.zhumang.user.entity.User;
import zysy.iflytek.zhumang.user.service.IUserService;
import zysy.iflytek.zhumang.redis.volunteer.service.VolunteerStatusService;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CallServiceImpl extends ServiceImpl<CallRecordMapper, CallRecord> implements CallService {

    @Autowired
    private CallWebSocketHandler webSocketHandler;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private IUserService userService;

    @Autowired
    private VolunteerStatusService volunteerStatusService;

    private static final long CALL_TIMEOUT = 300; // 通话超时时间（秒）

    @Override
    @Transactional
    public void handleCallRequest(Long userId, CallMessage message) {
        log.info("Handling call request from user {}", userId);
        
        // 验证用户是否为视障用户
        User user = userService.getById(userId);
        if (user == null || !"视障用户".equals(user.getRole())) {
            log.error("Invalid user role for call request: userId={}, role={}", userId, user != null ? user.getRole() : "null");
            throw new BusinessException("只有视障用户可以发起通话请求");
        }

        // 获取在线志愿者列表
        List<Long> onlineVolunteers = getOnlineVolunteers();
        log.info("Online volunteers count: {}", onlineVolunteers.size());
        
        if (onlineVolunteers.isEmpty()) {
            log.info("No online volunteers available for user {}", userId);
            CallMessage response = new CallMessage();
            response.setType(CallMessageType.ERROR);
            response.setContent("当前没有在线志愿者");
            webSocketHandler.sendMessage(userId, response);
            return;
        }

        // 随机选择一个在线志愿者
        Long selectedVolunteerId = onlineVolunteers.get(new Random().nextInt(onlineVolunteers.size()));
        log.info("Selected volunteer {} for user {}", selectedVolunteerId, userId);

        // 创建通话记录
        CallRecord callRecord = createCallRecord(userId, selectedVolunteerId);
        log.info("Created call record: roomId={}, callerId={}, calleeId={}", 
            callRecord.getRoomId(), callRecord.getCallerId(), callRecord.getCalleeId());

        // 发送通话请求给选中的志愿者
        CallMessage volunteerMessage = new CallMessage();
        volunteerMessage.setType(CallMessageType.CALL_REQUEST);
        volunteerMessage.setRoomId(callRecord.getRoomId());
        volunteerMessage.setCallerId(userId);
        volunteerMessage.setCalleeId(selectedVolunteerId);
        log.info("Sending call request to volunteer {}: roomId={}", selectedVolunteerId, callRecord.getRoomId());
        webSocketHandler.sendMessage(selectedVolunteerId, volunteerMessage);

        // 设置通话超时检查
        scheduleCallTimeout(callRecord.getRoomId());
        log.info("Call timeout scheduled for room {}", callRecord.getRoomId());
    }

    @Override
    @Transactional
    public void handleCallAccept(Long userId, CallMessage message) {
        // 验证用户是否为志愿者
        User user = userService.getById(userId);
        if (user == null || !"志愿者".equals(user.getRole())) {
            throw new BusinessException("只有志愿者可以接受通话请求");
        }

        CallRecord callRecord = getCallRecord(message.getRoomId());
        if (callRecord == null) {
            throw new BusinessException("通话记录不存在");
        }

        if (!userId.equals(callRecord.getCalleeId())) {
            throw new BusinessException("无权接受此通话请求");
        }

        // 更新通话状态为进行中
        updateCallStatus(message.getRoomId(), 1);
        callRecord.setStartTime(new Date());

        // 通知主叫用户通话已接受
        CallMessage callerMessage = new CallMessage();
        callerMessage.setType(CallMessageType.CALL_ACCEPT);
        callerMessage.setRoomId(message.getRoomId());
        callerMessage.setCallerId(callRecord.getCallerId());
        callerMessage.setCalleeId(userId);
        webSocketHandler.sendMessage(callRecord.getCallerId(), callerMessage);
    }

    @Override
    @Transactional
    public void handleCallReject(Long userId, CallMessage message) {
        CallRecord callRecord = getCallRecord(message.getRoomId());
        if (callRecord == null) {
            throw new BusinessException("通话记录不存在");
        }

        if (!userId.equals(callRecord.getCalleeId())) {
            throw new BusinessException("无权拒绝此通话请求");
        }

        // 更新通话状态为已拒绝
        updateCallStatus(message.getRoomId(), 3);

        // 通知主叫用户通话被拒绝
        CallMessage callerMessage = new CallMessage();
        callerMessage.setType(CallMessageType.CALL_REJECT);
        callerMessage.setRoomId(message.getRoomId());
        callerMessage.setCallerId(callRecord.getCallerId());
        callerMessage.setCalleeId(userId);
        webSocketHandler.sendMessage(callRecord.getCallerId(), callerMessage);

        // 尝试重新分配志愿者
        handleCallRequest(callRecord.getCallerId(), message);
    }

    @Override
    @Transactional
    public void handleCallEnd(Long userId, CallMessage message) {
        CallRecord callRecord = getCallRecord(message.getRoomId());
        if (callRecord == null) {
            throw new BusinessException("通话记录不存在");
        }

        if (!userId.equals(callRecord.getCallerId()) && !userId.equals(callRecord.getCalleeId())) {
            throw new BusinessException("无权结束此通话");
        }

        endCall(message.getRoomId());

        // 通知对方通话已结束
        Long otherUserId = userId.equals(callRecord.getCallerId()) ? callRecord.getCalleeId() : callRecord.getCallerId();
        CallMessage endMessage = new CallMessage();
        endMessage.setType(CallMessageType.CALL_END);
        endMessage.setRoomId(message.getRoomId());
        endMessage.setCallerId(callRecord.getCallerId());
        endMessage.setCalleeId(callRecord.getCalleeId());
        webSocketHandler.sendMessage(otherUserId, endMessage);
    }

    @Override
    public void updateVolunteerStatus(Long userId, boolean online) {
        User user = userService.getById(userId);
        if (user == null || !"志愿者".equals(user.getRole())) {
            return;
        }
        volunteerStatusService.setVolunteerStatus(userId, online);
    }

    @Override
    public void updateVolunteerHeartbeat(Long userId) {
        volunteerStatusService.refreshVolunteerStatus(userId);
    }

    @Override
    public List<Long> getOnlineVolunteers() {
        return volunteerStatusService.getOnlineVolunteers();
    }

    @Override
    public CallRecord getCallRecord(String roomId) {
        return lambdaQuery()
            .eq(CallRecord::getRoomId, roomId)
            .one();
    }

    @Override
    @Transactional
    public CallRecord createCallRecord(Long callerId, Long calleeId) {
        CallRecord callRecord = new CallRecord();
        callRecord.setRoomId(generateRoomId());
        callRecord.setCallerId(callerId);
        callRecord.setCalleeId(calleeId);
        callRecord.setStatus(0); // 等待中
        callRecord.setCreatedTime(new Date());
        save(callRecord);
        return callRecord;
    }

    @Override
    @Transactional
    public void updateCallStatus(String roomId, Integer status) {
        lambdaUpdate()
            .eq(CallRecord::getRoomId, roomId)
            .set(CallRecord::getStatus, status)
            .update();
    }

    @Override
    @Transactional
    public void endCall(String roomId) {
        CallRecord callRecord = getCallRecord(roomId);
        if (callRecord != null) {
            Date endTime = new Date();
            long duration = (endTime.getTime() - callRecord.getStartTime().getTime()) / 1000;

            lambdaUpdate()
                .eq(CallRecord::getRoomId, roomId)
                .set(CallRecord::getStatus, 2) // 已结束
                .set(CallRecord::getEndTime, endTime)
                .set(CallRecord::getDuration, (int) duration)
                .update();
        }
    }

    private String generateRoomId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void scheduleCallTimeout(String roomId) {
        redisTemplate.opsForValue().set(
            "call:timeout:" + roomId,
            roomId,
            CALL_TIMEOUT,
            TimeUnit.SECONDS
        );
    }
} 