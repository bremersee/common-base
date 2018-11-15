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

package org.bremersee.exception.feign;

import feign.FeignException;
import feign.Request;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.bremersee.exception.ErrorCodeAware;
import org.bremersee.exception.RestApiExceptionUtils;
import org.bremersee.exception.HttpResponseHeadersAware;
import org.bremersee.exception.HttpStatusAware;
import org.bremersee.exception.RestApiExceptionAware;
import org.bremersee.exception.model.RestApiException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * @author Christian Bremer
 */
public class FeignClientException extends FeignException implements HttpStatusAware,
    HttpResponseHeadersAware, RestApiExceptionAware, ErrorCodeAware {

  @Getter
  @Nullable
  private final Request request;

  @Getter
  @NotNull
  private final Map<String, ? extends Collection<String>> headers;

  @Getter
  @Nullable
  private final RestApiException restApiException;

  @SuppressWarnings("WeakerAccess")
  public FeignClientException(
      final Request request,
      final Map<String, ? extends Collection<String>> headers,
      final int status,
      final String message,
      final RestApiException restApiException) {

    super(resolveHttpStatusCode(status), StringUtils.hasText(message)
        ? message
        : RestApiExceptionUtils.NO_MESSAGE_VALUE);
    this.request = request;
    this.headers = headers != null ? headers : Collections.emptyMap();
    this.restApiException = restApiException;
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
