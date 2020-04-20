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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.bremersee.security.authentication.AuthenticationProperties.SimpleUser;
import org.bremersee.security.core.AuthorityConstants;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * The authentication properties test.
 *
 * @author Christian Bremer
 */
class AuthenticationPropertiesTest {

  /**
   * Is enable jwt support.
   */
  @Test
  void isEnableJwtSupport() {
    AuthenticationProperties expected = new AuthenticationProperties();
    expected.setEnableJwtSupport(true);
    assertTrue(expected.isEnableJwtSupport());

    AuthenticationProperties actual = new AuthenticationProperties();
    actual.setEnableJwtSupport(true);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));

    assertNotEquals(expected, null);
    assertNotEquals(expected, new Object());
  }

  /**
   * Gets roles json path.
   */
  @Test
  void getRolesJsonPath() {
    String value = UUID.randomUUID().toString();
    AuthenticationProperties expected = new AuthenticationProperties();
    expected.setRolesJsonPath(value);

    AuthenticationProperties actual = new AuthenticationProperties();
    actual.setRolesJsonPath(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Is roles value list.
   */
  @Test
  void isRolesValueList() {
    AuthenticationProperties expected = new AuthenticationProperties();
    expected.setRolesValueList(true);
    assertTrue(expected.isRolesValueList());

    AuthenticationProperties actual = new AuthenticationProperties();
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
    AuthenticationProperties expected = new AuthenticationProperties();
    expected.setRolesValueSeparator(value);

    AuthenticationProperties actual = new AuthenticationProperties();
    actual.setRolesValueSeparator(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Gets role prefix.
   */
  @Test
  void getRolePrefix() {
    String value = UUID.randomUUID().toString();
    AuthenticationProperties expected = new AuthenticationProperties();
    expected.setRolePrefix(value);

    AuthenticationProperties actual = new AuthenticationProperties();
    actual.setRolePrefix(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Gets name json path.
   */
  @Test
  void getNameJsonPath() {
    String value = UUID.randomUUID().toString();
    AuthenticationProperties expected = new AuthenticationProperties();
    expected.setNameJsonPath(value);

    AuthenticationProperties actual = new AuthenticationProperties();
    actual.setNameJsonPath(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Gets application.
   */
  @Test
  void getApplication() {
    AuthenticationProperties expected = new AuthenticationProperties();
    expected.getApplication().setIpAddresses(Arrays.asList("127.0.0.1/32", "::1"));
    expected.getApplication().setAdminRoles(Arrays.asList("ROLE_ADMIN", "ROLE_SUPERUSER"));
    expected.getApplication().setUserRoles(Arrays.asList("ROLE_USER", "ROLE_LOCAL_USER"));
    expected.getApplication().setDefaultAccessExpression("isFullyAuthenticated()");

    AuthenticationProperties actual = new AuthenticationProperties();
    actual.getApplication().setIpAddresses(Arrays.asList("127.0.0.1/32", "::1"));
    actual.getApplication().setAdminRoles(Arrays.asList("ROLE_ADMIN", "ROLE_SUPERUSER"));
    actual.getApplication().setUserRoles(Arrays.asList("ROLE_USER", "ROLE_LOCAL_USER"));
    actual.getApplication().setDefaultAccessExpression("isFullyAuthenticated()");

    assertEquals(expected, actual);
    assertNotEquals(expected, null);
    assertNotEquals(expected, new Object());
    assertTrue(expected.toString().contains("ROLE_USER"));

    assertTrue(expected.getApplication().adminRolesOrDefaults("ADMIN")
        .contains("ROLE_SUPERUSER"));
    assertTrue(expected.getApplication().userRolesOrDefaults("USER")
        .contains("ROLE_USER"));
    assertFalse(expected.getApplication().adminRolesOrDefaults("ADMIN")
        .contains("ADMIN"));
    assertFalse(expected.getApplication().userRolesOrDefaults("USER")
        .contains("USER"));

    String expectedExpr = "hasAuthority('ROLE_ADMIN')"
        + " or hasAuthority('ROLE_SUPERUSER')"
        + " or hasAuthority('ROLE_USER')"
        + " or hasAuthority('ROLE_LOCAL_USER')"
        + " or hasIpAddress('127.0.0.1/32')"
        + " or hasIpAddress('::1')";
    String expr = actual.getApplication().buildAccessExpression(false, true, true, true);
    assertEquals(expectedExpr, expr);

    expectedExpr = "hasAuthority('ROLE_ADMIN')"
        + " or hasAuthority('ROLE_SUPERUSER')"
        + " or hasAuthority('ROLE_USER')"
        + " or hasAuthority('ROLE_LOCAL_USER')";
    expr = actual.getApplication().buildAccessExpression(false, false, true, true);
    assertEquals(expectedExpr, expr);

    expectedExpr = "hasAuthority('ROLE_ADMIN')"
        + " or hasAuthority('ROLE_SUPERUSER')";
    expr = actual.getApplication().buildAccessExpression(false, false, false, true);
    assertEquals(expectedExpr, expr);

    expectedExpr = "hasIpAddress('127.0.0.1/32')"
        + " or hasIpAddress('::1')"
        + " or isFullyAuthenticated()";
    expr = actual.getApplication().buildAccessExpression(true, true, false, false);
    assertEquals(expectedExpr, expr);

    expectedExpr = "isFullyAuthenticated()";
    expr = actual.getApplication().buildAccessExpression(false, false, false, false);
    assertEquals(expectedExpr, expr);

    expected.getApplication().setAdminRoles(new ArrayList<>());
    expected.getApplication().setUserRoles(new ArrayList<>());
    assertTrue(expected.getApplication().adminRolesOrDefaults("ADMIN")
        .contains("ADMIN"));
    assertTrue(expected.getApplication().userRolesOrDefaults("USER")
        .contains("USER"));
    assertFalse(expected.getApplication().adminRolesOrDefaults("ADMIN")
        .contains("ROLE_SUPERUSER"));
    assertFalse(expected.getApplication().userRolesOrDefaults("USER")
        .contains("ROLE_USER"));
  }

  /**
   * Gets actuator.
   */
  @Test
  void getActuator() {
    AuthenticationProperties expected = new AuthenticationProperties();
    expected.getActuator().setIpAddresses(Arrays.asList("127.0.0.1/32", "::1"));
    expected.getActuator().setRoles(Arrays.asList("ROLE_SUPER_USER", "ROLE_ACTUATOR"));
    expected.getActuator().setAdminRoles(Arrays.asList("ROLE_A", "ROLE_B"));

    AuthenticationProperties actual = new AuthenticationProperties();
    actual.getActuator().setIpAddresses(Arrays.asList("127.0.0.1/32", "::1"));
    actual.getActuator().setRoles(Arrays.asList("ROLE_SUPER_USER", "ROLE_ACTUATOR"));
    actual.getActuator().setAdminRoles(Arrays.asList("ROLE_A", "ROLE_B"));

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("127.0.0.1/32"));
    assertTrue(expected.toString().contains("::1"));
    assertTrue(expected.toString().contains("ROLE_SUPER_USER"));
    assertTrue(expected.toString().contains("ROLE_ACTUATOR"));
    assertTrue(actual.getActuator().getAdminRoles().contains("ROLE_A"));
    assertTrue(actual.getActuator().getAdminRoles().contains("ROLE_B"));

    String value = "hasAuthority('ROLE_ACTUATOR') or hasAuthority('ROLE_SUPER_USER')"
        + " or hasIpAddress('127.0.0.1/32') or hasIpAddress('::1')";
    assertEquals(value, expected.getActuator().buildAccessExpression());
    assertNotEquals(expected.getActuator(), null);
    assertNotEquals(expected.getActuator(), new Object());

    expected = new AuthenticationProperties();
    expected.getActuator().setIpAddresses(Arrays.asList("127.0.0.1/32", "::1"));
    expected.getActuator().setRoles(Collections.emptyList());

    value = "hasAuthority('ROLE_ACTUATOR') or hasAuthority('ROLE_ADMIN')"
        + " or hasIpAddress('127.0.0.1/32') or hasIpAddress('::1')";
    assertEquals(value, expected.getActuator().buildAccessExpression());

    assertEquals(
        "hasAuthority('ROLE_A') or hasAuthority('ROLE_B')",
        actual.getActuator().buildAdminAccessExpression());
  }

  /**
   * Gets eureka.
   */
  @Test
  void getEureka() {
    AuthenticationProperties expected = new AuthenticationProperties();
    String pass = UUID.randomUUID().toString();
    expected.getEureka().setUsername("qwertz");
    expected.getEureka().setPassword(pass);
    AuthenticationProperties actual = new AuthenticationProperties();
    actual.getEureka().setUsername("qwertz");
    actual.getEureka().setPassword(pass);
    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("qwertz"));
    assertFalse(expected.toString().contains(pass));
    assertEquals("qwertz", expected.getEureka().getUsername());
    assertEquals(pass, expected.getEureka().getPassword());
    assertEquals(AuthorityConstants.EUREKA_ROLE_NAME, expected.getEureka().getRole());
    assertEquals(
        "hasAuthority('" + AuthorityConstants.EUREKA_ROLE_NAME + "')",
        expected.getEureka().buildAccessExpression());

    UserDetails[] userDetails = expected.getEureka().buildBasicAuthUserDetails();
    assertNotNull(userDetails);
    assertEquals(1, userDetails.length);
    UserDetails user = userDetails[0];
    assertEquals("qwertz", user.getUsername());
    assertNotNull(user.getPassword());
    assertTrue(user.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch(AuthorityConstants.EUREKA_ROLE_NAME::equals));

    expected.getEureka().setRole("");
    expected.getEureka().setIpAddresses(Collections.singletonList("127.0.0.1"));
    assertEquals(
        "isAuthenticated() or hasIpAddress('127.0.0.1')",
        expected.getEureka().buildAccessExpression());
  }

  /**
   * Gets password flow.
   */
  @Test
  void getPasswordFlow() {
    AuthenticationProperties expected = new AuthenticationProperties();
    expected.getPasswordFlow().setClientId("1234");
    expected.getPasswordFlow().setClientSecret("5678");
    expected.getPasswordFlow().setSystemPassword("9012");
    expected.getPasswordFlow().setSystemUsername("3456");
    expected.getPasswordFlow().setTokenEndpoint("http://localhost/token");

    AuthenticationProperties actual = new AuthenticationProperties();
    actual.getPasswordFlow().setClientId("1234");
    actual.getPasswordFlow().setClientSecret("5678");
    actual.getPasswordFlow().setSystemPassword("9012");
    actual.getPasswordFlow().setSystemUsername("3456");
    actual.getPasswordFlow().setTokenEndpoint("http://localhost/token");

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("1234"));
    // assertTrue(expected.toString().contains("5678"));
    // assertTrue(expected.toString().contains("9012"));
    assertTrue(expected.toString().contains("3456"));
    assertTrue(expected.toString().contains("http://localhost/token"));

    assertNotEquals(expected.getPasswordFlow(), null);
    assertNotEquals(expected.getPasswordFlow(), new Object());

    PasswordFlowProperties passwordFlow = expected.getPasswordFlow().toProperties();
    assertNotNull(passwordFlow);
    assertEquals(expected.getPasswordFlow().getClientId(), passwordFlow.getClientId());
    assertEquals(expected.getPasswordFlow().getClientSecret(), passwordFlow.getClientSecret());
    assertEquals(expected.getPasswordFlow().getSystemPassword(), passwordFlow.getPassword());
    assertEquals(expected.getPasswordFlow().getSystemUsername(), passwordFlow.getUsername());
    assertEquals(expected.getPasswordFlow().getTokenEndpoint(), passwordFlow.getTokenEndpoint());
  }

  /**
   * Gets client credential flow.
   */
  @Test
  void getClientCredentialFlow() {
    AuthenticationProperties expected = new AuthenticationProperties();
    expected.getClientCredentialsFlow().setClientId("1234");
    expected.getClientCredentialsFlow().setClientSecret("5678");
    expected.getClientCredentialsFlow().setTokenEndpoint("http://localhost/token");

    AuthenticationProperties actual = new AuthenticationProperties();
    actual.getClientCredentialsFlow().setClientId("1234");
    actual.getClientCredentialsFlow().setClientSecret("5678");
    actual.getClientCredentialsFlow().setTokenEndpoint("http://localhost/token");

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("1234"));
    assertTrue(expected.toString().contains("http://localhost/token"));

    assertNotEquals(expected.getClientCredentialsFlow(), null);
    assertNotEquals(expected.getClientCredentialsFlow(), new Object());

    ClientCredentialsFlowProperties properties = expected.getClientCredentialsFlow().toProperties();
    assertNotNull(properties);
    assertEquals(expected.getClientCredentialsFlow().getClientId(), properties.getClientId());
    assertEquals(expected.getClientCredentialsFlow().getClientSecret(),
        properties.getClientSecret());
    assertEquals(expected.getClientCredentialsFlow().getTokenEndpoint(),
        properties.getTokenEndpoint());
  }

  /**
   * Gets basic auth users.
   */
  @Test
  void getBasicAuthUsers() {
    SimpleUser expected = new SimpleUser();
    expected.setAuthorities(Arrays.asList("ROLE_USER", "ROLE_LOCAL_USER"));
    expected.setName("1234");
    expected.setPassword("5678");

    SimpleUser actual = new SimpleUser();
    actual.setAuthorities(Arrays.asList("ROLE_USER", "ROLE_LOCAL_USER"));
    actual.setName("1234");
    actual.setPassword("5678");

    assertEquals(expected, actual);
    assertNotEquals(expected, null);
    assertNotEquals(expected, new Object());
    assertTrue(expected.toString().contains("ROLE_LOCAL_USER"));
    assertTrue(expected.toString().contains("1234"));

    AuthenticationProperties expectedProperties = new AuthenticationProperties();
    expectedProperties.setBasicAuthUsers(Collections.singletonList(expected));

    AuthenticationProperties actualProperties = new AuthenticationProperties();
    actualProperties.setBasicAuthUsers(Collections.singletonList(expected));

    assertEquals(expectedProperties, actualProperties);
    assertTrue(expectedProperties.toString().contains("ROLE_LOCAL_USER"));
    assertTrue(expectedProperties.toString().contains("1234"));
  }

  /**
   * Build basic auth user details.
   */
  @Test
  void buildBasicAuthUserDetails() {
    SimpleUser su0 = new SimpleUser();
    su0.setName("admin");
    su0.setPassword("1234");
    su0.setAuthorities(Arrays.asList("ROLE_SUPER_USER", "ROLE_NORMAL_USER"));
    SimpleUser su1 = new SimpleUser();
    su1.setName("anna");
    su1.setPassword("5678");
    su1.setAuthorities(Arrays.asList("ROLE_DEVELOPER", "ROLE_NORMAL_USER"));

    AuthenticationProperties properties = new AuthenticationProperties();
    properties.getBasicAuthUsers().add(su0);
    properties.getBasicAuthUsers().add(su1);

    UserDetails[] details = properties.buildBasicAuthUserDetails();
    assertNotNull(details);
    assertEquals(properties.getBasicAuthUsers().size(), details.length);
    for (UserDetails userDetails : details) {
      Optional<SimpleUser> su = properties.getBasicAuthUsers().stream()
          .filter(simpleUser -> simpleUser.getName().equals(userDetails.getUsername()))
          .findAny();
      assertTrue(su.isPresent());
      for (String authority : su.get().getAuthorities()) {
        assertTrue(userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(a -> a.equals(authority)));
      }
    }
  }

}