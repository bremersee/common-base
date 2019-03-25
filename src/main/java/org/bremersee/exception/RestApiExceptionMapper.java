/*
 * Copyright 2018 the original author or authors.
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

import java.util.List;
import javax.validation.constraints.NotNull;
import org.bremersee.exception.model.RestApiException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

/**
 * Maps the error response into a {@link RestApiException}.
 *
 * @author Christian Bremer
 */
@Validated
public interface RestApiExceptionMapper {

  /**
   * Gets the paths this mapper is responsible for.
   *
   * @return the list of paths (typically ant paths)
   */
  @NotNull List<String> getApiPaths();

  /**
   * Build the exception model from the exception, the requested path and an handler. Typically the
   * handler is of type {@link org.springframework.web.method.HandlerMethod}.
   *
   * @param exception   the exception (required)
   * @param requestPath the requested path (optional)
   * @param handler     the handler (optional)
   * @return the rest api exception
   */
  @NotNull RestApiException build(
      @NotNull Throwable exception,
      @Nullable String requestPath,
      @Nullable Object handler);

  /**
   * Detects the http status.
   *
   * @param exception the exception (required)
   * @param handler   the handler (optional)
   * @return the http status
   */
  @NotNull
  HttpStatus detectHttpStatus(@NotNull Throwable exception, @Nullable Object handler);
}
