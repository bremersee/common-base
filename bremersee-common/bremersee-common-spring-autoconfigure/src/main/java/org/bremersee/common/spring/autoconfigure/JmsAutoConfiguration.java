/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.common.spring.autoconfigure;

import java.util.concurrent.Executor;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;

import org.bremersee.common.jms.DefaultJmsConverter;
import org.bremersee.common.jms.DestinationByNameResolver;
import org.bremersee.common.jms.SessionTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.AbstractJmsListenerContainerFactory;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author Christian Bremer
 *
 */
@Configuration
@AutoConfigureAfter(value = { SchedulingAutoConfiguration.class })
@ConditionalOnClass(name = { "javax.jms.ConnectionFactory",
        "org.springframework.jms.core.JmsTemplate",
        "org.springframework.transaction.PlatformTransactionManager",
        "org.springframework.oxm.jaxb.Jaxb2Marshaller",
        "org.bremersee.common.jms.DefaultJmsConverter" })
@EnableConfigurationProperties(JmsProperties.class)
public class JmsAutoConfiguration implements JmsListenerConfigurer {
    
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    protected JmsProperties properties;

    @Autowired
    protected ConnectionFactory connectionFactory;

    @Autowired
    @Qualifier("jaxbMarshaller")
    protected Jaxb2Marshaller marshaller;
    
    @Autowired
    protected Executor taskExecutor;

    @Autowired(required = false)
    protected PlatformTransactionManager transactionManager;

    @PostConstruct
    public void init() {
        // @formatter:off
        log.info("\n"
               + "**********************************************************************\n"
               + "*  Common Auto Configuration                                         *\n"
               + "**********************************************************************\n"
               + "properties = " + properties + "\n"
               + "connectionFactory = " + connectionFactory + "\n"
               + "taskExecutor = " + taskExecutor + "\n"
               + "transactionManager = " + transactionManager + "\n"
               + "**********************************************************************");
        // @formatter:on
    }
    
    @Bean(name = "defaultJmsConverter")
    @Primary
    public DefaultJmsConverter defaultJmsConverter() {
        DefaultJmsConverter c = new DefaultJmsConverter();
        c.setMarshaller(marshaller);
        return c;
    }

    @Bean(name = "destinationByNameResolver")
    @Primary
    public DestinationByNameResolver destinationByNameResolver() {
        return new DestinationByNameResolver();
    }
    
    @Bean(name = "defaultJmsTemplate")
    @Primary
    public JmsTemplate defaultJmsTemplate() {
        JmsTemplate impl = new JmsTemplate();
        jmsTemplateConfigurator().configure(impl, null);
        return impl;
    }
    
    @Bean(name = "defaultJmsListenerContainerFactory")
    @Primary
    public JmsListenerContainerFactory<? extends MessageListenerContainer> defaultJmsListenerContainerFactory() {
        
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        jmsListenerContainerFactoryConfigurator().configure(factory);
        return factory;
    }

    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
        registrar.setContainerFactory(defaultJmsListenerContainerFactory());
    }
    
    @Bean(name = "jmsListenerContainerFactoryConfigurator")
    @Primary
    public JmsListenerContainerFactoryConfigurator jmsListenerContainerFactoryConfigurator() {
        return new CommonJmsListenerContainerFactoryConfigurator(properties, connectionFactory, defaultJmsConverter(), taskExecutor, transactionManager);
    }
    
    @Bean(name = "jmsTemplateConfigurator")
    @Primary
    public JmsTemplateConfigurator jmsTemplateConfigurator() {
        return new CommonJmsTemplateConfigurator(properties, connectionFactory, defaultJmsConverter());
    }

    private static class CommonJmsListenerContainerFactoryConfigurator implements JmsListenerContainerFactoryConfigurator {
        
        private JmsProperties properties;
        
        private ConnectionFactory connectionFactory;
        
        private MessageConverter messageConverter;
        
        private Executor taskExecutor;
        
        private PlatformTransactionManager transactionManager;
        
        public CommonJmsListenerContainerFactoryConfigurator(
                JmsProperties properties,
                ConnectionFactory connectionFactory,
                MessageConverter messageConverter, Executor taskExecutor,
                PlatformTransactionManager transactionManager) {
            super();
            this.properties = properties;
            this.connectionFactory = connectionFactory;
            this.messageConverter = messageConverter;
            this.taskExecutor = taskExecutor;
            this.transactionManager = transactionManager;
        }

        @Override
        public void configure(
                AbstractJmsListenerContainerFactory<? extends AbstractMessageListenerContainer> factory) {
            
            if (factory != null) {
                AbstractJmsListenerContainerFactoryProperties props = properties.getJmsListenerContainerFactory();
                SessionTransaction sessionTransaction = properties.getJmsSessionTransaction();
                
                factory.setConnectionFactory(connectionFactory);
                factory.setDestinationResolver(new DestinationByNameResolver());
                factory.setMessageConverter(messageConverter);
                factory.setPubSubDomain(props.getPubSubDomain());
                factory.setSessionAcknowledgeMode(props.getSessionAcknowledgeMode());
                factory.setSessionTransacted(sessionTransaction.isSessionTransacted());
                factory.setSubscriptionDurable(props.getSubscriptionDurable());
                factory.setSubscriptionShared(props.getSubscriptionShared());
                
                if (factory instanceof DefaultJmsListenerContainerFactory) {
                    DefaultJmsListenerContainerFactory _factory = (DefaultJmsListenerContainerFactory)factory;
                    _factory.setCacheLevel(props.getCacheLevel());
                    _factory.setConcurrency(props.getConcurrency());
                    _factory.setMaxMessagesPerTask(props.getMaxMessagesPerTask());
                    _factory.setReceiveTimeout(props.getReceiveTimeout());
                    _factory.setTaskExecutor(taskExecutor);
                    if (sessionTransaction.isXA()) {
                        _factory.setTransactionManager(transactionManager);
                    }
                }
            }
        }
    }
    
    private static class CommonJmsTemplateConfigurator implements JmsTemplateConfigurator {
        
        private JmsProperties properties;
        
        private ConnectionFactory connectionFactory;
        
        private MessageConverter messageConverter;

        public CommonJmsTemplateConfigurator(JmsProperties properties,
                ConnectionFactory connectionFactory, MessageConverter messageConverter) {
            super();
            this.properties = properties;
            this.connectionFactory = connectionFactory;
            this.messageConverter = messageConverter;
        }
        
        @Override
        public void configure(JmsTemplate jmsTemplate) {
            configure(jmsTemplate, null);
        }

        @Override
        public void configure(JmsTemplate jmsTemplate,
                AbstractJmsDestinationProperties jmsDestinationProperties) {

            jmsTemplate.setConnectionFactory(connectionFactory);
            jmsTemplate.setDestinationResolver(new DestinationByNameResolver());
            jmsTemplate.setMessageConverter(messageConverter);
            jmsTemplate.setSessionTransacted(properties.getJmsSessionTransaction().isSessionTransacted());
            
            if (jmsDestinationProperties != null) {
                if (jmsDestinationProperties.getDestinationName() != null) {
                    jmsTemplate.setDefaultDestinationName(jmsDestinationProperties.getDestinationName());
                }
                jmsTemplate.setPubSubDomain(jmsDestinationProperties.isPubSubDomain());
                jmsTemplate.setPubSubNoLocal(jmsDestinationProperties.isPubSubNoLocal());
                jmsTemplate.setReceiveTimeout(jmsDestinationProperties.getReceiveTimeout());
                jmsTemplate.setSessionAcknowledgeMode(
                        jmsDestinationProperties.getSessionAcknowledgeMode());
            }
        }
    }

}
