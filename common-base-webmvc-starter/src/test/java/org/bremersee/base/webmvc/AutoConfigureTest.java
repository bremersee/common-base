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

package org.bremersee.base.webmvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.bremersee.base.webmvc.app.TestConfiguration;
import org.bremersee.security.core.AuthorityConstants;
import org.bremersee.test.security.authentication.WithJwtAuthenticationToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * The auto configure test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = TestConfiguration.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.application.name=common-base-webmvc-test",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/jwk",
        "bremersee.exception-mapping.api-paths=/api/**",
        "bremersee.security.authentication.enable-jwt-support=true"
    })
@TestInstance(Lifecycle.PER_CLASS)
@Slf4j
public class AutoConfigureTest {

  /**
   * The Context.
   */
  @Autowired
  WebApplicationContext context;

  /**
   * The Rest template.
   */
  @Autowired
  TestRestTemplate restTemplate;

  /**
   * The Mvc.
   */
  MockMvc mvc;

  /**
   * Sets .
   */
  @BeforeAll
  void setup() {
    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  /**
   * Current user name.
   *
   * @throws Exception the exception
   */
  @WithJwtAuthenticationToken(preferredUsername = "leopold")
  @Test
  void currentUserName() throws Exception {
    mvc.perform(get("/api/name"))
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().string("leopold"));
  }

  /**
   * Current admin name.
   *
   * @throws Exception the exception
   */
  @WithJwtAuthenticationToken(
      preferredUsername = "leopold",
      roles = {AuthorityConstants.ADMIN_ROLE_NAME})
  @Test
  void currentAdminName() throws Exception {
    mvc.perform(get("/api/admin/name"))
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().string("leopold"));
  }

  /**
   * Current admin name and expect forbidden.
   *
   * @throws Exception the exception
   */
  @WithJwtAuthenticationToken(preferredUsername = "leopold")
  @Test
  void currentAdminNameAndExpectForbidden() throws Exception {
    mvc.perform(get("/api/admin/name"))
        .andExpect(status().is4xxClientError());
  }

  /**
   * Exception.
   */
  @Test
  void exception() {
    ResponseEntity<String> response = restTemplate.getForEntity("/api/exception", String.class);
    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("TEST:4711"));
  }

  /**
   * Health.
   *
   * @throws Exception the exception
   */
  @Test
  void health() throws Exception {
    mvc.perform(get("/actuator/health"))
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().string("{\"status\":\"UP\"}"));
  }

  /**
   * Info.
   *
   * @throws Exception the exception
   */
  @WithMockUser(username = "actuator", authorities = {AuthorityConstants.ACTUATOR_ROLE_NAME})
  @Test
  void info() throws Exception {
    mvc.perform(get("/actuator/info"))
        .andExpect(status().is2xxSuccessful());
  }

  /**
   * Info and expect client error.
   *
   * @throws Exception the exception
   */
  @Test
  void infoAndExpectClientError() throws Exception {
    mvc.perform(get("/actuator/info"))
        .andExpect(status().is4xxClientError());
  }

}
