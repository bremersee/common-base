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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.bremersee.exception.ServiceException;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.http.MediaTypeHelper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * The default web client error decoder test.
 *
 * @author Christian Bremer
 */
class DefaultWebClientErrorDecoderTest {

  private static final DefaultWebClientErrorDecoder decoder = new DefaultWebClientErrorDecoder();

  /**
   * Test decode json.
   *
   * @throws Exception the exception
   */
  @Test
  void testDecodeJson() throws Exception {
    testDecode(MediaType.APPLICATION_JSON_VALUE);
  }

  /**
   * Test decode xml.
   *
   * @throws Exception the exception
   */
  @Test
  void testDecodeXml() throws Exception {
    testDecode(MediaType.APPLICATION_XML_VALUE);
  }

  private void testDecode(final String contentType) throws Exception {

    final HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    final RestApiException expected = restApiException();

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
  void testDecodeSomethingElse() throws Exception {

    final HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    final String expected = getJsonMapper().writeValueAsString(otherResponse());

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
  void testDecodeEmptyResponse() {
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

  /**
   * Returns json mapper.
   *
   * @return the json mapper
   */
  private static ObjectMapper getJsonMapper() {
    return Jackson2ObjectMapperBuilder.json().build();
  }

  /**
   * Returns xml mapper
   *
   * @return the xml mapper
   */
  private static XmlMapper getXmlMapper() {
    return Jackson2ObjectMapperBuilder.xml().createXmlMapper(true).build();
  }

  /**
   * Returns a rest api exception.
   *
   * @return the rest api exception
   */
  private static RestApiException restApiException() {
    RestApiException restApiException = new RestApiException();
    restApiException.setApplication("test");
    restApiException.setClassName(ServiceException.class.getName());
    restApiException.setErrorCode("TEST:4711");
    restApiException.setErrorCodeInherited(false);
    restApiException.setId(UUID.randomUUID().toString());
    restApiException.setMessage("Something failed.");
    restApiException.setPath("/api/something");
    restApiException.setTimestamp(OffsetDateTime.now(ZoneId.of("UTC")));
    return restApiException;
  }

  /**
   * Returns an other response.
   *
   * @return the map
   */
  private static Map<String, Object> otherResponse() {
    final Map<String, Object> map = new LinkedHashMap<>();
    map.put("timestamp", OffsetDateTime.now(ZoneId.of("UTC")));
    map.put("status", 404);
    map.put("reason", "Not found");
    return map;
  }

}