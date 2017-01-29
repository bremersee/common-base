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

package org.bremersee.common.exception;

/**
 * An exception that knows a custom and a HTTP status code.
 *
 * @author Christian Bremer
 */
public interface StatusCodeAwareException {

    /**
     * @return a custom status code, which can be more detailled as a HTTP status code
     */
    int getCustomStatusCode();

    /**
     * @return the HTTP status code
     */
    int getHttpStatusCode();

}
