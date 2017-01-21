package com.atlas.core.config;

import com.atlas.core.MessageConsumer;
import com.google.common.base.Throwables;

import com.atlas.client.JsonConverter;
import com.atlas.core.MessageProcessor;
import com.atlas.core.dw.JmsConfig;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.RouteDefinition;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Router {

    public static final String DEFAULT = "default";
    private final String header;
    private final Map<String, Class<? extends MessageConsumer>> routeMap;
    private Map<String, MessageConsumer> consumerMap = new HashMap<>();
    private boolean traceEnabled;

    public void addRoutes(ApplicationContext context) throws BeansException {
        if (getRouteMap() != null) {
            for (Map.Entry<String, Class<? extends MessageConsumer>> entry : getRouteMap().entrySet()) {
                getConsumerMap().put(entry.getKey(), context.getBean(entry.getValue()));
            }
        }
        routeBuilder(context);
    }

    private void routeBuilder(ApplicationContext context) {
        try {
            log.info(" =================================== Configuring Route ===================================");
            Router router = context.getBean(Router.class);
            JmsConfig props = context.getBean(JmsConfig.class);
            JsonConverter jsonConverter = context.getBean(JsonConverter.class);
            RouteBuilder routeBuilder = new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    String inputQueue = props.getJms().getInputQueue();
                    String deadLetterUri = inputQueue + ".DLQ";
                    errorHandler(deadLetterChannel(deadLetterUri).useOriginalMessage());
                    if (traceEnabled) {
                        log.info("Enabling tracing");
                        getContext().setTracing(true);
                    }
                    log.info("Failed messages go to: {}", deadLetterUri);

                    RouteDefinition route = from(inputQueue);
                    log.info("Route from: {}", inputQueue);
                    if (router.getHeader() != null && router.getRouteMap() != null) {
                        ChoiceDefinition choice = route.choice();
                        MessageConsumer otherwise = null;
                        for (Map.Entry<String, MessageConsumer> entry : router.getConsumerMap().entrySet()) {
                            if (entry.getKey().toLowerCase().equals(DEFAULT)) {
                                otherwise = entry.getValue();
                            } else {
                                choice = choice
                                    .when(header(router.getHeader()).isEqualTo(entry.getKey()))
                                    .process(converter(entry.getValue(), jsonConverter))
                                    .bean(entry.getValue());
                                logChoice("choice", entry.getKey(), entry.getValue());
                            }
                        }
                        if (otherwise != null) {
                            choice.otherwise().process(converter(otherwise, jsonConverter)).bean(otherwise);
                            logChoice("default choice", "", otherwise);

                        }
                    } else {
                        String[] consumers = context.getBeanNamesForType(MessageConsumer.class);
                        if (consumers.length > 1) {
                            String message = "Multiple consumers found, Did you forget to add routing map? "
                                             + "Found consumers: " + Arrays.toString(consumers);
                            log.error(message);
                            throw new IllegalStateException(message);
                        } else if (consumers.length == 1) {
                            MessageConsumer messageConsumer = context.getBean(MessageConsumer.class);
                            route
                                .process(converter(messageConsumer, jsonConverter))
                                .bean(messageConsumer);
                            logChoice("direct route", "", messageConsumer);
                        } else {
                            route.log("Received message: ${body}").stop();
                            log.info("No consumer found, messages are just logged from: {}", inputQueue);
                        }
                    }
                }

            };
            CamelContext camelContext = context.getBean(CamelContext.class);
            camelContext.addRoutes(routeBuilder);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Throwables.propagate(e);
        } finally {
            log.info(" =================================== completed Routing ===================================");
        }
    }

    private void logChoice(final String tag, String header, MessageConsumer consumer) {
        log.info("Added {}: [{}] -> [{}({})]", tag, header, consumer.getClass().getSimpleName(), consumer.getHandledType().getSimpleName());
    }

    private Processor converter(MessageConsumer consumer, JsonConverter jsonConverter) {
        return new MessageProcessor(consumer, jsonConverter);
    }
}
