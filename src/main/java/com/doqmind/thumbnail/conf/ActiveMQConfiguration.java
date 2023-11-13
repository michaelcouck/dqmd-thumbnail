package com.doqmind.thumbnail.conf;

import com.doqmind.thumbnail.model.Thumbnail;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Topic;
import java.util.Collections;
import java.util.HashMap;

/**
 * This is the configuration class for the JMS objects, the container factory
 * and the connection factory.
 *
 * @author Michael Couck
 * @version 1.0
 * @since 09-11-2023
 */
@Slf4j
@EnableJms
@Configuration
@Profile({"default", "dev", "test", "integration", "it", "uat", "prd"})
public class ActiveMQConfiguration {

    @Value("${spring.activemq.broker-url:vm://embedded?broker.useShutdownHook=false&broker.useJmx=false&broker.persistent=false}")
    private String brokerConnection;

    @Value("${thumbnail.thumbnail-topic}")
    protected String thumbnailTopicName;

    @Bean
    public ActiveMQConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setTrustedPackages(Collections.singletonList("com.doqmind"));
        factory.setBrokerURL(brokerConnection);
        return factory;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(final ConnectionFactory connectionFactory) {
        final MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        final HashMap<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put("Thumbnail", Thumbnail.class);
        messageConverter.setTypeIdPropertyName("typeId");
        messageConverter.setTypeIdMappings(typeIdMappings);

        DefaultJmsListenerContainerFactory defaultJmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
        sharedConnectionSingleConsumer(connectionFactory, defaultJmsListenerContainerFactory);
        defaultJmsListenerContainerFactory.setPubSubDomain(true);
        defaultJmsListenerContainerFactory.setMessageConverter(messageConverter);
        defaultJmsListenerContainerFactory.setConnectionFactory(connectionFactory);

        return defaultJmsListenerContainerFactory;
    }

    @Bean
    public JmsTemplate jmsTemplate(final ConnectionFactory connectionFactory) {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(connectionFactory);
        final JmsTemplate jmsTemplate = new JmsTemplate(cachingConnectionFactory);
        final MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        messageConverter.setTargetType(MessageType.TEXT);
        jmsTemplate.setMessageConverter(messageConverter);
        return jmsTemplate;
    }

    public Topic getThumbnailTopic() throws JMSException {
        return new ActiveMQTopic(thumbnailTopicName);
    }

    protected void sharedConnectionSingleConsumer(final ConnectionFactory connectionFactory,
                                                  final DefaultJmsListenerContainerFactory defaultJmsListenerContainerFactory) {
        //no need to do anything - this is only used for local profiles
    }
}