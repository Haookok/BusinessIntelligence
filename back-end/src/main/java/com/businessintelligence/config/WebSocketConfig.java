package com.businessintelligence.config;

import com.businessintelligence.websocket.DataWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new DataWebSocketHandler(), "/ws/data").setAllowedOrigins("*");
    }
}
