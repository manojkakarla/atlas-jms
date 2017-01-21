package com.atlas.core.config;

import com.atlas.core.MessageProducer;
import com.atlas.core.resource.GatewayResource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class GatewayConfig {

    private final MessageProducer<Map<String, String>> messageProducer;

    @Autowired
    public GatewayConfig(MessageProducer<Map<String, String>> messageProducer) {
        this.messageProducer = messageProducer;
    }

    @Bean
    public GatewayResource gatewayResource() {
        return new GatewayResource(messageProducer);
    }
}
