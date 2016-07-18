/**
 * 
 */
package org.bremersee.common.spring.autoconfigure;

/**
 * @author Christian Bremer
 *
 */
public abstract class AbstractJmsListenerContainerFactoryProperties {
    
    private Integer cacheLevel;
    
    private String concurrency;
    
    private Integer maxMessagesPerTask;
    
    private Boolean pubSubDomain;
    
    private Long receiveTimeout;
    
    private Integer sessionAcknowledgeMode;
    
    private Boolean sessionTransacted;
    
    private Boolean subscriptionDurable;
    
    private Boolean subscriptionShared;
    
    public AbstractJmsListenerContainerFactoryProperties() {
        super();
    }

    public AbstractJmsListenerContainerFactoryProperties(Integer cacheLevel,
            String concurrency, Integer maxMessagesPerTask,
            Boolean pubSubDomain, Long receiveTimeout,
            Integer sessionAcknowledgeMode, Boolean sessionTransacted,
            Boolean subscriptionDurable, Boolean subscriptionShared) {
        super();
        this.cacheLevel = cacheLevel;
        this.concurrency = concurrency;
        this.maxMessagesPerTask = maxMessagesPerTask;
        this.pubSubDomain = pubSubDomain;
        this.receiveTimeout = receiveTimeout;
        this.sessionAcknowledgeMode = sessionAcknowledgeMode;
        this.sessionTransacted = sessionTransacted;
        this.subscriptionDurable = subscriptionDurable;
        this.subscriptionShared = subscriptionShared;
    }

    @Override
    public String toString() {
        // @formatter:off
        return "JmsListenerContainerFactoryProperties ["
                + "\n  - cacheLevel=" + cacheLevel 
                + ", \n  - concurrency=" + concurrency
                + ", \n  - maxMessagesPerTask=" + maxMessagesPerTask
                + ", \n  - pubSubDomain=" + pubSubDomain 
                + ", \n  - receiveTimeout=" + receiveTimeout 
                + ", \n  - sessionAcknowledgeMode=" + sessionAcknowledgeMode 
                + ", \n  - sessionTransacted=" + sessionTransacted 
                + ", \n  - subscriptionDurable=" + subscriptionDurable 
                + ", \n  - subscriptionShared=" + subscriptionShared + "]";
        // @formatter:on
    }

    public Integer getCacheLevel() {
        return cacheLevel;
    }

    public void setCacheLevel(Integer cacheLevel) {
        this.cacheLevel = cacheLevel;
    }

    public String getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(String concurrency) {
        this.concurrency = concurrency;
    }

    public Integer getMaxMessagesPerTask() {
        return maxMessagesPerTask;
    }

    public void setMaxMessagesPerTask(Integer maxMessagesPerTask) {
        this.maxMessagesPerTask = maxMessagesPerTask;
    }

    public Boolean getPubSubDomain() {
        return pubSubDomain;
    }

    public void setPubSubDomain(Boolean pubSubDomain) {
        this.pubSubDomain = pubSubDomain;
    }

    public Long getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setReceiveTimeout(Long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    public Integer getSessionAcknowledgeMode() {
        return sessionAcknowledgeMode;
    }

    public void setSessionAcknowledgeMode(Integer sessionAcknowledgeMode) {
        this.sessionAcknowledgeMode = sessionAcknowledgeMode;
    }

    public Boolean getSessionTransacted() {
        return sessionTransacted;
    }

    public void setSessionTransacted(Boolean sessionTransacted) {
        this.sessionTransacted = sessionTransacted;
    }

    public Boolean getSubscriptionDurable() {
        return subscriptionDurable;
    }

    public void setSubscriptionDurable(Boolean subscriptionDurable) {
        this.subscriptionDurable = subscriptionDurable;
    }

    public Boolean getSubscriptionShared() {
        return subscriptionShared;
    }

    public void setSubscriptionShared(Boolean subscriptionShared) {
        this.subscriptionShared = subscriptionShared;
    }

}
