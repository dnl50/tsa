package dev.mieser.tsa.websocket;

import java.util.ArrayList;
import java.util.List;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.OnMessage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.mieser.tsa.domain.TimeStampResponseData;

@Getter
@ClientEndpoint
@RequiredArgsConstructor
public class CachingHistoryWebsocketClient {

    private final List<TimeStampResponseData> receivedMessages = new ArrayList<>();

    private final ObjectMapper objectMapper;

    @OnMessage
    public void onMessage(String message) {
        try {
            receivedMessages.add(objectMapper.readValue(message, TimeStampResponseData.class));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
