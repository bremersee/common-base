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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import org.bremersee.security.authentication.AuthProperties.PathMatcherProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

/**
 * The path matcher properties test.
 *
 * @author Christian Bremer
 */
class PathMatcherPropertiesTest {

  /**
   * Gets http method.
   */
  @Test
  void getHttpMethod() {
    String value = "POST";
    PathMatcherProperties expected = new PathMatcherProperties();
    expected.setHttpMethod(value);

    PathMatcherProperties actual = new PathMatcherProperties();

    assertEquals("*", actual.getHttpMethod());

    actual.setHttpMethod(value);
    assertEquals(value, actual.getHttpMethod());

    assertNotEquals(actual, null);
    assertNotEquals(actual, new Object());
    assertEquals(expected, actual);

    assertTrue(actual.toString().contains(value));
  }

  /**
   * Gets ant pattern.
   */
  @Test
  void getAntPattern() {
    String value = "/public/**";
    PathMatcherProperties expected = new PathMatcherProperties();
    expected.setAntPattern(value);

    PathMatcherProperties actual = new PathMatcherProperties();

    assertEquals("/**", actual.getAntPattern());

    actual.setAntPattern(value);
    assertEquals(value, actual.getAntPattern());

    assertEquals(expected, actual);

    assertTrue(actual.toString().contains(value));
  }

  /**
   * Gets access mode.
   */
  @Test
  void getAccessMode() {
    AccessMode value = AccessMode.DENY_ALL;
    PathMatcherProperties actual = new PathMatcherProperties();
    assertEquals(AccessMode.AUTHENTICATED, actual.getAccessMode());
    actual.setAccessMode(value);
    assertEquals(value, actual.getAccessMode());
  }

  /**
   * Gets roles.
   */
  @Test
  void getRoles() {
    String value = UUID.randomUUID().toString();
    PathMatcherProperties expected = new PathMatcherProperties();
    expected.setRoles(Collections.singletonList(value));
    PathMatcherProperties actual = new PathMatcherProperties();
    actual.setRoles(Collections.singletonList(value));
    assertEquals(Collections.singletonList(value), actual.getRoles());
    assertEquals(expected, actual);
  }

  /**
   * Gets ip addresses.
   */
  @Test
  void getIpAddresses() {
    String value = UUID.randomUUID().toString();
    PathMatcherProperties expected = new PathMatcherProperties();
    expected.setIpAddresses(Collections.singletonList(value));
    PathMatcherProperties actual = new PathMatcherProperties();
    actual.setIpAddresses(Collections.singletonList(value));
    assertEquals(Collections.singletonList(value), actual.getIpAddresses());
    assertEquals(expected, actual);
  }

  /**
   * Http method.
   */
  @Test
  void httpMethod() {
    PathMatcherProperties properties = new PathMatcherProperties();
    assertNull(properties.httpMethod());

    properties.setHttpMethod("get");
    assertEquals(HttpMethod.GET, properties.httpMethod());
  }

  /**
   * Roles.
   */
  @Test
  void roles() {
    String value = UUID.randomUUID().toString();
    PathMatcherProperties properties = new PathMatcherProperties();
    properties.setRoles(Collections.singletonList(value));
    Set<String> roles = properties.roles(null);
    assertNotNull(roles);
    assertTrue(roles.contains(value));

    roles = properties.roles(role -> "ROLE_" + role);
    assertNotNull(roles);
    assertTrue(roles.contains("ROLE_" + value));
  }

  /**
   * Access expression.
   */
  @Test
  void accessExpression() {
    PathMatcherProperties properties = new PathMatcherProperties();
    properties.setAccessMode(AccessMode.AUTHENTICATED);
    assertEquals("isAuthenticated()", properties.accessExpression(null));
  }

}
