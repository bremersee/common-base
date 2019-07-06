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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bremersee.exception.model.RestApiException;

/**
 * General http client exception.
 *
 * @author Christian Bremer
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class HttpClientException extends RuntimeException
    implements HttpStatusAware, RestApiExceptionAware, HttpResponseHeadersAware {

  private final int status;

  @Getter
  private final Map<String, ? extends Collection<String>> headers;

  @Getter
  private final RestApiException restApiException;

  /**
   * Instantiates a new http client exception.
   *
   * @param status           the status
   * @param message          the message
   * @param headers          the headers
   * @param restApiException the rest api exception
   */
  @SuppressWarnings("WeakerAccess")
  public HttpClientException(
      final int status,
      final String message,
      final Map<String, ? extends Collection<String>> headers,
      final RestApiException restApiException) {

    super(message);
    this.status = status;
    this.headers = headers != null ? headers : Collections.emptyMap();
    this.restApiException = restApiException;
  }

  @Override
  public int status() {
    return status;
  }

}
