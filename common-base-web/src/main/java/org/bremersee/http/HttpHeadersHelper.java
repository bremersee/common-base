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

package org.bremersee.http;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

/**
 * Helper to create http headers.
 *
 * @author Christian Bremer
 */
public abstract class HttpHeadersHelper {

  private HttpHeadersHelper() {
  }

  /**
   * Build http headers.
   *
   * @param headers the headers
   * @return the http headers
   */
  public static HttpHeaders buildHttpHeaders(
      final Map<String, ? extends Collection<String>> headers) {

    if (headers instanceof HttpHeaders) {
      return (HttpHeaders) headers;
    }
    if (headers instanceof MultiValueMap) {
      //noinspection unchecked,rawtypes
      return new HttpHeaders((MultiValueMap) headers);
    }
    final HttpHeaders httpHeaders = new HttpHeaders();
    if (headers != null) {
      headers.forEach(
          (BiConsumer<String, Collection<String>>) (key, values)
              -> httpHeaders.addAll(key, values != null
              ? new ArrayList<>(values)
              : Collections.emptyList()));
    }
    return httpHeaders;
  }

  /**
   * Gets content charset.
   *
   * @param headers the headers
   * @param defaultCharset the default charset
   * @return the content charset or the default charset if no content charset is specified
   */
  public static Charset getContentCharset(HttpHeaders headers, Charset defaultCharset) {
    return Optional.ofNullable(headers)
        .map(h -> h.get(HttpHeaders.CONTENT_TYPE))
        .filter(l -> l.size() > 1)
        .map(l -> l.subList(1, l.size()))
        .flatMap(l -> l.stream()
            .map(value -> Pattern.compile(".*charset=([^\\s|^;]+).*").matcher(value))
            .filter(Matcher::lookingAt)
            .map(matcher -> matcher.group(1))
            .filter(Charset::isSupported)
            .map(Charset::forName)
            .findFirst())
        .orElse(defaultCharset);
  }

}
