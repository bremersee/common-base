package org.bremersee.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.bremersee.exception.model.RestApiException;
import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * The http client exception test.
 *
 * @author Christian Bremer
 */
public class HttpClientExceptionTest {

  /**
   * Tests creation.
   */
  @Test
  public void create() {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add("foo", "bar");
    RestApiException restApiException = new RestApiException();
    HttpClientException exception = new HttpClientException(
        404,
        "Not found",
        headers,
        restApiException);
    assertEquals(404, exception.status());
    assertEquals("Not found", exception.getMessage());
    assertTrue(exception.getMultiValueHeaders().containsKey("foo"));
    assertEquals(restApiException, exception.getRestApiException());
    assertNotNull(exception.toString());
    assertEquals(exception, new HttpClientException(
        404,
        "Not found",
        headers,
        restApiException));
  }
}