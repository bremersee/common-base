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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * The http headers helper test.
 *
 * @author Christian Bremer
 */
class HttpHeadersHelperTest {

  /**
   * Build http headers.
   */
  @Test
  void buildHttpHeaders() {
    assertNotNull(HttpHeadersHelper.buildHttpHeaders(null));
    assertTrue(HttpHeadersHelper.buildHttpHeaders(null).isEmpty());

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    assertEquals(httpHeaders, HttpHeadersHelper.buildHttpHeaders(httpHeaders));

    MultiValueMap<String, String> multiMap = new LinkedMultiValueMap<>();
    multiMap.add("Accept", MediaType.TEXT_PLAIN_VALUE);
    httpHeaders = HttpHeadersHelper.buildHttpHeaders(multiMap);
    assertNotNull(httpHeaders);
    assertEquals(multiMap, httpHeaders);

    Map<String, List<String>> map = new HashMap<>();
    map.put("Accept", Collections.singletonList(MediaType.TEXT_PLAIN_VALUE));
    httpHeaders = HttpHeadersHelper.buildHttpHeaders(map);
    assertNotNull(httpHeaders);
    assertEquals(MediaType.TEXT_PLAIN_VALUE, httpHeaders.getFirst("Accept"));
  }
}