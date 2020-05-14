/*
 * Copyright 2020 the original author or authors.
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

package org.bremersee.exception.integration.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bremersee.exception.RestApiExceptionParser;
import org.bremersee.exception.integration.servlet.app.TestConfiguration;
import org.bremersee.exception.model.RestApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

/**
 * The exception handling test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = TestConfiguration.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.web-application-type=servlet",
        "spring.application.name=exception-test",
        "bremersee.auth.resource-server=none",
        "bremersee.exception-mapping.api-paths=/test/**"
    })
@TestInstance(Lifecycle.PER_CLASS) // allows us to use @BeforeAll with a non-static method
public class ExceptionHandlingTest {

  /**
   * The rest template.
   */
  @Autowired
  TestRestTemplate restTemplate;

  /**
   * The rest api exception parser.
   */
  @Autowired
  RestApiExceptionParser restApiExceptionParser;

  /**
   * Exception with rest template.
   */
  @Test
  void exceptionWithRestTemplate() {
    ResponseEntity<String> response = restTemplate
        .getForEntity("/test/exception", String.class);
    assertTrue(response.getStatusCode().is4xxClientError());
    RestApiException restApiException = restApiExceptionParser.parseException(
        response.getBody(),
        response.getHeaders());
    assertEquals("TEST:4711", restApiException.getErrorCode());
    assertEquals(Boolean.TRUE, restApiException.getErrorCodeInherited());
  }

}
