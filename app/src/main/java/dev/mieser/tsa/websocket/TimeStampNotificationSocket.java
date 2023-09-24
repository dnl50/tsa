package dev.mieser.tsa.websocket;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import dev.mieser.tsa.domain.TimeStampResponseData;
import dev.mieser.tsa.integration.api.IssueTimeStampService;
import dev.mieser.tsa.integration.api.TimeStampListener;
import dev.mieser.tsa.websocket.encoder.JsonEncoder;

@Slf4j
@ServerEndpoint(value = "/history/responses", encoders = JsonEncoder.class)
@ApplicationScoped
@RequiredArgsConstructor
public class TimeStampNotificationSocket implements TimeStampListener {

    private final IssueTimeStampService issueTimeStampService;

    private final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    @OnOpen
    public void sessionOpened(Session session) {
        log.debug("Websocket session opened (session ID '{}').", session.getId());
        sessions.add(session);
    }

    @OnClose
    public void sessionClosed(Session session) {
        log.debug("Websocket session closed (session ID '{}').", session.getId());
        sessions.remove(session);
    }

    @OnError
    public void sessionError(Session session, Throwable throwable) {
        log.debug("Websocket session error (session ID '{}').", session.getId(), throwable);
        sessions.remove(session);
    }

    @Override
    public void onResponse(TimeStampResponseData response) {
        sessions.forEach(session -> session.getAsyncRemote().sendObject(response, result -> {
            if (result.getException() != null) {
                log.debug("Failed to send message to session (session ID '{}').", session.getId(), result.getException());
            }
        }));
    }

    @PostConstruct
    void registerAsListener() {
        issueTimeStampService.registerListener(this);
    }

    @PreDestroy
    void unregisterListener() {
        issueTimeStampService.unregisterListener(this);
    }

}
