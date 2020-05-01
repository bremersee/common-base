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

package org.bremersee.actuator.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.UUID;
import org.bremersee.security.authentication.PasswordFlowProperties;
import org.bremersee.security.core.AuthorityConstants;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;

/**
 * The actuator security properties test.
 *
 * @author Christian Bremer
 */
class ActuatorSecurityPropertiesTest {

  /**
   * Gets role prefix.
   */
  @Test
  void getRolePrefix() {
    String value = UUID.randomUUID().toString();
    ActuatorSecurityProperties expected = new ActuatorSecurityProperties();
    expected.setRolePrefix(value);

    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    actual.setRolePrefix(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Gets unauthenticated endpoints.
   */
  @Test
  void getUnauthenticatedEndpoints() {
    Class<?> value = InfoEndpoint.class;
    ActuatorSecurityProperties expected = new ActuatorSecurityProperties();
    expected.setUnauthenticatedEndpoints(Collections.singletonList(value));

    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    actual.setUnauthenticatedEndpoints(Collections.singletonList(value));

    assertEquals(expected.getUnauthenticatedEndpoints(), actual.getUnauthenticatedEndpoints());
    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value.toString()));
  }

  /**
   * Unauthenticated endpoints or defaults.
   */
  @Test
  void unauthenticatedEndpointsOrDefaults() {
    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    assertTrue(actual.getUnauthenticatedEndpoints().isEmpty());
    assertTrue(actual.unauthenticatedEndpointsOrDefaults().contains(HealthEndpoint.class));
    assertTrue(actual.unauthenticatedEndpointsOrDefaults().contains(InfoEndpoint.class));

    actual.setUnauthenticatedEndpoints(Collections.singletonList(HealthEndpoint.class));
    assertTrue(actual.unauthenticatedEndpointsOrDefaults().contains(HealthEndpoint.class));
    assertFalse(actual.unauthenticatedEndpointsOrDefaults().contains(InfoEndpoint.class));
  }

  /**
   * Roles or defaults.
   */
  @Test
  void rolesOrDefaults() {
    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    assertTrue(actual.getRoles().isEmpty());
    assertTrue(actual.rolesOrDefaults().contains(AuthorityConstants.ACTUATOR_ROLE_NAME));
    assertTrue(actual.rolesOrDefaults().contains(AuthorityConstants.ACTUATOR_ADMIN_ROLE_NAME));
    actual.setRoles(Collections.singletonList("ROLE_FOOBAR"));
    assertFalse(actual.rolesOrDefaults().contains(AuthorityConstants.ACTUATOR_ROLE_NAME));
  }

  /**
   * Admin roles or defaults.
   */
  @Test
  void adminRolesOrDefaults() {
    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    assertTrue(actual.getAdminRoles().isEmpty());
    assertFalse(actual.adminRolesOrDefaults().contains(AuthorityConstants.ACTUATOR_ROLE_NAME));
    assertTrue(actual.adminRolesOrDefaults().contains(AuthorityConstants.ACTUATOR_ADMIN_ROLE_NAME));
    actual.setAdminRoles(Collections.singletonList("ROLE_FOOBAR"));
    assertFalse(
        actual.adminRolesOrDefaults().contains(AuthorityConstants.ACTUATOR_ADMIN_ROLE_NAME));
  }

  /**
   * Build access expression.
   */
  @Test
  void buildAccessExpression() {
    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    assertEquals(
        "hasAnyAuthority('ROLE_ACTUATOR','ROLE_ACTUATOR_ADMIN','ROLE_ADMIN')",
        actual.buildAccessExpression());
  }

  /**
   * Build admin access expression.
   */
  @Test
  void buildAdminAccessExpression() {
    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    assertEquals(
        "hasAnyAuthority('ROLE_ACTUATOR_ADMIN','ROLE_ADMIN')",
        actual.buildAdminAccessExpression());
  }

  /**
   * Ensure role prefix.
   */
  @Test
  void ensureRolePrefix() {
    ActuatorSecurityProperties properties = new ActuatorSecurityProperties();
    properties.setRolePrefix("FOO_");
    assertEquals("FOO_BAR", properties.ensureRolePrefix("BAR"));
    assertEquals("FOO_BAR", properties.ensureRolePrefix("FOO_BAR"));

    properties.setRolePrefix("");
    assertEquals("BAR", properties.ensureRolePrefix("BAR"));
    assertEquals("FOO_BAR", properties.ensureRolePrefix("FOO_BAR"));
  }

  /**
   * Is enable auto configuration.
   */
  @Test
  void isEnableAutoConfiguration() {
    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    assertTrue(actual.isEnableAutoConfiguration());
    actual.setEnableAutoConfiguration(false);
    assertFalse(actual.isEnableAutoConfiguration());
  }

  /**
   * Is cors disabled.
   */
  @Test
  void isCorsEnabled() {
    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    actual.setEnableCors(true);
    assertTrue(actual.isEnableCors());
    actual.setEnableCors(false);
    assertFalse(actual.isEnableCors());
  }

  /**
   * Gets admin roles.
   */
  @Test
  void getAdminRoles() {
    String value = UUID.randomUUID().toString();
    ActuatorSecurityProperties expected = new ActuatorSecurityProperties();
    expected.setAdminRoles(Collections.singletonList(value));

    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    actual.setAdminRoles(Collections.singletonList(value));

    assertEquals(expected.getAdminRoles(), actual.getAdminRoles());
    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Gets roles.
   */
  @Test
  void getRoles() {
    String value = UUID.randomUUID().toString();
    ActuatorSecurityProperties expected = new ActuatorSecurityProperties();
    expected.setRoles(Collections.singletonList(value));

    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    actual.setRoles(Collections.singletonList(value));

    assertEquals(expected.getRoles(), actual.getRoles());
    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Gets ip addresses.
   */
  @Test
  void getIpAddresses() {
    String value = UUID.randomUUID().toString();
    ActuatorSecurityProperties expected = new ActuatorSecurityProperties();
    expected.setIpAddresses(Collections.singletonList(value));

    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    actual.setIpAddresses(Collections.singletonList(value));

    assertEquals(expected.getIpAddresses(), actual.getIpAddresses());
    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Is enable jwt support.
   */
  @Test
  void isEnableJwtSupport() {
    ActuatorSecurityProperties expected = new ActuatorSecurityProperties();
    expected.setEnableJwtSupport(true);
    assertTrue(expected.isEnableJwtSupport());

    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    actual.setEnableJwtSupport(true);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));

    assertNotEquals(expected, null);
    assertNotEquals(expected, new Object());
  }

  /**
   * Gets jwk uri set.
   */
  @Test
  void getJwkUriSet() {
    String value = UUID.randomUUID().toString();
    ActuatorSecurityProperties expected = new ActuatorSecurityProperties();
    expected.setJwkUriSet(value);

    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    actual.setJwkUriSet(value);

    assertEquals(value, actual.getJwkUriSet());
    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Gets jws algorithm.
   */
  @Test
  void getJwsAlgorithm() {
    String value = UUID.randomUUID().toString();
    ActuatorSecurityProperties expected = new ActuatorSecurityProperties();
    expected.setJwsAlgorithm(value);

    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    actual.setJwsAlgorithm(value);

    assertEquals(value, actual.getJwsAlgorithm());
    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Gets issuer uri.
   */
  @Test
  void getIssuerUri() {
    String value = UUID.randomUUID().toString();
    ActuatorSecurityProperties expected = new ActuatorSecurityProperties();
    expected.setIssuerUri(value);

    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    actual.setIssuerUri(value);

    assertEquals(value, actual.getIssuerUri());
    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Gets roles json path.
   */
  @Test
  void getRolesJsonPath() {
    String value = UUID.randomUUID().toString();
    ActuatorSecurityProperties expected = new ActuatorSecurityProperties();
    expected.setRolesJsonPath(value);

    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    actual.setRolesJsonPath(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Is roles value list.
   */
  @Test
  void isRolesValueList() {
    ActuatorSecurityProperties expected = new ActuatorSecurityProperties();
    expected.setRolesValueList(true);
    assertTrue(expected.isRolesValueList());

    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    actual.setRolesValueList(true);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));
  }

  /**
   * Gets roles value separator.
   */
  @Test
  void getRolesValueSeparator() {
    String value = UUID.randomUUID().toString();
    ActuatorSecurityProperties expected = new ActuatorSecurityProperties();
    expected.setRolesValueSeparator(value);

    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    actual.setRolesValueSeparator(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Gets name json path.
   */
  @Test
  void getNameJsonPath() {
    String value = UUID.randomUUID().toString();
    ActuatorSecurityProperties expected = new ActuatorSecurityProperties();
    expected.setNameJsonPath(value);

    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    actual.setNameJsonPath(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Gets password flow.
   */
  @Test
  void getPasswordFlow() {
    ActuatorSecurityProperties expected = new ActuatorSecurityProperties();
    expected.getPasswordFlow().setClientId("1234");
    expected.getPasswordFlow().setClientSecret("5678");
    expected.getPasswordFlow().setTokenEndpoint("http://localhost/token");

    ActuatorSecurityProperties actual = new ActuatorSecurityProperties();
    actual.getPasswordFlow().setClientId("1234");
    actual.getPasswordFlow().setClientSecret("5678");
    actual.getPasswordFlow().setTokenEndpoint("http://localhost/token");

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("1234"));
    assertFalse(expected.toString().contains("3456"));
    assertTrue(expected.toString().contains("http://localhost/token"));

    assertNotEquals(expected.getPasswordFlow(), null);
    assertNotEquals(expected.getPasswordFlow(), new Object());

    PasswordFlowProperties passwordFlow = expected.getPasswordFlow()
        .toPasswordFlowProperties("username", "password");
    assertNotNull(passwordFlow);
    assertEquals(expected.getPasswordFlow().getClientId(), passwordFlow.getClientId());
    assertEquals(expected.getPasswordFlow().getClientSecret(), passwordFlow.getClientSecret());
    assertEquals(expected.getPasswordFlow().getTokenEndpoint(), passwordFlow.getTokenEndpoint());
  }
}