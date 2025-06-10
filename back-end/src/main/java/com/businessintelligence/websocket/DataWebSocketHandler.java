package com.businessintelligence.websocket;

import com.businessintelligence.DTO.SecondlyBrowseDTO;
import com.businessintelligence.repository.NewsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;  // 加这个

public class DataWebSocketHandler extends TextWebSocketHandler {

    // 每个实例一个会话集合
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final NewsRepository newsRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 是否启动过定时任务的标记，防止重复启动
    private final AtomicBoolean started = new AtomicBoolean(false);

    public DataWebSocketHandler(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("WebSocket connected, session id: " + session.getId());

        // 连接建立后，首次启动定时任务
        if (started.compareAndSet(false, true)) {
            scheduler.scheduleAtFixedRate(this::sendRealtimeData, 0, 5000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        System.out.println("WebSocket closed, session id: " + session.getId());

        // 如果没有会话了，关闭定时任务，防止资源浪费
        if (sessions.isEmpty() && started.compareAndSet(true, false)) {
            scheduler.shutdown();
            System.out.println("No sessions, scheduler shutdown");
        }
    }

    private void sendRealtimeData() {
        if (sessions.isEmpty()) {
            System.out.println("No active sessions, skipping sendRealtimeData");
            return;
        }
        System.out.println("sendRealtimeData triggered at " + System.currentTimeMillis());
        String jsonData = getLatestDataFromDB();

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(jsonData));
                } catch (IOException e) {
                    System.err.println("Failed to send message to session " + session.getId());
                    e.printStackTrace();
                }
            }
        }
    }

    private String getLatestDataFromDB() {
        List<Object[]> rawList = newsRepository.getSecondlyBrowseByNewsId(572);
        List<SecondlyBrowseDTO> dtoList = new ArrayList<>();

        for (Object[] row : rawList) {
            // 假设 start_ts 对应的是时间戳，类型是 Number 或 Date 类型
            long timestamp = 0;
            if (row[0] instanceof Number) {
                timestamp = ((Number) row[0]).longValue();
            } else if (row[0] instanceof java.sql.Timestamp) {
                timestamp = ((java.sql.Timestamp) row[0]).getTime();
            }
            long count = ((Number) row[1]).longValue();

            dtoList.add(new SecondlyBrowseDTO(timestamp, count));
        }

        try {
            return objectMapper.writeValueAsString(dtoList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "[]";
        }
    }
    // 用于给前端发送数据包装
    private static class RealtimeDataWrapper {
        public List<SecondlyBrowseDTO> data;
        public long timestamp;

        public RealtimeDataWrapper(List<SecondlyBrowseDTO> data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
    }
}
