package com.atlas.core;

import com.atlas.client.JsonConverter;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageProcessor implements Processor {

    private final MessageConsumer<?> consumer;
    private final JsonConverter jsonConverter;

    public MessageProcessor(MessageConsumer<?> consumer, JsonConverter jsonConverter) {
        this.consumer = consumer;
        this.jsonConverter = jsonConverter;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        exchange.getIn().setBody(jsonConverter.parseFromJson(body, consumer.getHandledType()));
        log.debug("Converting: [{}] to: {}", body, exchange.getIn().getBody());

    }
}
