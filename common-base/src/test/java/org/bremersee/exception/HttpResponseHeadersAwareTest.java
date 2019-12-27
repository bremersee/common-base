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

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * The http response headers aware test.
 *
 * @author Christian Bremer
 */
public class HttpResponseHeadersAwareTest {

  /**
   * Create headers.
   */
  @Test
  public void createHeaders() {
    Map<String, List<String>> source = new LinkedHashMap<>();
    source.put("A", List.of("ValueA"));
    source.put("B", List.of("ValueB1", "ValueB2"));
    source.put("C", List.of());
    Map<String, String> destination = HttpResponseHeadersAware.createHeaders(source);
    assertEquals("ValueA", destination.get("A"));
    assertEquals("ValueB1", destination.get("B"));
    assertEquals("", destination.get("C"));
  }

  /**
   * Create multi value headers.
   */
  @Test
  public void createMultiValueHeaders() {
    Map<String, String> source = new LinkedHashMap<>();
    source.put("A", "ValueA");
    source.put("B", "ValueB1");
    source.put("C", "");
    Map<String, ? extends Collection<String>> destination = HttpResponseHeadersAware
        .createMultiValueHeaders(source);
    assertEquals("ValueA", destination.get("A").iterator().next());
    assertEquals("ValueB1", destination.get("B").iterator().next());
    assertEquals("", destination.get("C").iterator().next());
  }
}