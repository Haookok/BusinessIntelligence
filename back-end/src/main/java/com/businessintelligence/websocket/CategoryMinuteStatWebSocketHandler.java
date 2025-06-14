package com.businessintelligence.websocket;

import com.businessintelligence.DTO.CategoryMinuteStatDTO;
import com.businessintelligence.repository.NewsLiveRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CategoryMinuteStatWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setName("CategoryMinuteStatScheduler");
        t.setDaemon(true);
        return t;
    });

    private final NewsLiveRepository newsLiveRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile Long lastSentTimestamp = null;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public CategoryMinuteStatWebSocketHandler(NewsLiveRepository repository) {
        this.newsLiveRepository = repository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("WebSocket connected, session id: " + session.getId());

        // ✅ 每个 session 独立发送初始历史数据
        sendInitialDataToSession(session);

        // 启动定时任务，仅一次
        if (started.compareAndSet(false, true)) {
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    sendRealtimeCategoryMinuteData(); // 增量推送
                } catch (Exception e) {
                    System.err.println("Error in scheduled task:");
                    e.printStackTrace();
                }
            }, 0, 1, TimeUnit.SECONDS);
            System.out.println("Category minute stats scheduled task started.");
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

    private void sendRealtimeCategoryMinuteData() {
        if (sessions.isEmpty()) {
            System.out.println("No active sessions. Skipping DB query and push.");
            return;
        }

        System.out.println("Triggered sendRealtimeCategoryMinuteData at " + System.currentTimeMillis());
        String jsonData = getLatestCategoryMinuteData();

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(jsonData));
                    System.out.println("✅ Sent data to session: " + session.getId());
                } catch (IOException e) {
                    System.err.println("❌ Failed to send message to session " + session.getId());
                    e.printStackTrace();
                }
            }
        }
    }

    private String getLatestCategoryMinuteData() {
        List<Object[]> rawList;

        if (lastSentTimestamp == null) {
            System.out.println("First-time full query without timestamp filter.");
            rawList = newsLiveRepository.getCategoryMinuteStatsInitial();
        } else {
            System.out.println("Querying DB since timestamp (s): " + lastSentTimestamp);
            rawList = newsLiveRepository.getCategoryMinuteStatsSince(lastSentTimestamp);
        }

        List<CategoryMinuteStatDTO> dtoList = new ArrayList<>();
        long maxTimestamp = lastSentTimestamp != null ? lastSentTimestamp : 0;

        for (Object[] row : rawList) {
            String category = (String) row[0];
            long count = ((Number) row[1]).longValue();
            long minuteTs = ((Number) row[2]).longValue();
            dtoList.add(new CategoryMinuteStatDTO(category, count, minuteTs));
            if (minuteTs > maxTimestamp) {
                maxTimestamp = minuteTs;
            }
        }

        if (!dtoList.isEmpty()) {
            lastSentTimestamp = maxTimestamp;
            System.out.println("📌 Updated lastSentTimestamp to: " + lastSentTimestamp);
        } else {
            System.out.println("ℹ No new data. Timestamp remains unchanged.");
        }

        try {
            return objectMapper.writeValueAsString(dtoList);
        } catch (JsonProcessingException e) {
            System.err.println("❌ Failed to convert DTO list to JSON.");
            e.printStackTrace();
            return "[]";
        }
    }

    private void sendInitialDataToSession(WebSocketSession session) {
        try {
            List<Object[]> rawList = newsLiveRepository.getCategoryMinuteStatsInitial();

            List<CategoryMinuteStatDTO> dtoList = new ArrayList<>();
            long maxTimestamp = 0;

            for (Object[] row : rawList) {
                String category = (String) row[0];
                long count = ((Number) row[1]).longValue();
                long minuteTs = ((Number) row[2]).longValue();
                dtoList.add(new CategoryMinuteStatDTO(category, count, minuteTs));
                if (minuteTs > maxTimestamp) {
                    maxTimestamp = minuteTs;
                }
            }

            String jsonData = objectMapper.writeValueAsString(dtoList);
            session.sendMessage(new TextMessage(jsonData));

            System.out.println("✅ Sent initial data to session " + session.getId());
        } catch (Exception e) {
            System.err.println("❌ Failed to send initial data to session " + session.getId());
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void onDestroy() {
        scheduler.shutdownNow();
        System.out.println("Scheduler shutdown on bean destroy.");
    }
}
