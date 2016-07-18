/**
 * 
 */
package org.bremersee.common.spring.autoconfigure;

import javax.jms.Session;

import org.springframework.util.StringUtils;

/**
 * @author Christian Bremer
 *
 */
public abstract class AbstractJmsDestinationProperties {

    private String destinationName;
    
    private boolean pubSubDomain = false;
    
    private boolean sessionTransacted = false;
    
    private int sessionAcknowledgeMode = Session.AUTO_ACKNOWLEDGE;
    
    private String concurrency = "1-4";
    
    private int cacheLevel = 4;
    
    private boolean exposeListenerSession = true;
    
    private int maxMessagesPerTask = -1;
    
    private boolean pubSubNoLocal = false;
    
    private long receiveTimeout = 1000L;
    
    private boolean subscriptionDurable = false;
    
    private String subscriptionName = null;
    
    private boolean subscriptionShared = false;

    public AbstractJmsDestinationProperties() {
        super();
    }

    public AbstractJmsDestinationProperties(String destinationName, boolean pubSubDomain) {
        super();
        this.destinationName = destinationName;
        this.pubSubDomain = pubSubDomain;
    }

    public AbstractJmsDestinationProperties(String destinationName, boolean pubSubDomain,
            String concurrency) {
        super();
        this.destinationName = destinationName;
        this.pubSubDomain = pubSubDomain;
        if (StringUtils.hasText(concurrency)) {
            this.concurrency = concurrency;
        }
    }

    public AbstractJmsDestinationProperties(String destinationName,
            boolean pubSubDomain, boolean sessionTransacted,
            int sessionAcknowledgeMode, String concurrency, int cacheLevel,
            boolean exposeListenerSession, int maxMessagesPerTask,
            boolean pubSubNoLocal, long receiveTimeout,
            boolean subscriptionDurable, String subscriptionName,
            boolean subscriptionShared) {
        super();
        this.destinationName = destinationName;
        this.pubSubDomain = pubSubDomain;
        this.sessionTransacted = sessionTransacted;
        this.sessionAcknowledgeMode = sessionAcknowledgeMode;
        if (StringUtils.hasText(concurrency)) {
            this.concurrency = concurrency;
        }
        this.cacheLevel = cacheLevel;
        this.exposeListenerSession = exposeListenerSession;
        this.maxMessagesPerTask = maxMessagesPerTask;
        this.pubSubNoLocal = pubSubNoLocal;
        this.receiveTimeout = receiveTimeout;
        this.subscriptionDurable = subscriptionDurable;
        this.subscriptionName = subscriptionName;
        this.subscriptionShared = subscriptionShared;
    }

    @Override
    public String toString() {
        // @formatter:off
        return "JmsDestinationProperties ["
                + "\n  - destinationName=" + destinationName 
                + ", \n  - pubSubDomain=" + pubSubDomain
                + ", \n  - sessionTransacted=" + sessionTransacted
                + ", \n  - sessionAcknowledgeMode=" + sessionAcknowledgeMode
                + ", \n  - concurrency=" + concurrency 
                + ", \n  - cacheLevel=" + cacheLevel
                + ", \n  - exposeListenerSession=" + exposeListenerSession
                + ", \n  - maxMessagesPerTask=" + maxMessagesPerTask
                + ", \n  - pubSubNoLocal=" + pubSubNoLocal 
                + ", \n  - receiveTimeout=" + receiveTimeout 
                + ", \n  - subscriptionDurable=" + subscriptionDurable 
                + ", \n  - subscriptionName=" + subscriptionName
                + ", \n  - subscriptionShared=" + subscriptionShared + "]";
        // @formatter:on
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public boolean isPubSubDomain() {
        return pubSubDomain;
    }

    public void setPubSubDomain(boolean pubSubDomain) {
        this.pubSubDomain = pubSubDomain;
    }

    public boolean isSessionTransacted() {
        return sessionTransacted;
    }

    public void setSessionTransacted(boolean sessionTransacted) {
        this.sessionTransacted = sessionTransacted;
    }

    public int getSessionAcknowledgeMode() {
        return sessionAcknowledgeMode;
    }

    public void setSessionAcknowledgeMode(int sessionAcknowledgeMode) {
        this.sessionAcknowledgeMode = sessionAcknowledgeMode;
    }

    public String getConcurrency() {
        if (isPubSubDomain()) {
            return "1";
        }
        return concurrency;
    }

    public void setConcurrency(String concurrency) {
        this.concurrency = concurrency;
    }

    public int getCacheLevel() {
        return cacheLevel;
    }

    public void setCacheLevel(int cacheLevel) {
        this.cacheLevel = cacheLevel;
    }

    public boolean isExposeListenerSession() {
        return exposeListenerSession;
    }

    public void setExposeListenerSession(boolean exposeListenerSession) {
        this.exposeListenerSession = exposeListenerSession;
    }

    public int getMaxMessagesPerTask() {
        return maxMessagesPerTask;
    }

    public void setMaxMessagesPerTask(int maxMessagesPerTask) {
        this.maxMessagesPerTask = maxMessagesPerTask;
    }

    public boolean isPubSubNoLocal() {
        return pubSubNoLocal;
    }

    public void setPubSubNoLocal(boolean pubSubNoLocal) {
        this.pubSubNoLocal = pubSubNoLocal;
    }

    public long getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    public boolean isSubscriptionDurable() {
        return subscriptionDurable;
    }

    public void setSubscriptionDurable(boolean subscriptionDurable) {
        this.subscriptionDurable = subscriptionDurable;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    public boolean isSubscriptionShared() {
        return subscriptionShared;
    }

    public void setSubscriptionShared(boolean subscriptionShared) {
        this.subscriptionShared = subscriptionShared;
    }
    
}
