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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bremersee.security.authentication.AuthProperties.ClientCredentialsFlow;
import org.bremersee.security.authentication.AuthProperties.EurekaAccessProperties;
import org.bremersee.security.authentication.AuthProperties.PasswordFlow;
import org.bremersee.security.authentication.AuthProperties.PathMatcherProperties;
import org.bremersee.security.authentication.AuthProperties.SimpleUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * The authentication properties test.
 *
 * @author Christian Bremer
 */
class AuthPropertiesTest {

  /**
   * Get resource server.
   */
  @Test
  void getResourceServer() {
    AuthProperties expected = new AuthProperties();
    expected.setResourceServer(AutoSecurityMode.AUTO);
    assertEquals(AutoSecurityMode.AUTO, expected.getResourceServer());

    AuthProperties actual = new AuthProperties();
    actual.setResourceServer(AutoSecurityMode.AUTO);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(AutoSecurityMode.AUTO.name()));

    assertNotEquals(expected, null);
    assertNotEquals(expected, new Object());

    assertEquals(expected.hashCode(), actual.hashCode());
    //noinspection SimplifiableJUnitAssertion
    assertTrue(actual.equals(expected));
  }

  /**
   * Get resource server order.
   */
  @Test
  void getResourceServerOrder() {
    AuthProperties expected = new AuthProperties();
    expected.setResourceServerOrder(123);
    assertEquals(123, expected.getResourceServerOrder());

    AuthProperties actual = new AuthProperties();
    actual.setResourceServerOrder(123);

    assertEquals(expected, actual);
  }

  /**
   * Gets role prefix.
   */
  @Test
  void getRolePrefix() {
    String value = UUID.randomUUID().toString();
    AuthProperties expected = new AuthProperties();
    expected.setRolePrefix(value);

    AuthProperties actual = new AuthProperties();
    actual.setRolePrefix(value);

    assertEquals(value, actual.getRolePrefix());
    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Ensure role prefix.
   */
  @Test
  void ensureRolePrefix() {
    AuthProperties properties = new AuthProperties();
    properties.setRolePrefix("FOO_");
    assertEquals("FOO_BAR", properties.ensureRolePrefix("BAR"));
    assertEquals("FOO_BAR", properties.ensureRolePrefix("FOO_BAR"));

    properties.setRolePrefix("");
    assertEquals("BAR", properties.ensureRolePrefix("BAR"));
    assertEquals("FOO_BAR", properties.ensureRolePrefix("FOO_BAR"));
  }

  /**
   * Gets roles json path.
   */
  @Test
  void getRolesJsonPath() {
    String value = UUID.randomUUID().toString();
    AuthProperties expected = new AuthProperties();
    expected.setRolesJsonPath(value);

    AuthProperties actual = new AuthProperties();
    actual.setRolesJsonPath(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Is roles value list.
   */
  @Test
  void isRolesValueList() {
    AuthProperties expected = new AuthProperties();
    expected.setRolesValueList(true);
    assertTrue(expected.isRolesValueList());

    AuthProperties actual = new AuthProperties();
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
    AuthProperties expected = new AuthProperties();
    expected.setRolesValueSeparator(value);

    AuthProperties actual = new AuthProperties();
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
    AuthProperties expected = new AuthProperties();
    expected.setNameJsonPath(value);

    AuthProperties actual = new AuthProperties();
    actual.setNameJsonPath(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Gets role definitions.
   */
  @Test
  void getRoleDefinitions() {
    String key = UUID.randomUUID().toString();
    List<String> value = Collections.singletonList(UUID.randomUUID().toString());
    AuthProperties expected = new AuthProperties();
    expected.setRoleDefinitions(Collections.singletonMap(key, value));

    AuthProperties actual = new AuthProperties();
    actual.setRoleDefinitions(Collections.singletonMap(key, value));
    assertEquals(Collections.singletonMap(key, value), actual.getRoleDefinitions());

    assertEquals(expected, actual);
  }

  /**
   * Gets ip definitions.
   */
  @Test
  void getIpDefinitions() {
    String key = UUID.randomUUID().toString();
    List<String> value = Collections.singletonList(UUID.randomUUID().toString());
    AuthProperties expected = new AuthProperties();
    expected.setIpDefinitions(Collections.singletonMap(key, value));

    AuthProperties actual = new AuthProperties();
    actual.setIpDefinitions(Collections.singletonMap(key, value));
    assertEquals(Collections.singletonMap(key, value), actual.getIpDefinitions());

    assertEquals(expected, actual);
  }

  /**
   * Gets path matchers.
   */
  @Test
  void getPathMatchers() {
    List<PathMatcherProperties> value = Collections.singletonList(new PathMatcherProperties());
    AuthProperties expected = new AuthProperties();
    expected.setPathMatchers(value);

    AuthProperties actual = new AuthProperties();
    actual.setPathMatchers(value);
    assertEquals(value, actual.getPathMatchers());

    assertEquals(expected, actual);
  }

  /**
   * Path matchers.
   */
  @Test
  void pathMatchers() {
    AuthProperties properties = new AuthProperties();
    PathMatcherProperties defaults = new PathMatcherProperties();
    defaults.setAccessMode(properties.getAnyAccessMode());
    Set<PathMatcherProperties> actual = properties.pathMatchers();
    assertNotNull(actual);
    assertEquals(1, actual.size());
    assertTrue(actual.contains(defaults));

    properties.setPathMatchers(Collections.singletonList(defaults));
    actual = properties.pathMatchers();
    assertNotNull(actual);
    assertEquals(1, actual.size());
    assertTrue(actual.contains(defaults));
  }

  /**
   * Gets any access mode.
   */
  @Test
  void getAnyAccessMode() {
    AccessMode value = AccessMode.PERMIT_ALL;
    AuthProperties expected = new AuthProperties();
    expected.setAnyAccessMode(value);

    AuthProperties actual = new AuthProperties();
    actual.setAnyAccessMode(value);
    assertEquals(value, actual.getAnyAccessMode());

    assertEquals(expected, actual);

    assertTrue(actual.toString().contains(value.name()));
  }

  /**
   * Gets eureka.
   */
  @Test
  void getEureka() {
    EurekaAccessProperties value = new EurekaAccessProperties();
    AuthProperties expected = new AuthProperties();
    expected.setEureka(value);

    AuthProperties actual = new AuthProperties();
    actual.setEureka(value);

    assertEquals(value, actual.getEureka());
    assertEquals(expected, actual);
  }

  /**
   * Gets password flow.
   */
  @Test
  void getPasswordFlow() {
    PasswordFlow value = new PasswordFlow();
    AuthProperties expected = new AuthProperties();
    expected.setPasswordFlow(value);

    AuthProperties actual = new AuthProperties();
    actual.setPasswordFlow(value);

    assertEquals(value, actual.getPasswordFlow());
    assertEquals(expected, actual);
  }

  /**
   * Gets client credential flow.
   */
  @Test
  void getClientCredentialFlow() {
    ClientCredentialsFlow value = new ClientCredentialsFlow();
    AuthProperties expected = new AuthProperties();
    expected.setClientCredentialsFlow(value);

    AuthProperties actual = new AuthProperties();
    actual.setClientCredentialsFlow(value);

    assertEquals(value, actual.getClientCredentialsFlow());
    assertEquals(expected, actual);
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

    AuthProperties expectedProperties = new AuthProperties();
    expectedProperties.setInMemoryUsers(Collections.singletonList(expected));

    AuthProperties actualProperties = new AuthProperties();
    actualProperties.setInMemoryUsers(Collections.singletonList(expected));

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

    AuthProperties properties = new AuthProperties();
    properties.getInMemoryUsers().add(su0);
    properties.getInMemoryUsers().add(su1);

    UserDetails[] details = properties.buildBasicAuthUserDetails(null);
    assertNotNull(details);
    assertEquals(properties.getInMemoryUsers().size(), details.length);
    for (UserDetails userDetails : details) {
      Optional<SimpleUser> su = properties.getInMemoryUsers().stream()
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
