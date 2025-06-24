package zysy.iflytek.zhumang.call.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import zysy.iflytek.zhumang.call.service.CallService;
import zysy.iflytek.zhumang.call.model.CallMessage;
import zysy.iflytek.zhumang.call.model.CallMessageType;
import zysy.iflytek.zhumang.utils.JwtUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class CallWebSocketHandler implements WebSocketHandler {

    @Autowired
    private CallService callService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // 存储用户ID和WebSocket会话的映射关系
    private static final Map<Long, WebSocketSession> USER_SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            USER_SESSIONS.put(userId, session);
            callService.updateVolunteerStatus(userId, true);
            log.info("User {} connected successfully. Current online users: {}. Session ID: {}", 
                userId, USER_SESSIONS.size(), session.getId());
        } else {
            log.error("Failed to establish WebSocket connection: Invalid user token");
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();
            log.info("Received WebSocket message: {}", payload);
            
            CallMessage callMessage = objectMapper.readValue(payload, CallMessage.class);
            Long userId = getUserIdFromSession(session);

            if (userId == null) {
                log.error("Received message from unauthorized session: {}", session.getId());
                sendErrorMessage(session, "Unauthorized");
                return;
            }

            log.info("Processing message from user {}: type={}", userId, callMessage.getType());
            handleCallMessage(userId, callMessage);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error: ", exception);
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            USER_SESSIONS.remove(userId);
            callService.updateVolunteerStatus(userId, false);
            log.info("User {} disconnected due to transport error", userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            USER_SESSIONS.remove(userId);
            callService.updateVolunteerStatus(userId, false);
            log.info("User {} disconnected, status: {}", userId, status);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void handleCallMessage(Long userId, CallMessage message) {
        try {
            switch (message.getType()) {
                case CALL_REQUEST:
                    callService.handleCallRequest(userId, message);
                    break;
                case CALL_ACCEPT:
                    callService.handleCallAccept(userId, message);
                    break;
                case CALL_REJECT:
                    callService.handleCallReject(userId, message);
                    break;
                case CALL_END:
                    callService.handleCallEnd(userId, message);
                    break;
                case HEARTBEAT:
                    handleHeartbeat(userId);
                    break;
                default:
                    log.warn("Unknown message type: {}", message.getType());
            }
        } catch (Exception e) {
            log.error("Error handling call message: ", e);
            WebSocketSession session = USER_SESSIONS.get(userId);
            if (session != null) {
                sendErrorMessage(session, "Error processing message: " + e.getMessage());
            }
        }
    }

    private void handleHeartbeat(Long userId) {
        callService.updateVolunteerHeartbeat(userId);
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        String token = session.getUri().getQuery();
        if (token != null && token.startsWith("token=")) {
            token = token.substring(6);
            try {
                return JwtUtils.getUserIdFromToken(token);
            } catch (Exception e) {
                log.error("Error parsing token: ", e);
            }
        }
        return null;
    }

    public void sendMessage(Long userId, CallMessage message) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        log.info("Attempting to send message to user {}: type={}, roomId={}, callerId={}, calleeId={}", 
            userId, message.getType(), message.getRoomId(), message.getCallerId(), message.getCalleeId());
        
        if (session == null) {
            log.error("User {} has no active WebSocket session", userId);
            return;
        }
        
        if (!session.isOpen()) {
            log.error("User {}'s WebSocket session is closed", userId);
            return;
        }
        
        try {
            String payload = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(payload));
            log.info("Successfully sent message to user {}: {}", userId, payload);
        } catch (IOException e) {
            log.error("Error sending message to user {}: ", userId, e);
        }
    }

    private void sendErrorMessage(WebSocketSession session, String error) {
        try {
            CallMessage errorMessage = new CallMessage();
            errorMessage.setType(CallMessageType.ERROR);
            errorMessage.setContent(error);
            String payload = objectMapper.writeValueAsString(errorMessage);
            session.sendMessage(new TextMessage(payload));
        } catch (IOException e) {
            log.error("Error sending error message: ", e);
        }
    }
} 