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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.jms.Session;

/**
 * @author Christian Bremer
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    public AbstractJmsDestinationProperties(String destinationName, boolean pubSubDomain) {
        this.destinationName = destinationName;
        this.pubSubDomain = pubSubDomain;
    }

    public AbstractJmsDestinationProperties(String destinationName, boolean pubSubDomain,
            String concurrency) {
        this.destinationName = destinationName;
        this.pubSubDomain = pubSubDomain;
        if (StringUtils.hasText(concurrency)) {
            this.concurrency = concurrency;
        }
    }

    public String getConcurrency() {
        if (isPubSubDomain()) {
            return "1";
        }
        return concurrency;
    }

}