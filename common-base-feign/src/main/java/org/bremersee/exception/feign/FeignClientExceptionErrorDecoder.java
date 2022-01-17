/*
 * Copyright 2019-2022 the original author or authors.
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
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionParser;
import org.bremersee.exception.RestApiExceptionParserImpl;
import org.bremersee.exception.model.RestApiException;
import org.springframework.http.HttpHeaders;

/**
 * This error decoder produces either a {@link FeignClientException} or a {@link
 * feign.RetryableException}.
 *
 * @author Christian Bremer
 */
@Slf4j
public class FeignClientExceptionErrorDecoder implements ErrorDecoder {

  private final RestApiExceptionParser parser;

  /**
   * Instantiates a new feign client exception error decoder.
   */
  public FeignClientExceptionErrorDecoder() {
    this(null);
  }

  /**
   * Instantiates a new Feign client exception error decoder.
   *
   * @param parser the parser
   */
  public FeignClientExceptionErrorDecoder(RestApiExceptionParser parser) {
    this.parser = parser != null ? parser : new RestApiExceptionParserImpl();
  }

  @Override
  public Exception decode(final String methodKey, final Response response) {

    if (log.isDebugEnabled()) {
      log.debug("Decoding feign exception at {}", methodKey);
    }
    final String body = readBody(response);
    final String message = String.format("status %s reading %s", response.status(), methodKey);
    final Map<String, Collection<String>> headers = Objects
        .requireNonNullElseGet(response.headers(), Map::of);
    final RestApiException restApiException = parser.parseException(
        body, response.headers());
    if (log.isDebugEnabled() && body != null) {
      log.debug("Is error formatted as rest api exception? {}",
          restApiException != null && !body.equals(restApiException.getMessage()));
    }
    FeignClientException feignClientException = new FeignClientException(
        response.status(),
        message,
        response.request(),
        headers,
        Objects.isNull(body) ? new byte[0] : body.getBytes(StandardCharsets.UTF_8),
        restApiException);
    HttpHeaders httpHeaders = buildHttpHeaders(headers);
    Date retryAfter = determineRetryAfter(httpHeaders.getFirst(RETRY_AFTER));
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

  static String readBody(Response response) {
    if (response == null || response.body() == null) {
      return null;
    }
    Charset charset = getContentCharset(buildHttpHeaders(response.headers()),
        StandardCharsets.UTF_8);
    try {
      return Util.toString(response.body().asReader(charset));
    } catch (Exception ignored) {
      return null;
    }
  }

  static Date determineRetryAfter(String retryAfter) {
    if (retryAfter == null) {
      return null;
    }
    try {
      if (retryAfter.matches("^[0-9]+\\.?0*$")) {
        String parsedRetryAfter = retryAfter.replaceAll("\\.0*$", "");
        long deltaMillis = SECONDS.toMillis(Long.parseLong(parsedRetryAfter));
        return new Date(System.currentTimeMillis() + deltaMillis);
      }
      return Date.from(OffsetDateTime.parse(retryAfter,
          DateTimeFormatter.RFC_1123_DATE_TIME).toInstant());
    } catch (Exception e) {
      log.warn("Parsing retry after date for feign's RetryableException failed.", e);
      return null;
    }
  }

  static HttpMethod findHttpMethod(Response response) {
    if (response == null || response.request() == null) {
      return null;
    }
    return response.request().httpMethod();
  }

}
