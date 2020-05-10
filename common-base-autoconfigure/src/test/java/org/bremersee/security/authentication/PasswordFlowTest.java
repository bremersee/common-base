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

package org.bremersee.security.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.bremersee.security.authentication.AuthProperties.PasswordFlow;
import org.junit.jupiter.api.Test;

/**
 * The password flow test.
 *
 * @author Christian Bremer
 */
class PasswordFlowTest {

  /**
   * Gets client id.
   */
  @Test
  void getClientId() {
    String value = UUID.randomUUID().toString();
    PasswordFlow expected = new PasswordFlow();
    expected.setClientId(value);
    PasswordFlow actual = new PasswordFlow();
    actual.setClientId(value);
    assertEquals(value, actual.getClientId());

    assertEquals(expected, actual);
    assertNotEquals(actual, null);
    assertNotEquals(actual, new Object());

    assertTrue(actual.toString().contains(value));
  }

  /**
   * Gets client secret.
   */
  @Test
  void getClientSecret() {
    String value = UUID.randomUUID().toString();
    PasswordFlow expected = new PasswordFlow();
    expected.setClientSecret(value);
    PasswordFlow actual = new PasswordFlow();
    actual.setClientSecret(value);
    assertEquals(value, actual.getClientSecret());

    assertEquals(expected, actual);

    assertFalse(actual.toString().contains(value));
  }

  /**
   * Gets token endpoint.
   */
  @Test
  void getTokenEndpoint() {
    String value = UUID.randomUUID().toString();
    PasswordFlow expected = new PasswordFlow();
    expected.setTokenEndpoint(value);
    PasswordFlow actual = new PasswordFlow();
    actual.setTokenEndpoint(value);
    assertEquals(value, actual.getTokenEndpoint());

    assertEquals(expected, actual);

    assertTrue(actual.toString().contains(value));
  }

  /**
   * Gets system username.
   */
  @Test
  void getSystemUsername() {
    String value = UUID.randomUUID().toString();
    PasswordFlow expected = new PasswordFlow();
    expected.setUsername(value);
    PasswordFlow actual = new PasswordFlow();
    actual.setUsername(value);
    assertEquals(value, actual.getUsername());

    assertEquals(expected, actual);
    assertNotEquals(actual, null);
    assertNotEquals(actual, new Object());

    assertTrue(actual.toString().contains(value));
  }

  /**
   * Gets system password.
   */
  @Test
  void getSystemPassword() {
    String value = UUID.randomUUID().toString();
    PasswordFlow expected = new PasswordFlow();
    expected.setPassword(value);
    PasswordFlow actual = new PasswordFlow();
    actual.setPassword(value);
    assertEquals(value, actual.getPassword());

    assertEquals(expected, actual);

    assertFalse(actual.toString().contains(value));
  }

}
