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

/**
 * The callback that will be executed by the {@link RunAsUtil} class.
 *
 * @author Christian Bremer
 */
public interface RunAsCallback<T> {

    /**
     * Executes the method and returns it's result.
     *
     * @return the result of the method
     */
    T execute();

}
