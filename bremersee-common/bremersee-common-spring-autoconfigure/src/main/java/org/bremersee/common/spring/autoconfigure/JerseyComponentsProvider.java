/**
 * 
 */
package org.bremersee.common.spring.autoconfigure;

import java.util.Map;

/**
 * @author Christian Bremer
 *
 */
public interface JerseyComponentsProvider {
    
    Object[] getJerseyComponents();
    
    Map<Class<? extends RuntimeException>, Integer> getExceptionStatusCodeMap();

}
