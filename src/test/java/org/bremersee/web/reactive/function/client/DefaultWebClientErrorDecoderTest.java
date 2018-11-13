package org.bremersee.web.reactive.function.client;

import static org.bremersee.http.converter.ObjectMapperHelper.getJsonMapper;
import static org.bremersee.http.converter.ObjectMapperHelper.getXmlMapper;

import org.bremersee.TestHelper;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.http.MediaTypeHelper;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

/**
 * @author Christian Bremer
 */
public class DefaultWebClientErrorDecoderTest {

  private static final DefaultWebClientErrorDecoder decoder = new DefaultWebClientErrorDecoder();

  @Test
  public void testDecodeJson() throws Exception {
    testDecode(MediaType.APPLICATION_JSON_VALUE);
  }

  @Test
  public void testDecodeXml() throws Exception {
    testDecode(MediaType.APPLICATION_XML_VALUE);
  }

  private void testDecode(final String contentType) throws Exception {

    final HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    final RestApiException expected = TestHelper.restApiException();

    final HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, contentType);

    ClientResponse.Headers internalHeaders = Mockito.mock(ClientResponse.Headers.class);
    Mockito.when(internalHeaders.asHttpHeaders()).thenReturn(headers);

    ClientResponse clientResponse = Mockito.mock(ClientResponse.class);
    Mockito.when(clientResponse.statusCode()).thenReturn(httpStatus);
    Mockito.when(clientResponse.headers()).thenReturn(internalHeaders);
    if (MediaTypeHelper.canContentTypeBeJson(contentType)) {
      Mockito.when(
          clientResponse.bodyToMono(String.class))
          .thenReturn(Mono.just(getJsonMapper().writeValueAsString(expected)));
    } else if (MediaTypeHelper.canContentTypeBeXml(contentType)) {
      Mockito.when(
          clientResponse.bodyToMono(String.class))
          .thenReturn(Mono.just(getXmlMapper().writeValueAsString(expected)));
    } else {
      throw new Exception("Content type is not supported in this test.");
    }

    Mono<? extends Throwable> throwableMono = decoder.apply(clientResponse);
    Assert.assertNotNull(throwableMono);
    Throwable throwable = throwableMono.block();
    Assert.assertNotNull(throwable);
    Assert.assertTrue(throwable instanceof WebClientException);
    Assert.assertEquals(httpStatus, ((WebClientException) throwable).getStatus());
    Assert.assertEquals(expected, ((WebClientException) throwable).getRestApiException());
  }

  @Test
  public void testDecodeSomethingElse() throws Exception {

    final HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    final String expected = getJsonMapper().writeValueAsString(TestHelper.otherResponse());

    final HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

    ClientResponse.Headers internalHeaders = Mockito.mock(ClientResponse.Headers.class);
    Mockito.when(internalHeaders.asHttpHeaders()).thenReturn(headers);

    ClientResponse clientResponse = Mockito.mock(ClientResponse.class);
    Mockito.when(clientResponse.statusCode()).thenReturn(httpStatus);
    Mockito.when(clientResponse.headers()).thenReturn(internalHeaders);
    Mockito.when(
        clientResponse.bodyToMono(String.class))
        .thenReturn(Mono.just(expected));

    Mono<? extends Throwable> throwableMono = decoder.apply(clientResponse);
    Assert.assertNotNull(throwableMono);
    Throwable throwable = throwableMono.block();
    Assert.assertNotNull(throwable);
    Assert.assertTrue(throwable instanceof WebClientException);
    Assert.assertEquals(httpStatus, ((WebClientException) throwable).getStatus());
    Assert.assertNotNull(((WebClientException) throwable).getRestApiException());
    //noinspection ConstantConditions
    Assert.assertEquals(expected,
        ((WebClientException) throwable).getRestApiException().getMessage());
  }

  @Test
  public void testDecodeEmptyResponse() {
    final HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    final HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

    ClientResponse.Headers internalHeaders = Mockito.mock(ClientResponse.Headers.class);
    Mockito.when(internalHeaders.asHttpHeaders()).thenReturn(headers);

    ClientResponse clientResponse = Mockito.mock(ClientResponse.class);
    Mockito.when(clientResponse.statusCode()).thenReturn(httpStatus);
    Mockito.when(clientResponse.headers()).thenReturn(internalHeaders);
    Mockito.when(
        clientResponse.bodyToMono(String.class))
        .thenReturn(Mono.empty());

    Mono<? extends Throwable> throwableMono = decoder.apply(clientResponse);
    Assert.assertNotNull(throwableMono);
    Throwable throwable = throwableMono.block();
    Assert.assertNotNull(throwable);
    Assert.assertTrue(throwable instanceof WebClientException);
    Assert.assertEquals(httpStatus, ((WebClientException) throwable).getStatus());
    // TODO wollen wir hier trotzdem ein cause haben? ja
    //Assert.assertNull(((WebClientException) throwable).getRestApiException());
  }

}