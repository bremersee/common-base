/**
 * 
 */
package org.bremersee.common.spring.autoconfigure;

import org.springframework.jms.config.AbstractJmsListenerContainerFactory;
import org.springframework.jms.listener.AbstractMessageListenerContainer;

/**
 * @author Christian Bremer
 *
 */
public interface JmsListenerContainerFactoryConfigurator {
    
    void configure(AbstractJmsListenerContainerFactory<? extends AbstractMessageListenerContainer> factory);

}
