/**
 * 
 */
package org.bremersee.common.exception;

/**
 * @author Christian Bremer
 *
 */
public interface ThrowableToDtoMapper {
    
    Object toDto(Throwable t);

}
