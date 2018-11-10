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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Date;

/**
 * A common base model object.
 *
 * @author Christian Bremer
 */
@ApiModel(description = "Common base model object.")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "baseType")
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
@Getter
@Setter
@EqualsAndHashCode(of = { "id" })
@ToString
@NoArgsConstructor
public abstract class AbstractBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "id")
    @JsonProperty(value = "id")
    @ApiModelProperty(name = "id", value = "The database ID.")
    private String id;

    @XmlAttribute(name = "created")
    @JsonProperty(value = "created")
    @ApiModelProperty("The created timestamp.")
    private Date created;

    @XmlAttribute(name = "createdBy")
    @JsonProperty(value = "createdBy")
    @ApiModelProperty("The creator's name.")
    private String createdBy;

    @XmlAttribute(name = "modified")
    @JsonProperty(value = "modified")
    @ApiModelProperty("The last modified timestamp (in millis).")
    private Date modified;

    @XmlAttribute(name = "modifiedBy")
    @JsonProperty(value = "modifiedBy")
    @ApiModelProperty("The modifier's name.")
    private String modifiedBy;

}
