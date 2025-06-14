package com.businessintelligence.websocket;

import com.businessintelligence.DTO.CategoryStatDTO;
import com.businessintelligence.repository.NewsLiveRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class UserCategoryStatWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setName("UserCategoryStatScheduler");
        t.setDaemon(true);
        return t;
    });

    private final NewsLiveRepository newsLiveRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile Long lastSentTimestamp = null;
    private final AtomicBoolean started = new AtomicBoolean(false);

    private final Long userId = 147256L; // 测试用的用户 ID

    public UserCategoryStatWebSocketHandler(NewsLiveRepository repository) {
        this.newsLiveRepository = repository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("WebSocket connected, session id: " + session.getId());

        if (started.compareAndSet(false, true)) {
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    sendRealtimeCategoryData();
                } catch (Exception e) {
                    System.err.println("Error in scheduled task:");
                    e.printStackTrace();
                }
            }, 0, 1, TimeUnit.SECONDS);
            System.out.println("Category stats scheduled task started.");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        System.out.println("WebSocket closed, session id: " + session.getId());

        if (sessions.isEmpty()) {
            System.out.println("All sessions closed. Data send logic will auto-pause.");
        }
    }

    private void sendRealtimeCategoryData() {
        if (sessions.isEmpty()) {
            System.out.println("No active sessions. Skipping DB query and push.");
            return;
        }

        System.out.println("Triggered sendRealtimeCategoryData at " + System.currentTimeMillis());
        String jsonData = getLatestCategoryData();

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(jsonData));
                    System.out.println("Sent data to session: " + session.getId());
                } catch (IOException e) {
                    System.err.println("Failed to send message to session " + session.getId());
                    e.printStackTrace();
                }
            }
        }
    }

    private String getLatestCategoryData() {
        List<Object[]> rawList;

        if (lastSentTimestamp == null) {
            System.out.println("First-time full query without timestamp filter.");
            rawList = newsLiveRepository.getCategoryDistributionInitial(userId);
        } else {
            System.out.println("Querying DB since timestamp (s): " + lastSentTimestamp);
            rawList = newsLiveRepository.getCategoryDistributionByUserIdSince(userId, lastSentTimestamp);
        }

        List<CategoryStatDTO> dtoList = new ArrayList<>();
        long maxTimestamp = lastSentTimestamp != null ? lastSentTimestamp : 0;

        for (Object[] row : rawList) {
            String category = (String) row[0];
            long count = ((Number) row[1]).longValue();
            long secondTs = ((Number) row[2]).longValue();
            dtoList.add(new CategoryStatDTO(category, count, secondTs));
            if (secondTs > maxTimestamp) {
                maxTimestamp = secondTs;
            }
        }

        if (!dtoList.isEmpty()) {
            lastSentTimestamp = maxTimestamp + 3600;
            System.out.println("Updated lastSentTimestamp to: " + lastSentTimestamp);
        } else {
            System.out.println("ℹ No new data. Timestamp remains unchanged.");
            lastSentTimestamp = null;
        }

        try {
            return objectMapper.writeValueAsString(dtoList);
        } catch (JsonProcessingException e) {
            System.err.println("Failed to convert DTO list to JSON.");
            e.printStackTrace();
            return "[]";
        }
    }

    @PreDestroy
    public void onDestroy() {
        scheduler.shutdownNow();
        System.out.println("Scheduler shutdown on bean destroy.");
    }
}
