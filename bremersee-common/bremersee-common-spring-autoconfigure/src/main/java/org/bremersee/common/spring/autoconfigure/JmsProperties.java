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

import org.bremersee.common.jms.SessionTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.jms.Session;

/**
 * @author Christian Bremer
 *
 */
@ConfigurationProperties("bremersee.jms")
public class JmsProperties {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    private SessionTransaction jmsSessionTransaction = SessionTransaction.AUTO;
    
    private JmsListenerContainerFactoryProperties jmsListenerContainerFactory = new JmsListenerContainerFactoryProperties(null, "1-5", null, false, null, Session.AUTO_ACKNOWLEDGE, null, null, null);

    @Override
    public String toString() {
        return "CommonProperties ["
                + "\n- jmsSessionTransaction=" + jmsSessionTransaction 
                + ", \n- jmsListenerContainerFactory="+ jmsListenerContainerFactory 
                + "]";
    }

    public SessionTransaction getJmsSessionTransaction() {
        if (jmsSessionTransaction == null || SessionTransaction.AUTO.equals(jmsSessionTransaction)) {
            if (isJtaTransactionManagerAvailable() && (isAtomikosAvailable() || isBitronixAvailable())) {
                jmsSessionTransaction = SessionTransaction.XA;
            } else {
                jmsSessionTransaction = SessionTransaction.LOCAL;
            }
        }
        return jmsSessionTransaction;
    }

    private boolean isJtaTransactionManagerAvailable() {
        try {
            Class.forName("org.springframework.transaction.jta.JtaTransactionManager");
            return true;
        } catch (ClassNotFoundException e) { // NOSONAR
            log.debug("JtaTransactionManager is not available.");
            return false;
        }
    }

    private boolean isAtomikosAvailable() {
        try {
            Class.forName("com.atomikos.icatch.jta.UserTransactionManager");
            return true;
        } catch (ClassNotFoundException e) { // NOSONAR
            log.debug("'om.atomikos.icatch.jta.UserTransactionManager' is not available.");
            return false;
        }
    }

    private boolean isBitronixAvailable() {
        try {
            Class.forName("bitronix.tm.jndi.BitronixContext");
            return true;
        } catch (ClassNotFoundException e) { // NOSONAR
            log.debug("'bitronix.tm.jndi.BitronixContext' is not available.");
            return false;
        }
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
