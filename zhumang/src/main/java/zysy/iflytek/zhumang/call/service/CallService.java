package zysy.iflytek.zhumang.call.service;

import zysy.iflytek.zhumang.call.model.CallMessage;
import zysy.iflytek.zhumang.call.entity.CallRecord;
import java.util.List;

public interface CallService {
    // 处理通话请求
    void handleCallRequest(Long userId, CallMessage message);

    // 处理接受通话
    void handleCallAccept(Long userId, CallMessage message);

    // 处理拒绝通话
    void handleCallReject(Long userId, CallMessage message);

    // 处理结束通话
    void handleCallEnd(Long userId, CallMessage message);

    // 更新志愿者状态
    void updateVolunteerStatus(Long userId, boolean online);

    // 更新志愿者心跳
    void updateVolunteerHeartbeat(Long userId);

    // 获取在线志愿者列表
    List<Long> getOnlineVolunteers();

    // 获取通话记录
    CallRecord getCallRecord(String roomId);

    // 创建通话记录
    CallRecord createCallRecord(Long callerId, Long calleeId);

    // 更新通话状态
    void updateCallStatus(String roomId, Integer status);

    // 结束通话并记录时长
    void endCall(String roomId);
} 