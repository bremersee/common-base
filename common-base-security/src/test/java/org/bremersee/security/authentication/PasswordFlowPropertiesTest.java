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

package org.bremersee.security.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bremersee.security.authentication.PasswordFlowProperties.Builder;
import org.junit.jupiter.api.Test;
import org.springframework.util.MultiValueMap;

/**
 * The password flow properties test.
 *
 * @author Christian Bremer
 */
class PasswordFlowPropertiesTest {

  /**
   * Gets client id.
   */
  @Test
  void getClientId() {
    Builder model = PasswordFlowProperties.builder();
    model.clientId("value");
    assertEquals("value", model.build().getClientId());

    assertNotEquals(model, null);
    assertNotEquals(model, new Object());
    assertEquals(model, model);
    assertEquals(model, PasswordFlowProperties.builder().clientId("value"));

    assertTrue(model.toString().contains("value"));
  }

  /**
   * Gets client secret.
   */
  @Test
  void getClientSecret() {
    Builder model = PasswordFlowProperties.builder();
    model.clientSecret("value");
    assertEquals("value", model.build().getClientSecret());
    assertEquals(model, PasswordFlowProperties.builder().clientSecret("value"));
    assertFalse(model.toString().contains("value"));
  }

  /**
   * Gets username.
   */
  @Test
  void getUsername() {
    Builder model = PasswordFlowProperties.builder();
    model.username("value");
    assertEquals("value", model.build().getUsername());
    assertEquals(model, PasswordFlowProperties.builder().username("value"));
    assertTrue(model.toString().contains("value"));
  }

  /**
   * Gets password.
   */
  @Test
  void getPassword() {
    Builder model = PasswordFlowProperties.builder();
    model.password("value");
    assertEquals("value", model.build().getPassword());
    assertEquals(model, PasswordFlowProperties.builder().password("value"));
    assertFalse(model.toString().contains("value"));
  }

  /**
   * Gets additional properties.
   */
  @Test
  void getAdditionalProperties() {
    Builder model = PasswordFlowProperties.builder();
    model.add("key", "value");
    assertEquals("value", model.build().getAdditionalProperties().getFirst("key"));
    assertEquals(model, PasswordFlowProperties.builder().add("key", "value"));
    assertTrue(model.toString().contains("value"));
  }

  /**
   * Gets token endpoint.
   */
  @Test
  void getTokenEndpoint() {
    Builder model = PasswordFlowProperties.builder();
    model.tokenEndpoint("value");
    assertEquals("value", model.build().getTokenEndpoint());
    assertEquals(model, PasswordFlowProperties.builder().tokenEndpoint("value"));
    assertTrue(model.toString().contains("value"));
  }

  /**
   * Gets basic auth properties.
   */
  @Test
  void getBasicAuthProperties() {
    assertTrue(PasswordFlowProperties.builder().build().getBasicAuthProperties().isEmpty());
  }

  /**
   * From.
   */
  @Test
  void from() {
    PasswordFlowProperties expected = PasswordFlowProperties.builder()
        .clientId("client")
        .clientSecret("secret")
        .tokenEndpoint("http://localhost/token")
        .add("key", "value")
        .from(null)
        .build();

    PasswordFlowProperties actual = PasswordFlowProperties.builder()
        .from(expected)
        .build();

    assertEquals(expected, actual);
    assertTrue(actual.toString().contains("http://localhost/token"));
  }

  /**
   * Create body.
   */
  @Test
  void createBody() {
    PasswordFlowProperties expected = PasswordFlowProperties.builder()
        .clientId("client")
        .username("user")
        .tokenEndpoint("http://localhost/token")
        .add("key", "value")
        .build();
    MultiValueMap<String, String> actual = expected.createBody();
    assertEquals("password", actual.getFirst("grant_type"));
    assertEquals("client", actual.getFirst("client_id"));
    assertEquals("", actual.getFirst("client_secret"));
    assertEquals("user", actual.getFirst("username"));
    assertEquals("", actual.getFirst("password"));

    expected = PasswordFlowProperties.builder()
        .from(expected)
        .clientSecret("secret")
        .password("changeit")
        .build();

    actual = expected.createBody();
    assertEquals("password", actual.getFirst("grant_type"));
    assertEquals("client", actual.getFirst("client_id"));
    assertEquals("secret", actual.getFirst("client_secret"));
    assertEquals("user", actual.getFirst("username"));
    assertEquals("changeit", actual.getFirst("password"));
  }
}