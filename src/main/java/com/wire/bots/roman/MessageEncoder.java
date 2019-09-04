package com.wire.bots.roman;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.roman.model.OutgoingMessage;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class MessageEncoder implements Encoder.Text<OutgoingMessage> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String encode(OutgoingMessage message) throws EncodeException {
        try {
            return mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new EncodeException(message, e.getMessage(), e);
        }
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
        // Custom initialization logic
    }

    @Override
    public void destroy() {
        // Close resources
    }
}
