package org.bremersee.web.reactive.function.client;

import static org.bremersee.http.converter.ObjectMapperHelper.getJsonMapper;
import static org.bremersee.http.converter.ObjectMapperHelper.getXmlMapper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bremersee.TestHelper;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.http.MediaTypeHelper;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * The default web client error decoder test.
 *
 * @author Christian Bremer
 */
public class DefaultWebClientErrorDecoderTest {

  private static final DefaultWebClientErrorDecoder decoder = new DefaultWebClientErrorDecoder();

  /**
   * Test decode json.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDecodeJson() throws Exception {
    testDecode(MediaType.APPLICATION_JSON_VALUE);
  }

  /**
   * Test decode xml.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDecodeXml() throws Exception {
    testDecode(MediaType.APPLICATION_XML_VALUE);
  }

  private void testDecode(final String contentType) throws Exception {

    final HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    final RestApiException expected = TestHelper.restApiException();

    final HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, contentType);

    ClientResponse.Headers internalHeaders = mock(ClientResponse.Headers.class);
    when(internalHeaders.asHttpHeaders()).thenReturn(headers);

    ClientResponse clientResponse = mock(ClientResponse.class);
    when(clientResponse.statusCode()).thenReturn(httpStatus);
    when(clientResponse.headers()).thenReturn(internalHeaders);
    if (MediaTypeHelper.canContentTypeBeJson(contentType)) {
      when(
          clientResponse.bodyToMono(String.class))
          .thenReturn(Mono.just(getJsonMapper().writeValueAsString(expected)));
    } else if (MediaTypeHelper.canContentTypeBeXml(contentType)) {
      when(
          clientResponse.bodyToMono(String.class))
          .thenReturn(Mono.just(getXmlMapper().writeValueAsString(expected)));
    } else {
      throw new Exception("Content type is not supported in this test.");
    }

    StepVerifier
        .create(decoder.apply(clientResponse))
        .assertNext(throwable -> {
          assertNotNull(throwable);
          assertEquals(httpStatus, throwable.getStatus());
          assertEquals(expected, throwable.getRestApiException());
        })
        .expectNextCount(0)
        .verifyComplete();
  }

  /**
   * Test decode something else.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDecodeSomethingElse() throws Exception {

    final HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    final String expected = getJsonMapper().writeValueAsString(TestHelper.otherResponse());

    final HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

    ClientResponse.Headers internalHeaders = mock(ClientResponse.Headers.class);
    when(internalHeaders.asHttpHeaders()).thenReturn(headers);

    ClientResponse clientResponse = mock(ClientResponse.class);
    when(clientResponse.statusCode()).thenReturn(httpStatus);
    when(clientResponse.headers()).thenReturn(internalHeaders);
    when(
        clientResponse.bodyToMono(String.class))
        .thenReturn(Mono.just(expected));

    StepVerifier
        .create(decoder.apply(clientResponse))
        .assertNext(throwable -> {
          assertNotNull(throwable);
          assertEquals(httpStatus, throwable.getStatus());
          assertNotNull(throwable.getRestApiException());
          assertEquals(expected, throwable.getRestApiException().getMessage());
        })
        .expectNextCount(0)
        .verifyComplete();
  }

  /**
   * Test decode empty response.
   */
  @Test
  public void testDecodeEmptyResponse() {
    final HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    final HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

    ClientResponse.Headers internalHeaders = mock(ClientResponse.Headers.class);
    when(internalHeaders.asHttpHeaders()).thenReturn(headers);

    ClientResponse clientResponse = mock(ClientResponse.class);
    when(clientResponse.statusCode()).thenReturn(httpStatus);
    when(clientResponse.headers()).thenReturn(internalHeaders);
    when(
        clientResponse.bodyToMono(String.class))
        .thenReturn(Mono.empty());

    StepVerifier
        .create(decoder.apply(clientResponse))
        .assertNext(throwable -> {
          assertNotNull(throwable);
          assertEquals(httpStatus, throwable.getStatus());
          assertNotNull(throwable.getRestApiException());
        })
        .expectNextCount(0)
        .verifyComplete();
  }

}