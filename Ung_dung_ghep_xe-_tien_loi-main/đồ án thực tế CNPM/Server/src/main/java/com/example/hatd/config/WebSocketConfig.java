package com.example.hatd.config; // (Hoặc package config của bạn)

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Kích hoạt máy chủ WebSocket
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Đây là các kênh (topics) mà client sẽ lắng nghe.
        // Server sẽ gửi tin nhắn đến các kênh bắt đầu bằng "/topic"
        registry.enableSimpleBroker("/topic");
        
        // Đây là tiền tố cho các API nhận tin nhắn (ví dụ: client gửi tin cho server)
        // (Không bắt buộc cho trường hợp của bạn, nhưng nên có)
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đây là Endpoint (đường dẫn) mà client sẽ kết nối tới WebSocket
        // "/ws" là tên phổ biến.
        registry.addEndpoint("/ws")
            .setAllowedOrigins("*");
    }
}