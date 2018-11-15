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

package org.bremersee.exception.feign;

import java.util.Collections;
import org.bremersee.exception.RestApiExceptionUtils;
import org.bremersee.exception.model.RestApiException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Christian Bremer
 */
public class FeignClientExceptionTest {

  @Test
  public void testWithNoUsefulValues() {
    final FeignClientException exception = new FeignClientException(null, null, 0, null, null);
    Assert.assertNull(exception.getRequest());
    Assert.assertNotNull(exception.getHeaders());
    Assert.assertEquals(exception.status(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    Assert.assertEquals(exception.getMessage(), RestApiExceptionUtils.NO_MESSAGE_VALUE);
    Assert.assertNull(exception.getRestApiException());
    Assert.assertNull(exception.getErrorCode());
  }

  @Test
  public void testWithSomeUsefulValues() {
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
    Assert.assertNull(exception.getRequest());
    Assert.assertNotNull(exception.getHeaders());
    Assert.assertTrue(exception.getHeaders().containsKey(HttpHeaders.CONTENT_TYPE));
    Assert.assertTrue(exception.getHeaders().get(HttpHeaders.CONTENT_TYPE)
        .contains(MediaType.APPLICATION_JSON_VALUE));
    Assert.assertEquals(exception.status(), HttpStatus.NOT_FOUND.value());
    Assert.assertEquals("Fatal", exception.getMessage());
    Assert.assertEquals(restApiException, exception.getRestApiException());
    Assert.assertEquals("TEST:0001", exception.getErrorCode());
  }

}
