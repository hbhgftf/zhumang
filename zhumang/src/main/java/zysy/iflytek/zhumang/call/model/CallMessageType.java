package zysy.iflytek.zhumang.call.model;

public enum CallMessageType {
    CALL_REQUEST,    // 通话请求
    CALL_ACCEPT,     // 接受通话
    CALL_REJECT,     // 拒绝通话
    CALL_END,        // 结束通话
    CALL_TIMEOUT,    // 通话超时
    CALL_CANCEL,     // 取消通话
    HEARTBEAT,       // 心跳消息
    ERROR           // 错误消息
} 