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
import java.util.Map;
import lombok.Getter;
import org.bremersee.exception.HttpResponseHeadersAware;
import org.bremersee.exception.HttpStatusAware;
import org.bremersee.exception.RestApiExceptionAware;
import org.bremersee.exception.model.RestApiException;
import org.springframework.http.HttpStatus;

/**
 * @author Christian Bremer
 */
public class FeignClientException extends FeignException implements HttpStatusAware,
    HttpResponseHeadersAware, RestApiExceptionAware {

  @Getter
  private final Request request;

  @Getter
  private final Map<String, Collection<String>> headers;

  @Getter
  private final RestApiException restApiException; // Do not rename! It's used with reflection!

  public FeignClientException(
      final Request request,
      final Map<String, Collection<String>> headers,
      final int status,
      final String message,
      final RestApiException restApiException) {

    super(resolveHttpStatusCode(status), message);
    this.request = request;
    this.headers = headers;
    this.restApiException = restApiException;
  }

  public String getErrorCode() {
    return restApiException != null ? restApiException.getErrorCode() : null;
  }

  private static int resolveHttpStatusCode(final int httpStatusCode) {
    final HttpStatus httpStatus = HttpStatus.resolve(httpStatusCode);
    return httpStatus != null ? httpStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR.value();
  }

}
