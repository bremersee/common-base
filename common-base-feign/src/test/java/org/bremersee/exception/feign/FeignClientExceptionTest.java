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

package org.bremersee.exception.feign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;
import org.bremersee.exception.RestApiExceptionUtils;
import org.bremersee.exception.model.RestApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * The feign client exception test.
 *
 * @author Christian Bremer
 */
class FeignClientExceptionTest {

  /**
   * Test with no useful values.
   */
  @Test
  void testWithNoUsefulValues() {
    final FeignClientException exception = new FeignClientException(null, null, 0, null, null);
    assertNull(exception.getRequest());
    assertNotNull(exception.getMultiValueHeaders());
    assertEquals(exception.status(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertEquals(exception.getMessage(), RestApiExceptionUtils.NO_MESSAGE_VALUE);
    assertNull(exception.getRestApiException());
    assertNull(exception.getErrorCode());
  }

  /**
   * Test with some useful values.
   */
  @Test
  void testWithSomeUsefulValues() {
    final RestApiException restApiException = new RestApiException();
    restApiException.setErrorCode("TEST:0001");
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    final FeignClientException exception = new FeignClientException(
        null,
        Collections.unmodifiableMap(headers),
        404,
        "Fatal",
        restApiException);
    assertNull(exception.getRequest());
    assertNotNull(exception.getMultiValueHeaders());
    assertTrue(exception.getMultiValueHeaders().containsKey(HttpHeaders.CONTENT_TYPE));
    assertTrue(exception.getMultiValueHeaders().get(HttpHeaders.CONTENT_TYPE)
        .contains(MediaType.APPLICATION_JSON_VALUE));
    assertEquals(exception.status(), HttpStatus.NOT_FOUND.value());
    assertEquals("Fatal", exception.getMessage());
    assertEquals(restApiException, exception.getRestApiException());
    assertEquals("TEST:0001", exception.getErrorCode());

    final Map<String, String> actualHeaders = exception.getHeaders();
    assertNotNull(actualHeaders);
    assertEquals(MediaType.APPLICATION_JSON_VALUE, actualHeaders.get(HttpHeaders.CONTENT_TYPE));
  }

}
