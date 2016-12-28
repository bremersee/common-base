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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A list of strings.
 *
 * @author Christian Bremer
 */
//@formatter:off
@ApiModel(description = "A list of strings.")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "stringList")
@XmlType(name = "stringList")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY,
        getterVisibility = Visibility.NONE,
        creatorVisibility = Visibility.NONE,
        isGetterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE)
@NoArgsConstructor
@ToString
@EqualsAndHashCode
//@formatter:on
public class StringListDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElementWrapper(name = "entries")
    @XmlElement(name = "entry")
    @JsonProperty(value = "entries")
    @ApiModelProperty("The entries of the string list.")
    private List<String> entries = new ArrayList<>();

    /**
     * Creates a list of string DTO from the specified collection.
     *
     * @param entries the source
     */
    public StringListDto(Collection<String> entries) {
        if (entries != null) {
            this.entries.addAll(entries);
        }
    }

    /**
     * @return the entries of this list
     */
    public List<String> getEntries() {
        if (entries == null) {
            entries = new ArrayList<>();
        }
        return entries;
    }

    /**
     * Sets the strings of this list.
     *
     * @param entries the source
     */
    public void setEntries(List<String> entries) {
        if (entries == null) {
            this.entries = new ArrayList<>();
        } else {
            this.entries = entries;
        }
    }

}
