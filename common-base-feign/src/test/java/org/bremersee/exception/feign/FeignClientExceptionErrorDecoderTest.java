/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.exception.feign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import feign.Request;
import feign.Request.HttpMethod;
import feign.Response;
import feign.Response.Body;
import feign.RetryableException;
import feign.Util;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.bremersee.exception.RestApiExceptionParserImpl;
import org.bremersee.exception.ServiceException;
import org.bremersee.exception.model.RestApiException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * The feign client exception error decoder test.
 *
 * @author Christian Bremer
 */
class FeignClientExceptionErrorDecoderTest {

  /**
   * Test decode json.
   *
   * @throws Exception the exception
   */
  @Test
  void testDecodeJson() throws Exception {
    final FeignClientExceptionErrorDecoder decoder = new FeignClientExceptionErrorDecoder();
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    final RestApiException expected = restApiException();
    @SuppressWarnings({"unchecked", "rawtypes"}) final Response response = Response
        .builder()
        .request(Request
            .create(
                HttpMethod.GET,
                "https://example.org",
                new HashMap<>(),
                null,
                StandardCharsets.UTF_8,
                null))
        .body(getJsonMapper().writeValueAsBytes(expected))
        .headers((Map) headers)
        .reason("Something bad")
        .status(500)
        .build();
    final Exception actual = decoder.decode("getSomething", response);
    assertNotNull(actual);
    assertTrue(actual instanceof FeignClientException);
    assertEquals(500, ((FeignClientException) actual).status());
    assertEquals(expected, ((FeignClientException) actual).getRestApiException());
  }

  /**
   * Test decode xml.
   *
   * @throws Exception the exception
   */
  @Test
  void testDecodeXml() throws Exception {
    final FeignClientExceptionErrorDecoder decoder = new FeignClientExceptionErrorDecoder(
        new RestApiExceptionParserImpl());
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
    final RestApiException expected = restApiException();
    @SuppressWarnings({"unchecked", "rawtypes"}) final Response response = Response
        .builder()
        .request(Request
            .create(
                HttpMethod.GET,
                "https://example.org",
                new HashMap<>(),
                null,
                StandardCharsets.UTF_8,
                null))
        .body(getXmlMapper().writeValueAsBytes(expected))
        .headers((Map) headers)
        .reason("Nothing found")
        .status(404)
        .build();
    final Exception actual = decoder.decode("getSomethingThatNotExists", response);
    assertNotNull(actual);
    assertTrue(actual instanceof FeignClientException);
    assertEquals(404, ((FeignClientException) actual).status());
    assertEquals(expected, ((FeignClientException) actual).getRestApiException());
  }

  /**
   * Test decode something else.
   *
   * @throws Exception the exception
   */
  @Test
  void testDecodeSomethingElse() throws Exception {
    final FeignClientExceptionErrorDecoder decoder = new FeignClientExceptionErrorDecoder(null);
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
    final String body = getXmlMapper().writeValueAsString(otherResponse());
    @SuppressWarnings({"unchecked", "rawtypes"}) final Response response = Response
        .builder()
        .request(Request
            .create(
                HttpMethod.GET,
                "https://example.org",
                new HashMap<>(),
                null,
                StandardCharsets.UTF_8,
                null))
        .body(body.getBytes(StandardCharsets.UTF_8))
        .headers((Map) headers)
        .reason("Something bad")
        .status(500)
        .build();
    final Exception actual = decoder.decode("getSomething", response);
    assertNotNull(actual);
    assertTrue(actual instanceof FeignClientException);
    assertEquals(500, ((FeignClientException) actual).status());
    assertNotNull(((FeignClientException) actual).getRestApiException());
    //noinspection ConstantConditions
    assertEquals(body, ((FeignClientException) actual).getRestApiException().getMessage());
  }

  /**
   * Test decode empty response.
   */
  @Test
  void testDecodeEmptyResponse() {
    final FeignClientExceptionErrorDecoder decoder = new FeignClientExceptionErrorDecoder(null);
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
    final String body = "";
    @SuppressWarnings({"unchecked", "rawtypes"}) final Response response = Response
        .builder()
        .request(Request
            .create(
                HttpMethod.GET,
                "https://example.org",
                new HashMap<>(),
                null,
                StandardCharsets.UTF_8,
                null))
        .body(body.getBytes(StandardCharsets.UTF_8))
        .headers((Map) headers)
        .reason("Something bad")
        .status(500)
        .build();
    final Exception actual = decoder.decode("getNothing", response);
    assertNotNull(actual);
    assertTrue(actual instanceof FeignClientException);
    assertEquals(500, ((FeignClientException) actual).status());
  }

  /**
   * Test decode retryable exception.
   *
   * @throws Exception the exception
   */
  @Test
  void testDecodeRetryableException() throws Exception {
    final FeignClientExceptionErrorDecoder decoder = new FeignClientExceptionErrorDecoder();
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    headers.add(Util.RETRY_AFTER, "30");
    final RestApiException restException = restApiException();
    @SuppressWarnings({"unchecked", "rawtypes"}) final Response response = Response
        .builder()
        .request(Request
            .create(
                HttpMethod.GET,
                "https://example.org",
                new HashMap<>(),
                null,
                StandardCharsets.UTF_8,
                null))
        .body(getJsonMapper().writeValueAsBytes(restException))
        .headers((Map) headers)
        .reason("Something went wrong.")
        .status(500)
        .build();
    final Exception actual = decoder.decode("theMethodKey", response);
    assertNotNull(actual);
    assertTrue(actual instanceof RetryableException);

    RetryableException retryableException = (RetryableException) actual;
    assertNotNull(retryableException.method());
    assertEquals(HttpMethod.GET, retryableException.method());

    assertNotNull(retryableException.retryAfter());
    assertTrue(retryableException.retryAfter()
        .before(new Date(System.currentTimeMillis() + 30001)));

    assertEquals(500, retryableException.status());

    assertNotNull(retryableException.getMessage());
    assertEquals("status 500 reading theMethodKey", retryableException.getMessage());

    // not set in our implementation: retryableException.contentUTF8()
  }

  /**
   * Read body.
   */
  @Test
  void readBody() {
    assertNull(FeignClientExceptionErrorDecoder.readBody(null));
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
    @SuppressWarnings({"unchecked", "rawtypes"}) final Response response = Response
        .builder()
        .request(Request
            .create(
                HttpMethod.GET,
                "https://example.org",
                new HashMap<>(),
                null,
                StandardCharsets.UTF_8,
                null))
        .body("Not found.".getBytes())
        .headers((Map) headers)
        .reason("Nothing found")
        .status(404)
        .build();
    assertEquals("Not found.", FeignClientExceptionErrorDecoder.readBody(response));
  }

  /**
   * Read body failed.
   *
   * @throws IOException the io exception
   */
  @Test
  void readBodyFailed() throws IOException {
    assertNull(FeignClientExceptionErrorDecoder.readBody(null));
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
    final Body body = Mockito.mock(Body.class);
    Mockito.when(body.asReader(any(Charset.class))).thenThrow(new IOException("Can happen"));
    @SuppressWarnings({"unchecked", "rawtypes"}) final Response response = Response
        .builder()
        .request(Request
            .create(
                HttpMethod.GET,
                "https://example.org",
                new HashMap<>(),
                null,
                StandardCharsets.UTF_8,
                null))
        .body(body)
        .headers((Map) headers)
        .reason("Nothing found")
        .status(404)
        .build();
    assertNull(FeignClientExceptionErrorDecoder.readBody(response));
  }

  /**
   * Determine retry after.
   */
  @Test
  void determineRetryAfter() {
    assertNull(FeignClientExceptionErrorDecoder.determineRetryAfter(null));
    Date actual = FeignClientExceptionErrorDecoder.determineRetryAfter("30");
    assertNotNull(actual);
    assertTrue(actual.before(new Date(System.currentTimeMillis() + 30001)));
    Date expected = Date.from(OffsetDateTime.parse("2007-12-24T18:21Z").toInstant());
    String value = OffsetDateTime.ofInstant(expected.toInstant(), ZoneOffset.UTC)
        .format(DateTimeFormatter.RFC_1123_DATE_TIME);
    actual = FeignClientExceptionErrorDecoder.determineRetryAfter(value);
    assertNotNull(actual);
    assertEquals(expected.getTime(), actual.getTime());
  }

  /**
   * Determine retry after failed.
   */
  @Test
  void determineRetryAfterFailed() {
    assertNull(FeignClientExceptionErrorDecoder.determineRetryAfter(null));
    Date actual = FeignClientExceptionErrorDecoder.determineRetryAfter("30");
    assertNotNull(actual);
    assertTrue(actual.before(new Date(System.currentTimeMillis() + 30001)));
    Date expected = Date.from(OffsetDateTime.parse("2007-12-24T18:21Z").toInstant());
    String value = OffsetDateTime.ofInstant(expected.toInstant(), ZoneOffset.UTC)
        .format(DateTimeFormatter.ISO_DATE_TIME);
    actual = FeignClientExceptionErrorDecoder.determineRetryAfter(value);
    assertNull(actual);
  }

  /**
   * Find http method.
   */
  @Test
  void findHttpMethod() {
    assertNull(FeignClientExceptionErrorDecoder.findHttpMethod(null));
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
    @SuppressWarnings({"unchecked", "rawtypes"}) final Response response = Response
        .builder()
        .request(Request
            .create(
                HttpMethod.GET,
                "https://example.org",
                new HashMap<>(),
                null,
                StandardCharsets.UTF_8,
                null))
        .body("Not found.".getBytes())
        .headers((Map) headers)
        .reason("Nothing found")
        .status(404)
        .build();
    assertEquals(HttpMethod.GET, FeignClientExceptionErrorDecoder.findHttpMethod(response));
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
   * Returns xml mapper.
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
