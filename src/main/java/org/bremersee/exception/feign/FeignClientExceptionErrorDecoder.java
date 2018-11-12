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

import static feign.Util.RETRY_AFTER;
import static java.lang.String.format;
import static java.util.Locale.US;
import static java.util.concurrent.TimeUnit.SECONDS;

import feign.Response;
import feign.RetryableException;
import feign.Util;
import feign.codec.ErrorDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionParser;
import org.bremersee.exception.RestApiExceptionParserImpl;
import org.bremersee.exception.model.RestApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * @author Christian Bremer
 */
@Slf4j
public class FeignClientExceptionErrorDecoder implements ErrorDecoder {

  private final DateFormat rfc822Format = new SimpleDateFormat(
      "EEE, dd MMM yyyy HH:mm:ss 'GMT'", US);

  private final RestApiExceptionParser parser;

  public FeignClientExceptionErrorDecoder() {
    this.parser = new RestApiExceptionParserImpl();
  }

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
    final String message = format("status %s reading %s", response.status(), methodKey);
    final String contentType = firstOrDefault(response.headers(), HttpHeaders.CONTENT_TYPE,
        MediaType.TEXT_PLAIN_VALUE);
    final RestApiException restApiException = parser.parseRestApiException(body, contentType);
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
    final Date retryAfter = determineRetryAfter(
        firstOrDefault(response.headers(), RETRY_AFTER, null));
    if (retryAfter != null) {
      return new RetryableException(feignClientException.getMessage(), feignClientException,
          retryAfter);
    }
    return feignClientException;
  }

  private String readBody(Response response) {
    if (response.body() == null) {
      return null;
    }
    try {
      return Util.toString(response.body().asReader());
    } catch (Exception ignored) {
      return null;
    }
  }

  private <T> T firstOrDefault(Map<String, Collection<T>> map, String key, T defaultValue) {
    if (map.containsKey(key) && !map.get(key).isEmpty()) {
      return map.get(key).iterator().next();
    }
    return defaultValue;
  }

  private Date determineRetryAfter(String retryAfter) {
    if (retryAfter == null) {
      return null;
    }
    if (retryAfter.matches("^[0-9]+$")) {
      long deltaMillis = SECONDS.toMillis(Long.parseLong(retryAfter));
      return new Date(System.currentTimeMillis() + deltaMillis);
    }
    synchronized (rfc822Format) {
      try {
        return rfc822Format.parse(retryAfter);
      } catch (ParseException ignored) {
        return null;
      }
    }
  }
}
