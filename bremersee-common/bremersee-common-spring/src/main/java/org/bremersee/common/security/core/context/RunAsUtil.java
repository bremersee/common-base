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

package org.bremersee.common.security.core.context;

import org.apache.commons.lang3.Validate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * <p>
 * Utility class to execute a callback with another authority.
 * </p>
 *
 * @author Christian Bremer
 */
public abstract class RunAsUtil { // NOSONAR

    private RunAsUtil() {
        super();
    }

    /**
     * Executes the callback with the specified authority (name and roles).
     *
     * @param name     the name of the executing authority
     * @param roles    the roles of the executing authority
     * @param callback the callback which should be executed
     * @param <T>      the response type
     * @return the response of the callback
     */
    @SuppressWarnings("SameParameterValue")
    public static <T> T runAs(final String name, final String[] roles, final RunAsCallback<T> callback) {

        Validate.notBlank(name, "Name mast not be null or blank");
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            SecurityContextHolder.getContext().setAuthentication(new RunAsAuthentication(name, roles));
            return callback.execute();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

    /**
     * Executes the callback with the specified authority (name and roles).
     *
     * @param name     the name of the executing authority
     * @param roles    the roles of the executing authority
     * @param callback the callback which should be executed
     */
    public static void runAs(final String name, final String[] roles, final RunAsCallbackWithoutResult callback) {

        Validate.notBlank(name, "Name mast not be null or blank");
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            SecurityContextHolder.getContext().setAuthentication(new RunAsAuthentication(name, roles));
            callback.run();

        } finally {
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

}
