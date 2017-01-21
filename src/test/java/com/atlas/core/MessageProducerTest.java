package com.atlas.core;

import com.google.common.collect.ImmutableMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.atlas.client.JsonConverter;

import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.internal.util.reflection.Whitebox;

import static org.hamcrest.CoreMatchers.startsWith;

public class MessageProducerTest extends CamelTestSupport {

    private static final String DIRECT_START = "direct:start";
    private static final String MOCK_RESULT = "mock:result";

    @EndpointInject(uri = MOCK_RESULT)
    protected MockEndpoint resultEndpoint;

    @EndpointInject(uri = DIRECT_START)
    protected ProducerTemplate producer;

    @Rule
    public ExpectedException ex = ExpectedException.none();

    private MessageProducer<String> testObj = new MessageProducer<>(new JsonConverter(new ObjectMapper()), DIRECT_START);

    @Override
    protected void doPostSetup() throws Exception {
        Whitebox.setInternalState(testObj, "producer", producer);
    }

    @Test
    public void testSend() throws Exception {
        resultEndpoint.expectedMessageCount(1);
        testObj.send("sample", "test");
        resultEndpoint.assertIsSatisfied();
    }

    @Test
    public void testSendWithHeader() throws Exception {
        resultEndpoint.expectedMessageCount(1);
        testObj.send("sample", ImmutableMap.of(MessageProducer.TYPE, "test", "any", "value"));
        resultEndpoint.assertIsSatisfied();
    }

    @Test
    public void testSendWithoutTypeHeader() throws Exception {
        ex.expect(IllegalArgumentException.class);
        ex.expectMessage(startsWith("Cannot send message without"));
        testObj.send("sample", ImmutableMap.of("K", "V"));
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from(DIRECT_START).to(MOCK_RESULT);
            }
        };
    }
}