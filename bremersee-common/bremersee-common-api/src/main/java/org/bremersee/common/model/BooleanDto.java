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

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * <p>
 * A {@link Boolean} wrapper.
 * </p>
 * 
 * @author Christian Bremer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "boolean")
@XmlType(name = "booleanType", propOrder = {
        "value"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, 
    getterVisibility = Visibility.NONE, 
    creatorVisibility = Visibility.NONE, 
    isGetterVisibility = Visibility.NONE, 
    setterVisibility = Visibility.NONE)
@JsonPropertyOrder(value = {
        "value" 
})
public class BooleanDto implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "value", required = true)
    @JsonProperty(value = "value", required = true)
    private boolean value;

    /**
     * Default constructor.
     */
    public BooleanDto() {
    }

    /**
     * Constructs an instance with the specified parameter.
     * 
     * @param value
     *            the boolean value
     */
    public BooleanDto(boolean value) {
        this.value = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "BooleanDto [value=" + value + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Boolean.valueOf(value).hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof Boolean) {
            return obj.equals(value);
        }
        if (getClass() != obj.getClass())
            return false;
        BooleanDto other = (BooleanDto) obj;
        if (value != other.value)
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    public BooleanDto clone() {
        return new BooleanDto(isValue());
    }

    /**
     * Return the boolean value.
     * 
     * @return the boolean value
     */
    public boolean isValue() {
        return value;
    }

    /**
     * Sets the boolean value.
     * 
     * @param value
     *            the boolean value
     */
    public void setValue(boolean value) {
        this.value = value;
    }

}
