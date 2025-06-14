package com.businessintelligence.websocket;

import com.businessintelligence.DTO.MinutelyBrowseDTO;
import com.businessintelligence.repository.NewsLiveRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setName("DataWebSocketScheduler");
        t.setDaemon(true);
        return t;
    });

    private final NewsLiveRepository newsLiveRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile Long lastSentTimestamp = null; // 秒级时间戳
    private final AtomicBoolean started = new AtomicBoolean(false);

    public DataWebSocketHandler(NewsLiveRepository newsLiveRepository) {
        this.newsLiveRepository = newsLiveRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("WebSocket connected, session id: " + session.getId());

        if (started.compareAndSet(false, true)) {
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    sendRealtimeData();
                } catch (Exception e) {
                    System.err.println("Unhandled exception in scheduled task:");
                    e.printStackTrace();
                }
            }, 0, 1, TimeUnit.SECONDS);
            System.out.println("Scheduled task started.");
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

    private void sendRealtimeData() {
        if (sessions.isEmpty()) {
            System.out.println("No active sessions, skipping sendRealtimeData");
            return;
        }

        System.out.println("Triggered sendRealtimeData at " + System.currentTimeMillis());
        String jsonData = getLatestDataFromDB();

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

    private String getLatestDataFromDB() {
        List<Object[]> rawList;

        if (lastSentTimestamp == null) {
            System.out.println("First-time full query without timestamp filter.");
            rawList = newsLiveRepository.getSecondlyBrowseInitial(572);
        } else {
            System.out.println("Querying DB since timestamp (s): " + lastSentTimestamp);
            rawList = newsLiveRepository.getSecondlyBrowseSince(572, lastSentTimestamp);
        }

        System.out.println("Retrieved " + rawList.size() + " rows from DB.");
        List<MinutelyBrowseDTO> dtoList = new ArrayList<>();
        long maxTimestamp = lastSentTimestamp != null ? lastSentTimestamp : 0;

        for (Object[] row : rawList) {
            long secondTs = ((Number) row[0]).longValue();
            long count = ((Number) row[1]).longValue();
            dtoList.add(new MinutelyBrowseDTO(secondTs, count));
            if (secondTs > maxTimestamp) {
                maxTimestamp = secondTs;
            }
        }

        if (!dtoList.isEmpty()) {
            lastSentTimestamp = maxTimestamp + 1;
            System.out.println("Updated lastSentTimestamp to: " + lastSentTimestamp);
        } else {
            System.out.println("No new data. Timestamp remains unchanged.");
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
