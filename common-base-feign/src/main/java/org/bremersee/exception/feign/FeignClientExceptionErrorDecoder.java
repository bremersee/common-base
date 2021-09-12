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

import static feign.Util.RETRY_AFTER;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.bremersee.http.HttpHeadersHelper.buildHttpHeaders;
import static org.bremersee.http.HttpHeadersHelper.getContentCharset;

import feign.Request.HttpMethod;
import feign.Response;
import feign.RetryableException;
import feign.Util;
import feign.codec.ErrorDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionParser;
import org.bremersee.exception.RestApiExceptionParserImpl;
import org.bremersee.exception.model.RestApiException;
import org.springframework.http.HttpHeaders;

/**
 * This error decoder produces either a {@link FeignClientException} or a {@link feign.RetryableException}.
 *
 * @author Christian Bremer
 */
@Slf4j
public class FeignClientExceptionErrorDecoder implements ErrorDecoder {

  private final RestApiExceptionParser parser;

  /**
   * Instantiates a new feign client exception error decoder.
   */
  @SuppressWarnings("WeakerAccess")
  public FeignClientExceptionErrorDecoder() {
    this.parser = new RestApiExceptionParserImpl();
  }

  /**
   * Instantiates a new Feign client exception error decoder.
   *
   * @param parser the parser
   */
  @SuppressWarnings("unused")
  public FeignClientExceptionErrorDecoder(
      final RestApiExceptionParser parser) {
    this.parser = parser != null ? parser : new RestApiExceptionParserImpl();
  }

  @Override
  public Exception decode(final String methodKey, final Response response) {

    if (log.isDebugEnabled()) {
      log.debug("msg=[Decoding error at {}]", methodKey);
    }
    final String body = readBody(response);
    final String message = String.format("status %s reading %s", response.status(), methodKey);
    final RestApiException restApiException = parser.parseException(
        body, response.headers());
    if (log.isDebugEnabled() && body != null) {
      log.debug("msg=[Is error formatted as rest api exception? {}]",
          restApiException != null && !body.equals(restApiException.getMessage()));
    }
    final FeignClientException feignClientException = new FeignClientException(
        response.request(),
        response.headers() != null ? Collections.unmodifiableMap(response.headers()) : null,
        response.status(),
        message,
        restApiException);
    final HttpHeaders httpHeaders = buildHttpHeaders(response.headers());
    final Date retryAfter = determineRetryAfter(httpHeaders.getFirst(RETRY_AFTER));
    if (retryAfter != null) {
      return new RetryableException(
          response.status(),
          feignClientException.getMessage(),
          findHttpMethod(response),
          feignClientException,
          retryAfter,
          response.request());
    }
    return feignClientException;
  }

  static String readBody(final Response response) {
    if (response == null || response.body() == null) {
      return null;
    }
    Charset charset = getContentCharset(buildHttpHeaders(response.headers()), StandardCharsets.UTF_8);
    try {
      return Util.toString(response.body().asReader(charset));
    } catch (Exception ignored) {
      return null;
    }
  }

  static Date determineRetryAfter(final String retryAfter) {
    if (retryAfter == null) {
      return null;
    }
    if (retryAfter.matches("^[0-9]+$")) {
      long deltaMillis = SECONDS.toMillis(Long.parseLong(retryAfter));
      return new Date(System.currentTimeMillis() + deltaMillis);
    }
    try {
      return Date.from(OffsetDateTime.parse(retryAfter,
          DateTimeFormatter.RFC_1123_DATE_TIME).toInstant());
    } catch (Exception ignored) {
      return null;
    }
  }

  static HttpMethod findHttpMethod(final Response response) {
    if (response == null || response.request() == null) {
      return null;
    }
    return response.request().httpMethod();
  }

}
