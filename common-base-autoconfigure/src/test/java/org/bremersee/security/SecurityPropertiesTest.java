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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bremersee.security.SecurityProperties.AuthenticationProperties;
import org.bremersee.security.SecurityProperties.CorsProperties;
import org.junit.jupiter.api.Test;

/**
 * The security properties test.
 *
 * @author Christian Bremer
 */
class SecurityPropertiesTest {

  /**
   * Gets cors.
   */
  @Test
  void getCors() {
    CorsProperties corsProperties = new CorsProperties();
    SecurityProperties a = new SecurityProperties();
    a.setCors(corsProperties);
    SecurityProperties b = new SecurityProperties();
    b.setCors(corsProperties);
    assertEquals(a, b);
    assertEquals(a, a);
    assertNotEquals(a, null);
    assertNotEquals(a, new Object());
    assertTrue(a.toString().contains(corsProperties.toString()));
  }

  /**
   * Gets authentication.
   */
  @Test
  void getAuthentication() {
    AuthenticationProperties authenticationProperties = new AuthenticationProperties();
    SecurityProperties a = new SecurityProperties();
    a.setAuthentication(authenticationProperties);
    SecurityProperties b = new SecurityProperties();
    b.setAuthentication(authenticationProperties);
    assertEquals(a, b);
    assertEquals(a, a);
    assertNotEquals(a, null);
    assertNotEquals(a, new Object());
    assertTrue(a.toString().contains(authenticationProperties.toString()));
  }
}