/*
 * Copyright 2019-2022 the original author or authors.
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

import feign.Request;
import feign.Request.HttpMethod;
import feign.RequestTemplate;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.exception.model.RestApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * The feign client exception test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class FeignClientExceptionTest {

  /**
   * Test feign client exception.
   */
  @Test
  void testFeignClientException(SoftAssertions softly) {
    RestApiException restApiException = new RestApiException();
    restApiException.setErrorCode("TEST:0001");
    Map<String, Collection<String>> headers = new LinkedHashMap<>();
    headers.put(HttpHeaders.CONTENT_TYPE, List.of(MediaType.APPLICATION_JSON_VALUE));
    Request request = Request.create(
        HttpMethod.GET,
        "http://localhost",
        Map.of(), // headers
        new byte[0],  // body
        StandardCharsets.UTF_8,
        new RequestTemplate());
    FeignClientException actual = new FeignClientException(404, "Fatal", request, headers,
        new byte[0], restApiException);
    softly.assertThat(actual.getErrorCode())
        .isEqualTo("TEST:0001");
    softly.assertThat(actual.getMultiValueHeaders())
        .containsKey(HttpHeaders.CONTENT_TYPE);
    softly.assertThat(actual.status())
        .isEqualTo(HttpStatus.NOT_FOUND.value());
    softly.assertThat(actual.getMessage())
        .isEqualTo("Fatal");
    softly.assertThat(actual.getRestApiException())
        .isEqualTo(restApiException);
  }

}
