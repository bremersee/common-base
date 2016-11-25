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

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Christian Bremer
 *
 */
@XmlType(name = "genderType")
public enum Gender {
    
    MALE("M"), FEMALE("F");

    private final String gosaValue;
    
    private Gender(String gosaValue) {
        this.gosaValue = gosaValue;
    }
    
    public String getGosaValue() {
        return gosaValue;
    }
    
    public static Gender fromGosaValue(String gosaValue) {
        if (StringUtils.isBlank(gosaValue)) {
            return null;
        }
        for (Gender gender : Gender.values()) {
            if (gosaValue.equalsIgnoreCase(gender.gosaValue)) {
                return gender;
            }
        }
        return null;
    }
    
}
