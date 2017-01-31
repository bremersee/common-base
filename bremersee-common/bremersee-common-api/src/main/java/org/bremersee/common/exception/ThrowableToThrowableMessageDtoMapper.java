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

package org.bremersee.common.exception;

import org.bremersee.common.model.ThrowableMessageDto;

import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Christian Bremer
 */
@XmlTransient
public class ThrowableToThrowableMessageDtoMapper implements ThrowableToDtoMapper {

    /**
     * Maps a {@link Throwable} into a {@link ThrowableMessageDto}.
     *
     * @param t the throwable
     * @return the DTO
     */
    @Override
    public ThrowableMessageDto toDto(Throwable t) {
        return new ThrowableMessageDto(t);
    }

}