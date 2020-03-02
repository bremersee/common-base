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

import java.util.Optional;
import org.bremersee.security.authentication.ClientCredentialsFlowProperties.Builder;
import org.junit.jupiter.api.Test;
import org.springframework.util.MultiValueMap;

/**
 * The client credentials flow properties test.
 *
 * @author Christian Bremer
 */
class ClientCredentialsFlowPropertiesTest {

  /**
   * Gets client id.
   */
  @Test
  void getClientId() {
    Builder model = ClientCredentialsFlowProperties.builder();
    model.clientId("value");
    assertEquals("value", model.build().getClientId());

    assertNotEquals(model, null);
    assertNotEquals(model, new Object());
    assertEquals(model, model);
    assertEquals(model, ClientCredentialsFlowProperties.builder().clientId("value"));

    assertTrue(model.toString().contains("value"));
  }

  /**
   * Gets client secret.
   */
  @Test
  void getClientSecret() {
    Builder model = ClientCredentialsFlowProperties.builder();
    model.clientSecret("value");
    assertEquals("value", model.build().getClientSecret());
    assertEquals(model, ClientCredentialsFlowProperties.builder().clientSecret("value"));
    assertFalse(model.toString().contains("value"));
  }

  /**
   * Gets additional properties.
   */
  @Test
  void getAdditionalProperties() {
    Builder model = ClientCredentialsFlowProperties.builder();
    model.add("key", "value");
    assertEquals("value", model.build().getAdditionalProperties().getFirst("key"));
    assertEquals(model, ClientCredentialsFlowProperties.builder().add("key", "value"));
    assertTrue(model.toString().contains("value"));
  }

  /**
   * Gets token endpoint.
   */
  @Test
  void getTokenEndpoint() {
    Builder model = ClientCredentialsFlowProperties.builder();
    model.tokenEndpoint("value");
    assertEquals("value", model.build().getTokenEndpoint());
    assertEquals(model, ClientCredentialsFlowProperties.builder().tokenEndpoint("value"));
    assertTrue(model.toString().contains("value"));
  }

  /**
   * Gets basic auth properties.
   */
  @Test
  void getBasicAuthProperties() {
    Optional<BasicAuthProperties> model = ClientCredentialsFlowProperties.builder()
        .clientId("foo")
        .clientSecret("bar")
        .build()
        .getBasicAuthProperties();
    assertTrue(model.isPresent());
    assertEquals("foo", model.get().getUsername());
    assertEquals("bar", model.get().getPassword());
  }

  /**
   * From.
   */
  @Test
  void from() {
    ClientCredentialsFlowProperties expected = ClientCredentialsFlowProperties.builder()
        .clientId("client")
        .clientSecret("secret")
        .tokenEndpoint("http://localhost/token")
        .add("key", "value")
        .from(null)
        .build();

    ClientCredentialsFlowProperties actual = ClientCredentialsFlowProperties.builder()
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
    ClientCredentialsFlowProperties expected = ClientCredentialsFlowProperties.builder()
        .clientId("foo")
        .clientSecret("bar")
        .tokenEndpoint("http://localhost/token")
        .add("key", "value")
        .build();
    MultiValueMap<String, String> actual = expected.createBody();
    assertEquals("client_credentials", actual.getFirst("grant_type"));
    assertFalse(actual.containsKey("client_id"));
    assertFalse(actual.containsKey("client_secret"));
  }
}