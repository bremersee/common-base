/*
 * Copyright 2015 the original author or authors.
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

package org.bremersee.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A DTO of a throwable object.
 *
 * @author Christian Bremer
 */
//@formatter:off
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "throwable")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@ApiModel(
        value = "ThrowableDto",
        description = "A full described error message.",
        discriminator = "@class",
        parent = ThrowableMessageDto.class)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
//@formatter:on
public class ThrowableDto extends ThrowableMessageDto {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @XmlElementWrapper(name = "stackTrace")
    @XmlElement(name = "stackTraceElement")
    @ApiModelProperty("The stack trace.")
    private List<StackTraceElementDto> stackTrace = new ArrayList<>();

    @XmlElement(name = "cause")
    @ApiModelProperty("The cause.")
    private ThrowableDto cause;

    /**
     * Creates the DTO of the specified throwable.
     *
     * @param throwable the source object
     */
    public ThrowableDto(Throwable throwable) {
        super(throwable);
        if (throwable != null) {
            StackTraceElement[] stackTraceElements = throwable.getStackTrace();
            if (stackTraceElements != null) {
                for (StackTraceElement elem : stackTraceElements) {
                    this.stackTrace.add(new StackTraceElementDto(
                            elem.getClassName(),
                            elem.getMethodName(),
                            elem.getFileName(),
                            elem.getLineNumber()));
                }
            }
            if (throwable.getCause() != null) {
                this.cause = new ThrowableDto(throwable.getCause());
            }
        }
    }

}
