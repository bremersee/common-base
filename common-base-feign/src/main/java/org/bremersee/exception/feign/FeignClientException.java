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

package org.bremersee.exception.feign;

import feign.FeignException;
import feign.Request;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.bremersee.exception.ErrorCodeAware;
import org.bremersee.exception.HttpResponseHeadersAware;
import org.bremersee.exception.HttpStatusAware;
import org.bremersee.exception.RestApiExceptionAware;
import org.bremersee.exception.RestApiExceptionUtils;
import org.bremersee.exception.model.RestApiException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * Feign exception that stores the error payload as a {@link RestApiException}. If the error payload
 * cannot be parsed as {@link RestApiException}, the whole body of the error payload will be stored
 * in the message field of the {@link RestApiException}.
 *
 * @author Christian Bremer
 */
public class FeignClientException extends FeignException implements HttpStatusAware,
    HttpResponseHeadersAware, RestApiExceptionAware, ErrorCodeAware {

  @Getter
  @Nullable
  private final Request request;

  @NotNull
  private final Map<String, ? extends Collection<String>> headers;

  @Getter
  @Nullable
  private final RestApiException restApiException;

  /**
   * Default constructor.
   *
   * @param request          the original request
   * @param headers          the response headers
   * @param status           the response status code
   * @param message          the message of this {@link FeignClientException}
   * @param restApiException the rest api exception
   */
  @SuppressWarnings("WeakerAccess")
  public FeignClientException(
      @Nullable final Request request,
      @Nullable final Map<String, ? extends Collection<String>> headers,
      final int status,
      @Nullable final String message,
      @Nullable final RestApiException restApiException) {

    super(
        resolveHttpStatusCode(status),
        StringUtils.hasText(message)
            ? message
            : RestApiExceptionUtils.NO_MESSAGE_VALUE,
        request != null
            ? request.body()
            : null);
    this.request = request;
    this.headers = headers != null ? headers : Collections.emptyMap();
    this.restApiException = restApiException;
  }

  @Override
  public Map<String, String> getHeaders() {
    return HttpResponseHeadersAware.createHeaders(getMultiValueHeaders());
  }

  @Override
  public Map<String, ? extends Collection<String>> getMultiValueHeaders() {
    return headers;
  }

  @Override
  public String getErrorCode() {
    return restApiException != null ? restApiException.getErrorCode() : null;
  }

  private static int resolveHttpStatusCode(final int httpStatusCode) {
    final HttpStatus httpStatus = HttpStatus.resolve(httpStatusCode);
    return httpStatus != null ? httpStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR.value();
  }

}
