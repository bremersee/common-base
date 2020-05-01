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

package org.bremersee.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.TreeSet;
import java.util.function.Function;
import org.bremersee.security.SecurityProperties.AuthenticationProperties.AccessMode;
import org.bremersee.security.SecurityProperties.AuthenticationProperties.EurekaAccessProperties;
import org.bremersee.security.SecurityProperties.AuthenticationProperties.PathMatcherProperties;
import org.junit.jupiter.api.Test;

/**
 * The access expression utils test.
 *
 * @author Christian Bremer
 */
class AccessExpressionUtilsTest {

  private Function<String, String> ensureRolePrefixFunction = new SecurityProperties()
      .getAuthentication()::ensureRolePrefix;

  /**
   * Has authority expr.
   */
  @Test
  void hasAuthorityExpr() {
    String actual = AccessExpressionUtils.hasAuthorityExpr("ADMIN", ensureRolePrefixFunction);
    assertEquals("hasAuthority('ROLE_ADMIN')", actual);
    actual = AccessExpressionUtils.hasAuthorityExpr("ROLE_ADMIN", null);
    assertEquals("hasAuthority('ROLE_ADMIN')", actual);
  }

  /**
   * Has any authority expr.
   */
  @Test
  void hasAnyAuthorityExpr() {
    String actual = AccessExpressionUtils.hasAnyAuthorityExpr(null, null);
    assertEquals("", actual);
    actual = AccessExpressionUtils.hasAnyAuthorityExpr(Collections.singletonList("ADMIN"), null);
    assertEquals("hasAuthority('ADMIN')", actual);
    actual = AccessExpressionUtils.hasAnyAuthorityExpr(
        Arrays.asList("ADMIN", "ROLE_SUPERUSER"),
        ensureRolePrefixFunction);
    assertEquals("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPERUSER')", actual);
  }

  /**
   * Has ip address expr.
   */
  @Test
  void hasIpAddressExpr() {
    String actual = AccessExpressionUtils.hasIpAddressExpr("192.168.1.0/24");
    assertEquals("hasIpAddress('192.168.1.0/24')", actual);
    actual = AccessExpressionUtils.hasIpAddressExpr("");
    assertEquals("", actual);
  }

  /**
   * Has ip address expr of ip addresses.
   */
  @Test
  void hasIpAddressExprOfIpAddresses() {
    String actual = AccessExpressionUtils.hasIpAddressExpr(
        Arrays.asList("192.168.1.0/24", "192.168.2.0/24"));
    assertEquals("hasIpAddress('192.168.1.0/24') or hasIpAddress('192.168.2.0/24')", actual);
    actual = AccessExpressionUtils.hasIpAddressExpr(Collections.emptyList());
    assertEquals("", actual);
  }

  /**
   * Has authority or ip address expr.
   */
  @Test
  void hasAuthorityOrIpAddressExpr() {
    String actual = AccessExpressionUtils.hasAuthorityOrIpAddressExpr(null, null, null);
    assertEquals("", actual);

    actual = AccessExpressionUtils.hasAuthorityOrIpAddressExpr(
        Collections.singletonList("ROLE_ADMIN"),
        null,
        null);
    assertEquals("hasAuthority('ROLE_ADMIN')", actual);

    actual = AccessExpressionUtils.hasAuthorityOrIpAddressExpr(
        Arrays.asList("ADMIN", "ROLE_SUPERUSER"),
        ensureRolePrefixFunction,
        null);
    assertEquals("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPERUSER')", actual);

    actual = AccessExpressionUtils.hasAuthorityOrIpAddressExpr(
        Arrays.asList("ADMIN", "ROLE_SUPERUSER"),
        ensureRolePrefixFunction,
        Collections.singletonList("192.168.2.0/24"));
    assertEquals(
        "hasAnyAuthority('ROLE_ADMIN','ROLE_SUPERUSER') or hasIpAddress('192.168.2.0/24')",
        actual);

    actual = AccessExpressionUtils.hasAuthorityOrIpAddressExpr(
        new TreeSet<>(),
        ensureRolePrefixFunction,
        Collections.singleton("192.168.2.0/24"));
    assertEquals("hasIpAddress('192.168.2.0/24')", actual);
  }

  /**
   * Build path matcher properties access expression.
   */
  @Test
  void buildPathMatcherPropertiesAccessExpression() {
    PathMatcherProperties properties = new PathMatcherProperties();
    properties.setAccessMode(AccessMode.DENY_ALL);
    String actual = AccessExpressionUtils.buildAccessExpression(properties, null);
    assertEquals("denyAll", actual);

    properties.setAccessMode(AccessMode.PERMIT_ALL);
    actual = AccessExpressionUtils.buildAccessExpression(properties, null);
    assertEquals("permitAll", actual);

    properties.setAccessMode(AccessMode.AUTHENTICATED);
    actual = AccessExpressionUtils.buildAccessExpression(properties, null);
    assertEquals("isAuthenticated()", actual);

    properties.setIpAddresses(Arrays.asList("192.168.1.0/24", "192.168.2.0/24"));
    actual = AccessExpressionUtils.buildAccessExpression(properties, null);
    assertEquals(
        "hasIpAddress('192.168.1.0/24') or hasIpAddress('192.168.2.0/24') or isAuthenticated()",
        actual);

    properties.setRoles(Collections.singletonList("ADMIN"));
    properties.setIpAddresses(Arrays.asList("192.168.1.0/24", "192.168.2.0/24"));
    actual = AccessExpressionUtils.buildAccessExpression(properties, ensureRolePrefixFunction);
    assertEquals(
        "hasAuthority('ROLE_ADMIN') "
            + "or hasIpAddress('192.168.1.0/24') or hasIpAddress('192.168.2.0/24')",
        actual);
  }

  /**
   * Build eureka properties access expression.
   */
  @Test
  void buildEurekaPropertiesAccessExpression() {
    EurekaAccessProperties properties = new EurekaAccessProperties();
    properties.setRole("ROLE_ADMIN");
    String actual = AccessExpressionUtils.buildAccessExpression(properties, null);
    assertEquals("hasAuthority('ROLE_ADMIN')", actual);

    properties.setRole("");
    actual = AccessExpressionUtils.buildAccessExpression(properties, null);
    assertEquals("isAuthenticated()", actual);

    properties.setIpAddresses(Collections.singletonList("127.0.0.1/32"));
    actual = AccessExpressionUtils.buildAccessExpression(properties, null);
    assertEquals("isAuthenticated() or hasIpAddress('127.0.0.1/32')", actual);
  }
}