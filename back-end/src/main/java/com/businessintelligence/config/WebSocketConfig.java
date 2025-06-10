package com.businessintelligence.config;

import com.businessintelligence.repository.NewsRepository;
import com.businessintelligence.websocket.DataWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final NewsRepository newsRepository;

    public WebSocketConfig(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new DataWebSocketHandler(newsRepository), "/ws/data").setAllowedOrigins("*");
    }
}



