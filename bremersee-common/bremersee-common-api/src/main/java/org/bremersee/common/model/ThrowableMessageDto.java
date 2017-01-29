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

package org.bremersee.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bremersee.common.exception.StatusCodeAwareException;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * @author Christian Bremer
 */
//@formatter:off
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "throwableMessage")
@XmlType(name = "throwableMessageType")
@XmlSeeAlso({ThrowableDto.class})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class")
@JsonSubTypes({
        @Type(ThrowableDto.class)
})
@ApiModel(
        value = "ThrowableMessageDto",
        description = "A lightweight error message.",
        discriminator = "@class",
        subTypes = {ThrowableDto.class})
@Data
@NoArgsConstructor
//@formatter:on
public class ThrowableMessageDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "customStatusCode")
    @ApiModelProperty("A custom status code.")
    private Integer customStatusCode;

    @XmlElement(name = "message")
    @ApiModelProperty("The error message.")
    private String message;

    @XmlAttribute(name = "className")
    @ApiModelProperty("The (java) class name.")
    private String className;

    public ThrowableMessageDto(Throwable t) {
        if (t != null) {
            this.className = t.getClass().getName();
            this.message = t.getMessage();
            if (t instanceof StatusCodeAwareException) {
                this.customStatusCode = ((StatusCodeAwareException) t).getCustomStatusCode();
            }
        }
    }

}
