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
import org.bremersee.security.authentication.AuthProperties.ClientCredentialsFlow;
import org.junit.jupiter.api.Test;

/**
 * The client credential flow test.
 *
 * @author Christian Bremer
 */
class ClientCredentialsFlowTest {

  /**
   * Gets client id.
   */
  @Test
  void getClientId() {
    String value = UUID.randomUUID().toString();
    ClientCredentialsFlow expected = new ClientCredentialsFlow();
    expected.setClientId(value);
    ClientCredentialsFlow actual = new ClientCredentialsFlow();
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
    ClientCredentialsFlow expected = new ClientCredentialsFlow();
    expected.setClientSecret(value);
    ClientCredentialsFlow actual = new ClientCredentialsFlow();
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
    ClientCredentialsFlow expected = new ClientCredentialsFlow();
    expected.setTokenEndpoint(value);
    ClientCredentialsFlow actual = new ClientCredentialsFlow();
    actual.setTokenEndpoint(value);
    assertEquals(value, actual.getTokenEndpoint());

    assertEquals(expected, actual);

    assertTrue(actual.toString().contains(value));
  }

}
