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

package org.bremersee.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bremersee.web.app.servlet.WebTestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * The web in memory integration test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = WebTestConfiguration.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.web-application-type=servlet",
        "spring.application.name=servlet-test",
        "bremersee.security.authentication.resource-server-auto-configuration=true",
        "bremersee.security.authentication.enable-jwt-support=false",
        "bremersee.security.authentication.basic-auth-users[0].name=user",
        "bremersee.security.authentication.basic-auth-users[0].password=user",
        "bremersee.security.authentication.basic-auth-users[0].authorities=ROLE_USER",
        "bremersee.security.authentication.basic-auth-users[1].name=admin",
        "bremersee.security.authentication.basic-auth-users[1].password=admin",
        "bremersee.security.authentication.basic-auth-users[1].authorities=ROLE_ADMIN",
        "bremersee.security.authentication.basic-auth-users[2].name=someone",
        "bremersee.security.authentication.basic-auth-users[2].password=someone",
        "bremersee.security.authentication.basic-auth-users[2].authorities=ROLE_SOMETHING",
        "bremersee.security.authentication.any-access-mode=deny_all",
        "bremersee.security.authentication.path-matchers[0].ant-pattern=/public/**",
        "bremersee.security.authentication.path-matchers[0].access-mode=permit_all",
        "bremersee.security.authentication.path-matchers[1].ant-pattern=/protected/**",
        "bremersee.security.authentication.path-matchers[1].roles=ROLE_USER",
        "bremersee.security.authentication.path-matchers[2].ant-pattern=/protected/**",
        "bremersee.security.authentication.path-matchers[2].http-method=POST",
        "bremersee.security.authentication.path-matchers[2].roles=ROLE_ADMIN",
        "bremersee.exception-mapping.api-paths=/**",
    })
@TestInstance(Lifecycle.PER_CLASS) // allows us to use @BeforeAll with a non-static method
public class WebInMemoryIntegrationTest {

  /**
   * The rest template.
   */
  @Autowired
  TestRestTemplate restTemplate;

  /**
   * Gets public.
   */
  @Test
  void getPublic() {
    assertEquals(
        "public",
        restTemplate
            .getForEntity("/public", String.class)
            .getBody());
  }

  /**
   * Gets protected.
   */
  @Test
  void getProtected() {
    ResponseEntity<String> response = restTemplate
        .withBasicAuth("user", "user")
        .getForEntity("/protected", String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("protected", response.getBody());
  }

  /**
   * Gets protected anf expect forbidden.
   */
  @Test
  void getProtectedAnfExpectForbidden() {
    ResponseEntity<String> response = restTemplate
        .withBasicAuth("someone", "someone")
        .getForEntity("/protected", String.class);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  /**
   * Post protected.
   */
  @Test
  void postProtected() {
    ResponseEntity<String> response = restTemplate
        .withBasicAuth("admin", "admin")
        .postForEntity("/protected", "hello", String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("hello", response.getBody());
  }

}
