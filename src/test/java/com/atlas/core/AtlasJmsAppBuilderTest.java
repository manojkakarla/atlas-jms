package com.atlas.core;

import com.atlas.core.dw.AtlasJmsApp;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.internal.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.internal.spi.mapper.JacksonMappingProvider;
import com.atlas.client.JsonConverter;
import com.atlas.client.config.JsonConfig;
import com.atlas.core.dw.JmsConfig;
import com.atlas.core.dw.MessageConfig;
import com.atlas.core.testing.AtlasAppRule;
import com.atlas.infrastructure.SpringBundle;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

//TODO investigate
@Ignore
public class AtlasJmsAppBuilderTest {

    private static final String HEADER = "test";
    private ApplicationContext context;

    private static <T extends JmsConfig> SpringBundle<T> buildConfig() {
        return new AtlasJmsAppBuilder<T>()
                .withJms(true, MessageProducer.TYPE,
                        ImmutableMap.of("DECRYPT", TestDecryptConsumer.class, HEADER, TestConsumer.class))
                .withSpring(TestConfig.class)
                .withInfoResource()
                .buildConfig();
    }


    @ClassRule
    public static final AtlasAppRule<JmsConfig> RULE = new AtlasAppRule(AtlasJmsApp.class, buildConfig(), Resources.getResource("sample.yml").getPath());

    @Before
    public void setUp() throws Exception {
        context = ((AtlasJmsApp) RULE.getApplication()).getContext();
    }

    @Test
    public void testBuild() throws Exception {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpUriRequest request = new HttpGet("http://localhost:9090/info/properties");

        HttpResponse httpResponse = httpClient.execute(request);
        InputStream stream = httpResponse.getEntity().getContent();
        StringWriter output = new StringWriter();
        IOUtils.copy(stream, output);
        MessageConfig messageConfig = readJmsConfig(output);

        assertThat(messageConfig.getBrokerUrl()).isEqualTo("vm://localhost:61616");
    }

    private static CountDownLatch latch = new CountDownLatch(2);

    @Test
    public void testBuildJms() throws Exception {
        MessageProducer<MessageConfig> producer = context.getBean(MessageProducer.class, MessageConfig.class);
        MessageConfig message = new MessageConfig("test", "user", "password", "in", "out", null, 10);
        producer.send(message, HEADER);
        producer.send(message, "DECRYPT");
        latch.await();
        TestConsumer consumer = context.getBean(TestConsumer.class);
        assertThat(consumer.getData()).isEqualTo(message);
        TestDecryptConsumer consumer2 = context.getBean(TestDecryptConsumer.class);
        assertThat(consumer2.getData()).isEqualTo(message);
    }

    private MessageConfig readJmsConfig(StringWriter output) {
        ParseContext parseContext = parseContext(JsonConfig.configureMapper());
        return parseContext.parse(output.toString()).read("$.jms", MessageConfig.class);
    }

    private ParseContext parseContext(ObjectMapper objectMapper) {
        com.jayway.jsonpath.Configuration config = com.jayway.jsonpath.Configuration
                .builder()
                .mappingProvider(new JacksonMappingProvider(objectMapper))
                .jsonProvider(new JacksonJsonProvider())
                .build();
        return JsonPath.using(config);
    }

    @Configuration
    @Import(JsonConfig.class)
    public static class TestConfig {

        @Autowired
        private JsonConverter jsonConverter;
        @Autowired
        private JmsConfig jmsConfig;

        @Bean
        public MessageProducer messageProducer() {
            return new MessageProducer(jsonConverter, jmsConfig.getJms().getInputQueue());
        }

        @Bean
        public TestConsumer testConsumer() {
            return new TestConsumer(MessageConfig.class, latch);
        }

        @Bean
        public TestDecryptConsumer testDecryptConsumer() {
            return new TestDecryptConsumer(MessageConfig.class, latch);
        }

    }
}