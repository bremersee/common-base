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

package org.bremersee.common.security.acls.model;

/**
 * @author Christian Bremer
 */
public class ObjectIdentityUtils {

    public static final String ACL_TYPE_SEPARATOR = "#";

    /**
     * Never construct.
     */
    private ObjectIdentityUtils() {
        super();
    }

    public static boolean hasAttribute(final String type) {
        return type != null && type.contains(ACL_TYPE_SEPARATOR);
    }

    public static String createTypeWithAttribute(String type, String attribute) {
        return getType(type) + ACL_TYPE_SEPARATOR + attribute;
    }

    public static String getType(String typeWithAttribute) {
        if (typeWithAttribute == null) {
            return null;
        }
        final int index = typeWithAttribute.indexOf(ACL_TYPE_SEPARATOR);
        if (index < 0) {
            return typeWithAttribute;
        }
        return typeWithAttribute.substring(0, index);
    }

    public static String getAttribute(String typeWithAttribute) {
        if (typeWithAttribute == null) {
            return null;
        }
        final int index = typeWithAttribute.indexOf(ACL_TYPE_SEPARATOR);
        if (index < 0) {
            return null;
        }
        return typeWithAttribute.substring(index + ACL_TYPE_SEPARATOR.length());
    }

    public static boolean supportsAclType(final String type, final String... validTypes) {
        if (type == null || validTypes == null) {
            return false;
        }
        for (String validType : validTypes) {
            if (validType.equalsIgnoreCase(getType(type))) {
                return true;
            }
        }
        return false;
    }

}
