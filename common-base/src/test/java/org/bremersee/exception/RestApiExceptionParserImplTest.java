/*
 * Copyright 2018 the original author or authors.
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

import static org.bremersee.http.converter.ObjectMapperHelper.getJsonMapper;
import static org.bremersee.http.converter.ObjectMapperHelper.getXmlMapper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.TestHelper;
import org.bremersee.exception.model.RestApiException;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * The rest api exception parser impl test.
 *
 * @author Christian Bremer
 */
@Slf4j
public class RestApiExceptionParserImplTest {

  /**
   * Test response is null.
   */
  @Test
  public void testResponseIsNull() {
    final RestApiException actual = new RestApiExceptionParserImpl()
        .parseException(null, null);
    assertNotNull(actual);
    assertEquals(new RestApiException().getMessage(), actual.getMessage());
  }

  /**
   * Test response is json.
   *
   * @throws Exception the exception
   */
  @Test
  public void testResponseIsJson() throws Exception {
    final RestApiException expected = TestHelper.restApiException();
    log.info("Expected: {}", expected);
    final RestApiException actual = new RestApiExceptionParserImpl()
        .parseException(
            getJsonMapper().writeValueAsString(expected),
            buildHttpHeaders(MediaType.APPLICATION_JSON_UTF8, null));
    log.info("Actual:   {}", actual);
    assertNotNull(actual);
    assertEquals(expected, actual);
  }

  /**
   * Test response is xml.
   *
   * @throws Exception the exception
   */
  @Test
  public void testResponseIsXml() throws Exception {
    final RestApiException expected = TestHelper.restApiException();
    log.info("Expected: {}", expected);
    final RestApiException actual = new RestApiExceptionParserImpl()
        .parseException(
            getXmlMapper().writeValueAsString(expected),
            buildHttpHeaders(MediaType.APPLICATION_XML, null));
    log.info("Actual:   {}", actual);
    assertNotNull(actual);
    assertEquals(expected, actual);
  }

  /**
   * Test response is something else.
   *
   * @throws Exception the exception
   */
  @Test
  public void testResponseIsSomethingElse() throws Exception {
    final String response = getJsonMapper()
        .writeValueAsString(TestHelper.otherResponse());
    final RestApiException expected = new RestApiException();
    expected.setMessage(response);
    final RestApiException actual = new RestApiExceptionParserImpl()
        .parseException(
            response,
            buildHttpHeaders(MediaType.APPLICATION_JSON, null));
    expected.setTimestamp(actual.getTimestamp());
    log.info("Expected: {}", expected);
    log.info("Actual:   {}", actual);
    assertNotNull(actual);
    assertEquals(expected, actual);
  }

  /**
   * Test response is empty.
   */
  @Test
  public void testResponseIsEmpty() {
    final String response = "";
    final RestApiException expected = new RestApiException();
    expected.setMessage(RestApiExceptionUtils.NO_MESSAGE_VALUE);
    final RestApiException actual = new RestApiExceptionParserImpl()
        .parseException(
            response,
            buildHttpHeaders(MediaType.APPLICATION_JSON, null));
    expected.setTimestamp(actual.getTimestamp());
    log.info("Expected: {}", expected);
    log.info("Actual:   {}", actual);
    assertNotNull(actual);
    assertEquals(expected, actual);
  }

  /**
   * Test response is empty but headers are present.
   */
  @Test
  public void testResponseIsEmptyButHeadersArePresent() {
    final OffsetDateTime now = OffsetDateTime.now(ZoneId.of("UTC"));
    final String nowStr = now.format(RestApiExceptionUtils.TIMESTAMP_FORMATTER);
    final String response = "";
    final RestApiException expected = new RestApiException();
    expected.setMessage("Something went wrong");
    expected.setId(UUID.randomUUID().toString());
    expected.setErrorCode("TEST:4711");
    expected.setTimestamp(OffsetDateTime.parse(nowStr, RestApiExceptionUtils.TIMESTAMP_FORMATTER));
    expected.setClassName(ServiceException.class.getName());
    final MultiValueMap<String, String> errorHeaders = new LinkedMultiValueMap<>();
    errorHeaders.add(RestApiExceptionUtils.ID_HEADER_NAME, expected.getId());
    errorHeaders.add(RestApiExceptionUtils.TIMESTAMP_HEADER_NAME, nowStr);
    errorHeaders.add(RestApiExceptionUtils.MESSAGE_HEADER_NAME, expected.getMessage());
    errorHeaders.add(RestApiExceptionUtils.CODE_HEADER_NAME, expected.getErrorCode());
    errorHeaders.add(RestApiExceptionUtils.CLASS_HEADER_NAME, expected.getClassName());
    final RestApiException actual = new RestApiExceptionParserImpl()
        .parseException(
            response,
            buildHttpHeaders(MediaType.APPLICATION_JSON, errorHeaders));
    log.info("Expected: {}", expected);
    log.info("Actual:   {}", actual);
    assertNotNull(actual);
    assertEquals(expected, actual);
  }

  private HttpHeaders buildHttpHeaders(
      MediaType contentType,
      MultiValueMap<String, String> errorHeaders) {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(contentType);
    if (errorHeaders != null) {
      httpHeaders.putAll(errorHeaders);
    }
    return httpHeaders;
  }

}
