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

package org.bremersee.actuator.security.authentication.resourceserver.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.bremersee.actuator.security.authentication.resourceserver.servlet.app.TestConfiguration;
import org.bremersee.test.security.authentication.WithJwtAuthenticationToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

/**
 * The jwt and in memory test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = TestConfiguration.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.web-application-type=servlet",
        "spring.application.name=resourceserver-jwt-in-memory",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/jwk",
        "management.endpoints.web.exposure.include=*",
        "bremersee.actuator.auth.enable=auto",
        "bremersee.auth.resource-server=auto",
        "bremersee.auth.any-access-mode=deny_all",
        "bremersee.auth.path-matchers[0].ant-pattern=/public/**",
        "bremersee.auth.path-matchers[0].access-mode=permit_all",
        "bremersee.auth.path-matchers[1].ant-pattern=/protected/**",
        "bremersee.auth.path-matchers[1].roles=ROLE_USER",
        "bremersee.auth.path-matchers[2].ant-pattern=/protected/**",
        "bremersee.auth.path-matchers[2].http-method=POST",
        "bremersee.auth.path-matchers[2].roles=ROLE_ADMIN",
        "bremersee.auth.in-memory-users[0].name=user",
        "bremersee.auth.in-memory-users[0].password=user",
        "bremersee.auth.in-memory-users[0].authorities=ROLE_USER",
        "bremersee.auth.in-memory-users[1].name=admin",
        "bremersee.auth.in-memory-users[1].password=admin",
        "bremersee.auth.in-memory-users[1].authorities=ROLE_ADMIN,ROLE_ACTUATOR_ADMIN",
        "bremersee.auth.in-memory-users[2].name=someone",
        "bremersee.auth.in-memory-users[2].password=someone",
        "bremersee.auth.in-memory-users[2].authorities=ROLE_SOMETHING",
        "bremersee.auth.in-memory-users[3].name=actuator",
        "bremersee.auth.in-memory-users[3].password=actuator",
        "bremersee.auth.in-memory-users[3].authorities=ROLE_ACTUATOR",
        "bremersee.exception-mapping.api-paths=/**"
    })
@TestInstance(Lifecycle.PER_CLASS) // allows us to use @BeforeAll with a non-static method
public class JwtAndInMemoryTest {

  /**
   * The test rest template.
   */
  @Autowired
  TestRestTemplate testRestTemplate;

  /**
   * The rest template builder.
   */
  @Autowired
  RestTemplateBuilder restTemplateBuilder;

  /**
   * The local server port.
   */
  @LocalServerPort
  int port;

  /**
   * The context.
   */
  @Autowired
  WebApplicationContext context;

  /**
   * The mock mvc.
   */
  MockMvc mvc;

  /**
   * Setup mock mvc.
   */
  @BeforeAll
  void setup() {
    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  /**
   * Base url of the local server.
   *
   * @return the base url of the local server
   */
  String baseUrl() {
    return "http://localhost:" + port;
  }

  /**
   * Rest template rest template.
   *
   * @return the rest template
   */
  RestTemplate restTemplate() {
    return restTemplateBuilder
        .rootUri(baseUrl())
        .build();
  }

  /**
   * Rest template rest template.
   *
   * @param user the user
   * @param password the password
   * @return the rest template
   */
  RestTemplate restTemplate(String user, String password) {
    return restTemplateBuilder
        .rootUri(baseUrl())
        .basicAuthentication(user, password)
        .build();
  }

  /**
   * Gets public.
   *
   * @throws Exception the exception
   */
  @Test
  void getPublic() throws Exception {
    mvc.perform(get("/public")
        .accept(MediaType.TEXT_PLAIN))
        .andExpect(status().isOk())
        .andExpect(content().string("public"));
  }

  /**
   * Gets protected.
   *
   * @throws Exception the exception
   */
  @Test
  @WithJwtAuthenticationToken(roles = "ROLE_USER")
  void getProtected() throws Exception {
    mvc.perform(get("/protected"))
        .andExpect(status().isOk())
        .andExpect(content().string("protected"));
  }

  /**
   * Gets protected and expect forbidden.
   *
   * @throws Exception the exception
   */
  @Test
  @WithJwtAuthenticationToken(roles = "ROLE_SOMETHING")
  void getProtectedAndExpectForbidden() throws Exception {
    mvc.perform(get("/protected"))
        .andExpect(status().isForbidden());
  }

  /**
   * Post protected.
   *
   * @throws Exception the exception
   */
  @Test
  @WithJwtAuthenticationToken(roles = "ROLE_ADMIN")
  void postProtected() throws Exception {
    mvc.perform(post("/protected")
        .accept(MediaType.TEXT_PLAIN)
        .contentType(MediaType.TEXT_PLAIN)
        .content("Good Morning"))
        .andExpect(status().isOk())
        .andExpect(content().string("Good Morning"));
  }

  /**
   * Gets health.
   */
  @Test
  void getHealth() {
    String response = restTemplate()
        .getForEntity("/actuator/health", String.class)
        .getBody();
    assertNotNull(response);
    assertTrue(response.contains("\"UP\""));
  }

  /**
   * Gets metrics.
   */
  @Test
  void getMetrics() {
    ResponseEntity<String> response = restTemplate("actuator", "actuator")
        .getForEntity("/actuator/metrics", String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  /**
   * Gets metrics and expect exception.
   */
  @Test
  void getMetricsAndExpectException() {
    assertThrows(HttpClientErrorException.class, () -> restTemplate("user", "user")
        .getForEntity("/actuator/metrics", String.class));
  }

  /**
   * Gets metrics and expect forbidden.
   *
   * @throws Exception the exception
   */
  @Test
  @WithMockUser(username = "user", password = "user", authorities = "ROLE_USER")
  void getMetricsAndExpectForbidden() throws Exception {
    mvc.perform(get("/actuator/metrics"))
        .andExpect(status().isForbidden());
  }

}
