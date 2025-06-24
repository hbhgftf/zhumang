package zysy.iflytek.zhumang.call.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import zysy.iflytek.zhumang.call.websocket.CallWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Value("${spring.websocket.endpoint}")
    private String endpoint;

    @Value("${spring.websocket.allowed-origins}")
    private String allowedOrigins;

    @Value("${spring.websocket.heartbeat-interval}")
    private long heartbeatInterval;

    @Value("${spring.websocket.connection-timeout}")
    private long connectionTimeout;

    @Value("${spring.websocket.max-text-message-size}")
    private int maxTextMessageSize;

    @Autowired
    private CallWebSocketHandler callWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(callWebSocketHandler, endpoint)
                .setAllowedOrigins(allowedOrigins.split(","));
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(maxTextMessageSize);
        container.setMaxBinaryMessageBufferSize(maxTextMessageSize);
        container.setMaxSessionIdleTimeout(connectionTimeout);
        return container;
    }
} 