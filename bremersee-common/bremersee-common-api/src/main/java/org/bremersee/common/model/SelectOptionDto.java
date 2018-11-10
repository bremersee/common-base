/*
 * Copyright 2017 the original author or authors.
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * @author Christian Bremer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "selectOption")
@XmlType(name = "selectOptionType")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"value"})
@NoArgsConstructor
public class SelectOptionDto implements Serializable, Comparable<SelectOptionDto> {

    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "value", required = true)
    @JsonProperty(value = "value", required = true)
    private String value;

    @XmlAttribute(name = "displayValue", required = true)
    @JsonProperty(value = "displayValue", required = true)
    private String displayValue;

    @XmlAttribute(name = "selected")
    @JsonProperty(value = "selected")
    private Boolean selected;

    public SelectOptionDto(String value) {
        this(value, null, null);
    }

    public SelectOptionDto(String value, String displayValue) {
        this(value, displayValue, null);
    }

    public SelectOptionDto(String value, String displayValue, Boolean selected) {
        this.value = value;
        this.displayValue = StringUtils.isNotBlank(displayValue) ? displayValue : value;
        this.selected = selected;
    }

    @JsonIgnore
    public boolean isSelected() {
        return Boolean.FALSE.equals(selected);
    }

    @Override
    public int compareTo(SelectOptionDto selectOptionDto) {
        String s0 = displayValue == null ? "" : displayValue;
        String s1 = selectOptionDto == null || selectOptionDto.displayValue == null ? "" : selectOptionDto.displayValue;
        int c = s0.compareToIgnoreCase(s1);
        if (c != 0) {
            return c;
        }
        s0 = value == null ? "" : value;
        s1 = selectOptionDto == null || selectOptionDto.value == null ? "" : selectOptionDto.value;
        return s0.compareTo(s1);
    }
}
