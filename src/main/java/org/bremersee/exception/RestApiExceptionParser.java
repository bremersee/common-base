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

package org.bremersee.exception;

import java.util.Collection;
import java.util.Map;
import org.bremersee.exception.model.RestApiException;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

/**
 * A http response parser that creates a {@link RestApiException}.
 *
 * @author Christian Bremer
 */
@Validated
public interface RestApiExceptionParser {

  /**
   * Parse the error reponse and create a rest api exception model
   *
   * @param response the response body
   * @param headers  the response headers
   * @return the rest api exception model
   */
  RestApiException parseRestApiException(
      @Nullable String response,
      @Nullable Map<String, ? extends Collection<String>> headers);

}
