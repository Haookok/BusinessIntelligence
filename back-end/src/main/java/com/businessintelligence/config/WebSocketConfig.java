package com.businessintelligence.config;

import com.businessintelligence.repository.NewsLiveRepository;
import com.businessintelligence.repository.NewsRepository;
import com.businessintelligence.websocket.CategoryMinuteStatWebSocketHandler;
import com.businessintelligence.websocket.DataWebSocketHandler;
import com.businessintelligence.websocket.UserCategoryStatWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final NewsLiveRepository newsLiveRepository;
    public WebSocketConfig(NewsLiveRepository newsLiveRepository) {
        this.newsLiveRepository = newsLiveRepository;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new DataWebSocketHandler(newsLiveRepository), "/ws/data").setAllowedOrigins("*");
        registry.addHandler(new UserCategoryStatWebSocketHandler(newsLiveRepository), "/ws/category").setAllowedOrigins("*");
        registry.addHandler(new CategoryMinuteStatWebSocketHandler(newsLiveRepository), "/ws/minute-category-stats").setAllowedOrigins("*");

    }
}



