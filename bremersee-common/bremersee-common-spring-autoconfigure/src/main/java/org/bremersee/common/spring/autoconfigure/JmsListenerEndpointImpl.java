/**
 * 
 */
package org.bremersee.common.spring.autoconfigure;

import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.MessageListener;

import org.apache.commons.lang3.Validate;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.listener.endpoint.JmsActivationSpecConfig;
import org.springframework.jms.listener.endpoint.JmsMessageEndpointManager;

/**
 * @author Christian Bremer
 *
 */
public class JmsListenerEndpointImpl implements org.springframework.jms.config.JmsListenerEndpoint {

    private final AtomicInteger counter = new AtomicInteger();

    private AbstractJmsDestinationProperties destinationProperties;

    private Object messageListener;

    private String messageSelector;

    private String id;

    /**
     * Default constructor.
     */
    public JmsListenerEndpointImpl() {
    }

    public JmsListenerEndpointImpl(AbstractJmsDestinationProperties destinationProperties, Object messageListener) {
        setDestinationProperties(destinationProperties);
        setMessageListener(messageListener);
    }

    public AbstractJmsDestinationProperties getDestinationProperties() {
        return destinationProperties;
    }

    public void setDestinationProperties(AbstractJmsDestinationProperties destinationProperties) {
        this.destinationProperties = destinationProperties;
    }

    public Object getMessageListener() {
        return messageListener;
    }

    public String getMessageSelector() {
        return messageSelector;
    }

    public void setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
    }

    public void setId(String id) {
        synchronized (counter) {
            this.id = id;
        }
    }

    /**
     * Set the message listener implementation to register. This can be either a
     * standard JMS {@link MessageListener} object, a Spring
     * {@link SessionAwareMessageListener} object or a {@link BmsListener}
     * object.
     * 
     * @param messageListener
     *            the message listener
     */
    public void setMessageListener(final Object messageListener) {
        Validate.notNull(messageListener, "messageListener must not be null");
        if (messageListener instanceof MessageListener || messageListener instanceof SessionAwareMessageListener) {
            this.messageListener = messageListener;
        } else {
            throw new IllegalArgumentException("messageListener must be an object of type "
                    + MessageListener.class.getName() + " or " + SessionAwareMessageListener.class.getName());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.jms.config.JmsListenerEndpoint#getId()
     */
    @Override
    public String getId() {
        synchronized (counter) {
            if (id == null) {
                id = getClass().getName() + "#" + this.counter.getAndIncrement();
            }
            return id;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.jms.config.JmsListenerEndpoint#setupListenerContainer
     * (org.springframework.jms.listener.MessageListenerContainer)
     */
    @Override
    public void setupListenerContainer(MessageListenerContainer listenerContainer) {

        if (listenerContainer instanceof AbstractMessageListenerContainer) {
            setupJmsListenerContainer((AbstractMessageListenerContainer) listenerContainer);
        } else {
            new JcaEndpointConfigurer().configureEndpoint(listenerContainer);
        }
    }

    private void setupJmsListenerContainer(AbstractMessageListenerContainer listenerContainer) {
        if (getDestinationProperties() != null) {
            if (getDestinationProperties().getConcurrency() != null) {
                listenerContainer.setConcurrency(getDestinationProperties().getConcurrency());
            }
            if (getDestinationProperties().getDestinationName() != null) {
                listenerContainer.setDestinationName(getDestinationProperties().getDestinationName());
            }
            listenerContainer.setPubSubDomain(getDestinationProperties().isPubSubDomain());
            listenerContainer.setPubSubNoLocal(getDestinationProperties().isPubSubNoLocal());
            listenerContainer.setSessionAcknowledgeMode(getDestinationProperties().getSessionAcknowledgeMode());
            // listenerContainer.setSessionTransacted(destinationProperties.isSessionTransacted());
            listenerContainer.setSubscriptionDurable(destinationProperties.isSubscriptionDurable());
            if (getDestinationProperties().getSubscriptionName() != null) {
                listenerContainer.setSubscriptionName(getDestinationProperties().getSubscriptionName());
            }
            listenerContainer.setSubscriptionShared(getDestinationProperties().isSubscriptionShared());
        }
        if (getMessageSelector() != null) {
            listenerContainer.setMessageSelector(getMessageSelector());
        }
        setupMessageListener(listenerContainer);
    }

    private void setupMessageListener(MessageListenerContainer container) {
        container.setupMessageListener(messageListener);
    }

    /**
     * Inner class to avoid a hard dependency on the JCA API.
     */
    private class JcaEndpointConfigurer {

        public void configureEndpoint(Object listenerContainer) {
            if (listenerContainer instanceof JmsMessageEndpointManager) {
                setupJcaMessageContainer((JmsMessageEndpointManager) listenerContainer);
            } else {
                throw new IllegalArgumentException("Could not configure endpoint with the specified container '"
                        + listenerContainer + "' Only JMS (" + AbstractMessageListenerContainer.class.getName()
                        + " subclass) or JCA (" + JmsMessageEndpointManager.class.getName() + ") are supported.");
            }
        }

        private void setupJcaMessageContainer(JmsMessageEndpointManager container) {
            JmsActivationSpecConfig activationSpecConfig = container.getActivationSpecConfig();
            if (activationSpecConfig == null) {
                activationSpecConfig = new JmsActivationSpecConfig();
                container.setActivationSpecConfig(activationSpecConfig);
            }
            if (getDestinationProperties() != null) {
                if (getDestinationProperties().getConcurrency() != null) {
                    activationSpecConfig.setConcurrency(getDestinationProperties().getConcurrency());
                }
                if (getDestinationProperties().getDestinationName() != null) {
                    activationSpecConfig.setDestinationName(getDestinationProperties().getDestinationName());
                }
                activationSpecConfig.setPubSubDomain(getDestinationProperties().isPubSubDomain());
                activationSpecConfig.setSubscriptionDurable(destinationProperties.isSubscriptionDurable());
                if (getDestinationProperties().getSubscriptionName() != null) {
                    activationSpecConfig.setSubscriptionName(getDestinationProperties().getSubscriptionName());
                }
                activationSpecConfig.setSubscriptionShared(getDestinationProperties().isSubscriptionShared());
            }
            if (getMessageSelector() != null) {
                activationSpecConfig.setMessageSelector(getMessageSelector());
            }
            setupMessageListener(container);
        }
    }

}
