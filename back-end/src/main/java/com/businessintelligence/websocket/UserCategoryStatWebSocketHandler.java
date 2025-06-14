package com.businessintelligence.websocket;

import com.businessintelligence.DTO.CategoryStatDTO;
import com.businessintelligence.repository.NewsLiveRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

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
    private final AtomicBoolean started = new AtomicBoolean(false);

    // 每个 session 的 userId 和 lastTimestamp
    private final ConcurrentHashMap<WebSocketSession, Long> sessionUserIdMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<WebSocketSession, Long> sessionLastTimestampMap = new ConcurrentHashMap<>();

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
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        if (session == null || message == null) {
            System.err.println("⚠️ handleTextMessage received null session or message.");
            return;
        }

        String payload = message.getPayload();
        System.out.println("📥 接收到前端消息原文: \"" + payload + "\" 来自 session " + session.getId());

        try {
            long userId = Long.parseLong(payload.trim());
            sessionUserIdMap.put(session, userId);
            sessionLastTimestampMap.put(session, null); // 重置时间戳
            System.out.println("✅ 解析成功，userId: " + userId + "，已存储到 session " + session.getId());
        } catch (NumberFormatException e) {
            System.err.println("❌ 无效的 userId 格式，session " + session.getId() + " 发送了: \"" + payload + "\"");
        } catch (Exception ex) {
            System.err.println("❌ handleTextMessage 异常: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        sessionUserIdMap.remove(session);
        sessionLastTimestampMap.remove(session);
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

        for (WebSocketSession session : sessions) {
            if (session == null || !session.isOpen()) continue;

            try {
                Long userId = sessionUserIdMap.get(session);
                if (userId == null) continue;

                Long lastSentTimestamp = sessionLastTimestampMap.get(session);
                List<Object[]> rawList;

                if (lastSentTimestamp == null) {
                    System.out.println("First-time query for session " + session.getId() + ", userId: " + userId);
                    rawList = newsLiveRepository.getCategoryDistributionInitial(userId);
                } else {
                    System.out.println("Query for session " + session.getId() + " since " + lastSentTimestamp);
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
                    sessionLastTimestampMap.put(session, maxTimestamp + 3600);
                    System.out.println("🔁 Session " + session.getId() + " 更新 lastSentTimestamp: " + (maxTimestamp + 3600));
                } else {
                    sessionLastTimestampMap.put(session, null);
                    System.out.println("ℹ Session " + session.getId() + " 没有新数据。");
                }

                String json = objectMapper.writeValueAsString(dtoList);
                session.sendMessage(new TextMessage(json));
                System.out.println("✅ Sent data to session: " + session.getId());
            } catch (Exception e) {
                System.err.println("❌ Error during sending data to session: " + session.getId());
                e.printStackTrace();
            }
        }
    }

    @PreDestroy
    public void onDestroy() {
        scheduler.shutdownNow();
        System.out.println("Scheduler shutdown on bean destroy.");
    }
}
