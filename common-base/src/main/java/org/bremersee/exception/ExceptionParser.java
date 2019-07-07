/*
 * Copyright 2019 the original author or authors.
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

package org.bremersee.exception;

import java.util.Collection;
import java.util.Map;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

/**
 * The exception parser.
 *
 * @param <T> the parsed exception type
 * @author Christian Bremer
 */
@Validated
public interface ExceptionParser<T> {

  /**
   * Parse exception.
   *
   * @param response the response
   * @param headers  the headers
   * @return the parsed exception
   */
  T parseException(
      @Nullable String response,
      @Nullable Map<String, ? extends Collection<String>> headers);

}
