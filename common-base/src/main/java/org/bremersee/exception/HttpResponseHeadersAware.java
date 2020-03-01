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

package org.bremersee.exception;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.validation.constraints.NotNull;

/**
 * Marker interface to get the response headers.
 *
 * @author Christian Bremer
 */
public interface HttpResponseHeadersAware {

  /**
   * Create headers map.
   *
   * @param headers the headers
   * @return the map
   */
  static Map<String, String> createHeaders(Map<String, ? extends Collection<String>> headers) {
    if (headers == null || headers.isEmpty()) {
      return Collections.emptyMap();
    }
    final Map<String, String> map = new LinkedHashMap<>();
    for (Entry<String, ? extends Collection<String>> entry : headers.entrySet()) {
      if (entry.getValue() != null && !entry.getValue().isEmpty()) {
        map.put(entry.getKey(), entry.getValue().iterator().next());
      } else {
        map.put(entry.getKey(), "");
      }
    }
    return map;
  }

  /**
   * Create multi value headers map.
   *
   * @param headers the headers
   * @return the map
   */
  static Map<String, ? extends Collection<String>> createMultiValueHeaders(
      Map<String, String> headers) {
    if (headers == null || headers.isEmpty()) {
      return Collections.emptyMap();
    }
    final Map<String, List<String>> map = new LinkedHashMap<>();
    for (Entry<String, String> entry : headers.entrySet()) {
      if (entry.getValue() != null) {
        map.put(entry.getKey(), Collections.singletonList(entry.getValue()));
      }
    }
    return map;
  }

  /**
   * Gets the response headers.
   *
   * @return the response headers
   */
  @NotNull
  Map<String, String> getHeaders();

  /**
   * Gets the response headers as multi value map.
   *
   * @return the response headers
   */
  @NotNull
  Map<String, ? extends Collection<String>> getMultiValueHeaders();

}
