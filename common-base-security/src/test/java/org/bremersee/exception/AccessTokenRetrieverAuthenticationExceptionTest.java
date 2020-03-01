package org.bremersee.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * The type Access token retriever authentication exception test.
 *
 * @author Christian Bremer
 */
class AccessTokenRetrieverAuthenticationExceptionTest {

  /**
   * Status.
   */
  @Test
  void status() {
    AccessTokenRetrieverAuthenticationException exception
        = new AccessTokenRetrieverAuthenticationException(null, null);
    assertEquals(RestApiExceptionUtils.NO_MESSAGE_VALUE, exception.getMessage());
    assertEquals(HttpStatus.UNAUTHORIZED.value(), exception.status());

    exception = new AccessTokenRetrieverAuthenticationException(HttpStatus.FORBIDDEN, null);
    assertEquals("Server returned status code 403 (Forbidden)", exception.getMessage());
    assertEquals(HttpStatus.FORBIDDEN.value(), exception.status());

    exception = new AccessTokenRetrieverAuthenticationException(null, "A message");
    assertEquals("A message", exception.getMessage());
    assertEquals(HttpStatus.UNAUTHORIZED.value(), exception.status());

    exception = new AccessTokenRetrieverAuthenticationException(HttpStatus.FORBIDDEN, "A message");
    assertEquals("Server returned status code 403 (Forbidden): A message", exception.getMessage());
    assertEquals(HttpStatus.FORBIDDEN.value(), exception.status());
  }

}