package com.atlas.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import com.atlas.client.JsonConverter;

import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageProducer<T> {

    public static final String TYPE = "JMSType";
    @EndpointInject
    protected ProducerTemplate producer;
    private final JsonConverter jsonConverter;
    private final String queue;


    public MessageProducer(JsonConverter jsonConverter, String queue) {
        this.jsonConverter = jsonConverter;
        this.queue = queue;
    }

    public void send(T message, Object type) {
        send(message, type, queue);
    }

    public void send(T message, Object type, String endpoint) {
        send(message, ImmutableMap.of(TYPE, type), endpoint);
    }

    public void send(T message, Map<String, Object> headers) {
        send(message, headers, queue);
    }

    public void send(T message, Map<String, Object> headers, String endpoint) {
        Preconditions.checkArgument(headers.containsKey(TYPE), "Cannot send message without [%s] header", TYPE);
        String json = jsonConverter.convertToJson(message);
        log.info("Sending message: {} with headers: {} to queue: [{}]", json, headers, queue);

        producer.sendBodyAndHeaders(endpoint, json, headers);
    }
}
