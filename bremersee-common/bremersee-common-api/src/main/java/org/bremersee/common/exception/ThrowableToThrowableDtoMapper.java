/**
 * 
 */
package org.bremersee.common.exception;

import javax.xml.bind.annotation.XmlTransient;

import org.bremersee.common.model.ThrowableDto;

/**
 * @author Christian Bremer
 *
 */
@XmlTransient
public class ThrowableToThrowableDtoMapper implements ThrowableToDtoMapper {

    @Override
    public Object toDto(Throwable t) {
        return new ThrowableDto(t);
    }

}
