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

package org.bremersee.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bremersee.exception.app.servlet.WebTestConfiguration;
import org.bremersee.exception.model.RestApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

/**
 * The web integration test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = WebTestConfiguration.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.web-application-type=servlet",
        "spring.application.name=servlet-test",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/jwk",
        "bremersee.security.authentication.resource-server-auto-configuration=true",
        "bremersee.security.authentication.enable-jwt-support=true",
        "bremersee.security.authentication.any-access-mode=permit_all",
        "bremersee.exception-mapping.api-paths=/test/**"
    })
public class WebIntegrationTest {

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
