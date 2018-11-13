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

package org.bremersee.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

/**
 * @author Christian Bremer
 */
public abstract class HttpHeadersHelper {

  private HttpHeadersHelper() {
  }

  public static HttpHeaders buildHttpHeaders(Map<String, ? extends Collection<String>> headers) {

    if (headers instanceof HttpHeaders) {
      return (HttpHeaders) headers;
    }
    if (headers instanceof MultiValueMap) {
      //noinspection unchecked
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
}
