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

package org.bremersee.web.reactive.function.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bremersee.exception.MessageExceptionParser;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.test.StepVerifier;

/**
 * The message aware web client error decoder test.
 *
 * @author Christian Bremer
 */
class MessageAwareWebClientErrorDecoderTest {

  /**
   * Tests Creation.
   */
  @Test
  void create() {
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
  void buildException() {
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