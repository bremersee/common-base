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
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * <p>
 * A {@link Boolean} wrapper.
 * </p>
 * 
 * @author Christian Bremer
 */
@ApiModel(description = "A boolean wrapper object.")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "boolean")
@XmlType(name = "booleanType", propOrder = {
        "value"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.ALWAYS)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BooleanDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "value", required = true)
    @JsonProperty(value = "value", required = true)
    @ApiModelProperty(value = "The boolean value.", required = true)
    private boolean value;

}
