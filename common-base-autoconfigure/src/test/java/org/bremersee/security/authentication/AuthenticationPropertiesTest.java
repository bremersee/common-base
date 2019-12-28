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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Optional;
import org.bremersee.security.authentication.AuthenticationProperties.SimpleUser;
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

  /**
   * Gets actuator.
   */
  @Test
  void getActuator() {
    String expected = "hasAuthority('ROLE_ACTUATOR') or hasAuthority('ROLE_SUPER_USER')"
        + " or hasIpAddress('127.0.0.1/32') or hasIpAddress('::1')";
    AuthenticationProperties properties = new AuthenticationProperties();
    properties.getActuator().setIpAddresses(Arrays.asList("127.0.0.1/32", "::1"));
    properties.getActuator().setRoles(Arrays.asList("ROLE_SUPER_USER", "ROLE_ACTUATOR"));
    assertEquals(expected, properties.getActuator().buildAccessExpression());
  }

  /**
   * Gets password flow.
   */
  @Test
  void getPasswordFlow() {
    AuthenticationProperties properties = new AuthenticationProperties();
    properties.getPasswordFlow().setClientId("clientId");
    properties.getPasswordFlow().setClientSecret("clientSecret");
    properties.getPasswordFlow().setSystemPassword("systemPassword");
    properties.getPasswordFlow().setSystemUsername("systemUser");
    properties.getPasswordFlow().setTokenEndpoint("http://localhost/token");
    PasswordFlowProperties passwordFlow = properties.getPasswordFlow().toProperties();
    assertNotNull(passwordFlow);
    assertEquals(properties.getPasswordFlow().getClientId(), passwordFlow.getClientId());
    assertEquals(properties.getPasswordFlow().getClientSecret(), passwordFlow.getClientSecret());
    assertEquals(properties.getPasswordFlow().getSystemPassword(), passwordFlow.getPassword());
    assertEquals(properties.getPasswordFlow().getSystemUsername(), passwordFlow.getUsername());
    assertEquals(properties.getPasswordFlow().getTokenEndpoint(), passwordFlow.getTokenEndpoint());
  }
}