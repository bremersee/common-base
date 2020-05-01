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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.bremersee.test.security.authentication.WithJwtAuthenticationToken;
import org.bremersee.web.app.servlet.WebTestConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * The web jwt integration test.
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
public class WebJwtIntegrationTest {

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
   * Gets public.
   *
   * @throws Exception the exception
   */
  @Test
  void getPublic() throws Exception {
    mvc.perform(get("/public")
        .accept(MediaType.TEXT_PLAIN))
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().string("public"));
  }

  /**
   * Gets protected.
   *
   * @throws Exception the exception
   */
  @WithJwtAuthenticationToken(roles = "ROLE_USER")
  @Test
  void getProtected() throws Exception {
    mvc.perform(get("/protected"))
        .andExpect(status().isOk())
        .andExpect(content().string("protected"));
  }

  /**
   * Gets protected anf expect forbidden.
   *
   * @throws Exception the exception
   */
  @WithJwtAuthenticationToken(roles = "ROLE_SOMETHING")
  @Test
  void getProtectedAnfExpectForbidden() throws Exception {
    mvc.perform(get("/protected"))
        .andExpect(status().isForbidden());
  }

  /**
   * Post protected.
   *
   * @throws Exception the exception
   */
  @WithJwtAuthenticationToken(roles = "ROLE_ADMIN")
  @Test
  void postProtected() throws Exception {
    mvc.perform(post("/protected")
        .accept(MediaType.TEXT_PLAIN)
        .contentType(MediaType.TEXT_PLAIN)
        .content("Good Morning"))
        .andExpect(status().isOk())
        .andExpect(content().string("Good Morning"));
  }

}
