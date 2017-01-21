package com.atlas.core.config;

import com.atlas.client.JsonConverter;
import com.atlas.client.config.JsonConfig;
import com.atlas.core.MessageProducer;
import com.atlas.core.dw.JmsConfig;
import com.atlas.core.dw.MessageConfig;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.spring.CamelBeanPostProcessor;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(JsonConfig.class)
public class SpringJmsConfig {

    private final JsonConverter jsonConverter;
    private final JmsConfig props;

    @Autowired
    public SpringJmsConfig(JsonConverter jsonConverter, JmsConfig props) {
        this.jsonConverter = jsonConverter;
        this.props = props;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public PooledConnectionFactory connectionPool() {
        MessageConfig jms = props.getJms();
        ActiveMQConnectionFactory amqConnection = new ActiveMQConnectionFactory(jms.getUsername(), jms.getPassword(), jms.getBrokerUrl());
        RedeliveryPolicy policy = amqConnection.getRedeliveryPolicy();
        policy.setInitialRedeliveryDelay(500);
        policy.setBackOffMultiplier(2);
        policy.setUseExponentialBackOff(true);
        policy.setMaximumRedeliveries(2);
        PooledConnectionFactory pool = new PooledConnectionFactory(amqConnection);
        pool.setMaxConnections(8);
        return pool;
    }


    @Bean(destroyMethod = "stop")
    public CamelContext camelContext() throws Exception {
        CamelContext context = new SpringCamelContext();
        ActiveMQComponent component = new ActiveMQComponent();
        JmsConfiguration configuration = new JmsConfiguration(connectionPool());
        configuration.setConcurrentConsumers(props.getJms().getConcurrentConsumers());
        component.setConfiguration(configuration);
        context.addComponent("activemq", component);
        context.setAutoStartup(true);
        return context;
    }

    @Bean
    public BeanPostProcessor postProcessor() throws Exception {
        return new CamelBeanPostProcessor();
    }

    @Bean
    public MessageProducer messageProducer() {
        return new MessageProducer(jsonConverter, props.getJms().getOutputQueue());
    }


}
