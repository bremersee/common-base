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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.UUID;
import org.bremersee.security.authentication.AuthProperties.EurekaAccessProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * The eureka access properties test.
 *
 * @author Christian Bremer
 */
class EurekaAccessPropertiesTest {

  /**
   * Gets username.
   */
  @Test
  void getUsername() {
    String value = UUID.randomUUID().toString();
    EurekaAccessProperties expected = new EurekaAccessProperties();
    expected.setUsername(value);
    EurekaAccessProperties actual = new EurekaAccessProperties();
    actual.setUsername(value);
    assertEquals(value, actual.getUsername());

    assertEquals(expected, actual);
    assertNotEquals(actual, null);
    assertNotEquals(actual, new Object());

    assertTrue(actual.toString().contains(value));
  }

  /**
   * Gets password.
   */
  @Test
  void getPassword() {
    String value = UUID.randomUUID().toString();
    EurekaAccessProperties expected = new EurekaAccessProperties();
    expected.setPassword(value);
    EurekaAccessProperties actual = new EurekaAccessProperties();
    actual.setPassword(value);
    assertEquals(value, actual.getPassword());
    assertEquals(expected, actual);
    assertFalse(actual.toString().contains(value));
  }

  /**
   * Gets ip addresses.
   */
  @Test
  void getIpAddresses() {
    String value = UUID.randomUUID().toString();
    EurekaAccessProperties expected = new EurekaAccessProperties();
    expected.setIpAddresses(Collections.singletonList(value));
    EurekaAccessProperties actual = new EurekaAccessProperties();
    actual.setIpAddresses(Collections.singletonList(value));
    assertEquals(Collections.singletonList(value), actual.getIpAddresses());
    assertEquals(expected, actual);
    assertTrue(actual.toString().contains(value));
  }

  /**
   * Gets role.
   */
  @Test
  void getRole() {
    String value = UUID.randomUUID().toString();
    EurekaAccessProperties expected = new EurekaAccessProperties();
    expected.setRole(value);
    EurekaAccessProperties actual = new EurekaAccessProperties();
    actual.setRole(value);
    assertEquals(value, actual.getRole());

    assertEquals(expected, actual);
    assertNotEquals(actual, null);
    assertNotEquals(actual, new Object());

    assertTrue(actual.toString().contains(value));
  }

  /**
   * Role.
   */
  @Test
  void role() {
    String value = "ADMIN";
    EurekaAccessProperties actual = new EurekaAccessProperties();
    actual.setRole(value);
    assertEquals("ADMIN", actual.role(null));
    assertEquals(
        "ROLE_ADMIN",
        actual.role(new AuthProperties()::ensureRolePrefix));
  }

  /**
   * Build access expression.
   */
  @Test
  void buildAccessExpression() {
    EurekaAccessProperties actual = new EurekaAccessProperties();
    actual.setRole("ROLE_EUREKA");
    assertEquals(
        "hasAuthority('ROLE_EUREKA')",
        actual.buildAccessExpression(null));
    actual.setRole("");
    assertEquals(
        "isAuthenticated()",
        actual.buildAccessExpression(null));
  }

  /**
   * Build basic auth user details.
   */
  @Test
  void buildBasicAuthUserDetails() {
    EurekaAccessProperties properties = new EurekaAccessProperties();
    properties.setUsername(UUID.randomUUID().toString());
    properties.setPassword(UUID.randomUUID().toString());
    properties.setRole("ROLE_QWERTZ");
    UserDetails[] userDetails = properties.buildBasicAuthUserDetails(null);
    assertNotNull(userDetails);
    assertEquals(1, userDetails.length);
    UserDetails user = userDetails[0];
    assertEquals(properties.getUsername(), user.getUsername());
    assertNotNull(user.getPassword());
    assertTrue(user.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch(a -> a.equals(properties.getRole())));

    userDetails = properties.buildBasicAuthUserDetails(null, user);
    assertNotNull(userDetails);
    assertEquals(2, userDetails.length);

    properties.setUsername("");
    userDetails = properties.buildBasicAuthUserDetails(null, user);
    assertNotNull(userDetails);
    assertEquals(1, userDetails.length);
  }

}
