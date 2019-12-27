package org.bremersee.web.reactive.function.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.bremersee.exception.MessageExceptionParser;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.test.StepVerifier;

/**
 * The message aware web client error decoder test.
 *
 * @author Christian Bremer
 */
public class MessageAwareWebClientErrorDecoderTest {

  /**
   * Tests Creation.
   */
  @Test
  public void create() {
    MessageAwareWebClientErrorDecoder decoder = new MessageAwareWebClientErrorDecoder();
    assertTrue(decoder.getParser() instanceof MessageExceptionParser);
    assertNull(decoder.getErrorCode());

    decoder = new MessageAwareWebClientErrorDecoder("FOO");
    assertTrue(decoder.getParser() instanceof MessageExceptionParser);
    assertEquals("FOO", decoder.getErrorCode());

    decoder = new MessageAwareWebClientErrorDecoder(new MessageExceptionParser(), "FOO");
    assertTrue(decoder.getParser() instanceof MessageExceptionParser);
    assertEquals("FOO", decoder.getErrorCode());
  }

  /**
   * Tests build exception.
   */
  @Test
  public void buildException() {
    MessageAwareWebClientErrorDecoder decoder = new MessageAwareWebClientErrorDecoder("FOO");

    ClientResponse clientResponse = ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
        .header("x-foo", "bar")
        .body("Error message")
        .build();

    StepVerifier
        .create(decoder.apply(clientResponse))
        .assertNext(webClientException -> {
          assertEquals("FOO", webClientException.getErrorCode());
          assertTrue(webClientException.getHeaders().containsKey("x-foo"));
          assertNotNull(webClientException.getRestApiException());
          assertEquals("Error message",
              webClientException.getRestApiException().getMessage());
        })
        .expectNextCount(0)
        .verifyComplete();
  }
}