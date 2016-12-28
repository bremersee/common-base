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

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.googlecode.jmapper.annotations.JMap;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * @author Christian Bremer
 *
 */
//@formatter:off
@ApiModel(description = "A stack trace element of a throwable DTO.")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "stackTraceElementType", propOrder = {
        "declaringClass",
        "methodName",
        "fileName",
        "lineNumber"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonAutoDetect(
        fieldVisibility = Visibility.ANY, 
        getterVisibility = Visibility.NONE, 
        creatorVisibility = Visibility.NONE, 
        isGetterVisibility = Visibility.NONE, 
        setterVisibility = Visibility.NONE
)
@JsonPropertyOrder({
        "declaringClass",
        "methodName",
        "fileName",
        "lineNumber"
})
@Data
@NoArgsConstructor
@AllArgsConstructor
//@formatter:on
public class StackTraceElementDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(name = "declaringClass")
    @JsonProperty(value = "declaringClass")
    @ApiModelProperty("The declaring class.")
    @JMap
    private String declaringClass;
    
    @XmlElement(name = "methodName")
    @JsonProperty(value = "methodName")
    @ApiModelProperty("The method name.")
    @JMap
    private String methodName;
    
    @XmlElement(name = "fileName")
    @JsonProperty(value = "fileName")
    @ApiModelProperty("The file name.")
    @JMap
    private String fileName;
    
    @XmlElement(name = "lineNumber", defaultValue = "0")
    @JsonProperty(value = "lineNumber", defaultValue = "0")
    @ApiModelProperty("The line number.")
    @JMap
    private int    lineNumber;

    public StackTraceElement toStackTraceElement() {
        return new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
    }

}
