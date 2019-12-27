package org.bremersee.exception;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * The http response headers aware test.
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