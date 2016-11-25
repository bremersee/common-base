/**
 * 
 */
package org.bremersee.common.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.springframework.jms.support.destination.DestinationResolver;

/**
 * @author Christian Bremer
 *
 */
public class DestinationByNameResolver implements DestinationResolver {

    @Override
    public String toString() {
        return "DestinationByNameResolver";
    }

    @Override
    public Destination resolveDestinationName(Session session,
            String destinationName, boolean pubSubDomain) throws JMSException {

        return JmsUtils.resolveDestination(destinationName, pubSubDomain, session);
    }

}
