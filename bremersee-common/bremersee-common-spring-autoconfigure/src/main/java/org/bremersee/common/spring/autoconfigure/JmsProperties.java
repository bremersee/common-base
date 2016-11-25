/**
 * 
 */
package org.bremersee.common.spring.autoconfigure;

import javax.jms.Session;

import org.bremersee.common.jms.SessionTransaction;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Christian Bremer
 *
 */
@ConfigurationProperties("bremersee.jms")
public class JmsProperties {
    
    private SessionTransaction jmsSessionTransaction = SessionTransaction.AUTO;
    
    private JmsListenerContainerFactoryProperties jmsListenerContainerFactory = new JmsListenerContainerFactoryProperties(null, "1-5", null, false, null, Session.AUTO_ACKNOWLEDGE, null, null, null);

    //private JmsDestinationProperties toClientQueue = new JmsDestinationProperties("to.client.queue", false);
    
    @Override
    public String toString() {
        return "CommonProperties ["
                + "\n- jmsSessionTransaction=" + jmsSessionTransaction 
                + ", \n- jmsListenerContainerFactory="+ jmsListenerContainerFactory 
                + "]";
    }

    public SessionTransaction getJmsSessionTransaction() {
        if (jmsSessionTransaction == null || SessionTransaction.AUTO.equals(jmsSessionTransaction)) {
            try {
                Class.forName("org.springframework.transaction.jta.JtaTransactionManager");
                
                try {
                    Class.forName("bitronix.tm.jndi.BitronixContext");
                    jmsSessionTransaction = SessionTransaction.XA;
                    
                } catch (Exception e0) {
                    try {
                        Class.forName("com.atomikos.icatch.jta.UserTransactionManager");
                        jmsSessionTransaction = SessionTransaction.XA;
                        
                    } catch (Exception e1) {
                        jmsSessionTransaction = SessionTransaction.LOCAL;
                    }
                }
                
            } catch (Exception e) {
                jmsSessionTransaction = SessionTransaction.LOCAL;
            }
        }
        return jmsSessionTransaction;
    }

    public JmsListenerContainerFactoryProperties getJmsListenerContainerFactory() {
        return jmsListenerContainerFactory;
    }

    public void setJmsSessionTransaction(SessionTransaction jmsSessionTransaction) {
        this.jmsSessionTransaction = jmsSessionTransaction;
    }
    
    public static class JmsDestinationProperties extends AbstractJmsDestinationProperties {

        public JmsDestinationProperties() {
            super();
        }

        public JmsDestinationProperties(String destinationName,
                boolean pubSubDomain, boolean sessionTransacted,
                int sessionAcknowledgeMode, String concurrency, int cacheLevel,
                boolean exposeListenerSession, int maxMessagesPerTask,
                boolean pubSubNoLocal, long receiveTimeout,
                boolean subscriptionDurable, String subscriptionName,
                boolean subscriptionShared) {
            super(destinationName, pubSubDomain, sessionTransacted, sessionAcknowledgeMode,
                    concurrency, cacheLevel, exposeListenerSession, maxMessagesPerTask,
                    pubSubNoLocal, receiveTimeout, subscriptionDurable, subscriptionName,
                    subscriptionShared);
        }

        public JmsDestinationProperties(String destinationName,
                boolean pubSubDomain, String concurrency) {
            super(destinationName, pubSubDomain, concurrency);
        }

        public JmsDestinationProperties(String destinationName,
                boolean pubSubDomain) {
            super(destinationName, pubSubDomain);
        }
    }
    
    public static class JmsListenerContainerFactoryProperties extends AbstractJmsListenerContainerFactoryProperties {

        public JmsListenerContainerFactoryProperties() {
            super();
        }

        public JmsListenerContainerFactoryProperties(Integer cacheLevel,
                String concurrency, Integer maxMessagesPerTask,
                Boolean pubSubDomain, Long receiveTimeout,
                Integer sessionAcknowledgeMode, Boolean sessionTransacted,
                Boolean subscriptionDurable, Boolean subscriptionShared) {
            super(cacheLevel, concurrency, maxMessagesPerTask, pubSubDomain, receiveTimeout,
                    sessionAcknowledgeMode, sessionTransacted, subscriptionDurable,
                    subscriptionShared);
        }
    }
    
}
