/**
 * 
 */
package org.bremersee.common.spring.autoconfigure;

import org.springframework.jms.core.JmsTemplate;

/**
 * @author Christian Bremer
 *
 */
public interface JmsTemplateConfigurator {

    void configure(JmsTemplate jmsTemplate);

    void configure(JmsTemplate jmsTemplate, AbstractJmsDestinationProperties jmsDestinationProperties);

}
