package com.businessintelligence.config;

import com.businessintelligence.repository.NewsLiveRepository;
import com.businessintelligence.repository.NewsRepository;
import com.businessintelligence.websocket.DataWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

//    private final NewsRepository newsRepository;
    private final NewsLiveRepository newsLiveRepository;

//    public WebSocketConfig(NewsRepository newsRepository) {
//        this.newsRepository = newsRepository;
//    }
    public WebSocketConfig(NewsLiveRepository newsLiveRepository) {
        this.newsLiveRepository = newsLiveRepository;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new DataWebSocketHandler(newsLiveRepository), "/ws/data").setAllowedOrigins("*");
    }
}



