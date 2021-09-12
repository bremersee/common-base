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

package org.bremersee.web.reactive.function.client;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.bremersee.exception.ErrorCodeAware;
import org.bremersee.exception.HttpResponseHeadersAware;
import org.bremersee.exception.RestApiExceptionAware;
import org.bremersee.exception.model.RestApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;

/**
 * The web client exception.
 *
 * @author Christian Bremer
 */
public class WebClientException
    extends ResponseStatusException
    implements RestApiExceptionAware, ErrorCodeAware, HttpResponseHeadersAware {

  private final Map<String, ? extends Collection<String>> headers;

  @Getter
  private final RestApiException restApiException;

  /**
   * Instantiates a new web client exception.
   *
   * @param status the status
   * @param headers the headers
   * @param restApiException the rest api exception
   */
  public WebClientException(
      final HttpStatus status,
      final Map<String, ? extends Collection<String>> headers,
      final RestApiException restApiException) {

    super(status);
    this.headers = headers != null ? headers : Collections.emptyMap();
    this.restApiException = restApiException;
  }

  /**
   * Gets the response headers.
   *
   * @return the headers
   * @deprecated in favour of {@link #getResponseHeaders()}
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  @NonNull
  @Override
  public Map<String, String> getHeaders() {
    return HttpResponseHeadersAware.createHeaders(getMultiValueHeaders());
  }

  @NonNull
  @Override
  public HttpHeaders getResponseHeaders() {
    HttpHeaders httpHeaders = new HttpHeaders();
    for (Map.Entry<String, ? extends Collection<String>> entry : headers.entrySet()) {
      httpHeaders.put(entry.getKey(), List.copyOf(entry.getValue()));
    }
    return httpHeaders;
  }

  @Override
  public Map<String, ? extends Collection<String>> getMultiValueHeaders() {
    return headers;
  }

  @Override
  public String getErrorCode() {
    return getRestApiException() != null ? getRestApiException().getErrorCode() : null;
  }

}
