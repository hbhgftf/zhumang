package zysy.iflytek.zhumang.call.model;

import lombok.Data;

@Data
public class CallMessage {
    private CallMessageType type;
    private String roomId;
    private Long callerId;
    private Long calleeId;
    private String content;
    private Object data;
} 