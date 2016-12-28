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
 * Gender enumeration.
 *
 * @author Christian Bremer
 */
@XmlType(name = "genderType")
public enum Gender {

    MALE("M"), FEMALE("F"), UNKNOWN("U");

    private final String stringValue;

    Gender(String stringValue) {
        this.stringValue = stringValue;
    }

    /**
     * Returns {@code M}, {@code F} or {@code null}. {@code U} (that means unknown) is not supported by GOSA.
     *
     * @return {@code M}, {@code F} or {@code null}
     */
    public String getGosaValue() {
        // gosa supports only M and F
        if ("M".equals(stringValue) || "F".equals(stringValue)) {
            return stringValue;
        }
        return null;
    }

    /**
     * Creates a gender enumeration from the GOSA representation ({@code M} or {@code F}).
     *
     * @param gosaValue {@code M} or {@code F}
     * @return the gender enumeration or {@code null}
     */
    public static Gender fromGosaValue(String gosaValue) {
        if (StringUtils.isBlank(gosaValue)) {
            return null;
        }
        for (Gender gender : Gender.values()) {
            if (gosaValue.equalsIgnoreCase(gender.stringValue)) {
                return gender;
            }
        }
        return null;
    }

}
