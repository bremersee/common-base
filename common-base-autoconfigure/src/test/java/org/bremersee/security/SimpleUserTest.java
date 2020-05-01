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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.UUID;
import org.bremersee.security.SecurityProperties.AuthenticationProperties.SimpleUser;
import org.bremersee.security.core.AuthorityConstants;
import org.junit.jupiter.api.Test;

/**
 * The simple user test.
 *
 * @author Christian Bremer
 */
class SimpleUserTest {

  /**
   * Gets name.
   */
  @Test
  void getName() {
    String value = UUID.randomUUID().toString();
    SimpleUser expected = new SimpleUser();
    expected.setName(value);
    SimpleUser actual = new SimpleUser();
    actual.setName(value);
    assertEquals(value, actual.getName());

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
    SimpleUser expected = new SimpleUser();
    expected.setPassword(value);
    SimpleUser actual = new SimpleUser();
    actual.setPassword(value);
    assertEquals(value, actual.getPassword());

    assertEquals(expected, actual);

    assertFalse(actual.toString().contains(value));
  }

  /**
   * Gets authorities.
   */
  @Test
  void getAuthorities() {
    String value = UUID.randomUUID().toString();
    SimpleUser expected = new SimpleUser();
    expected.setAuthorities(Collections.singletonList(value));
    SimpleUser actual = new SimpleUser();
    actual.setAuthorities(Collections.singletonList(value));
    assertEquals(Collections.singletonList(value), actual.getAuthorities());

    assertEquals(expected, actual);
    assertNotEquals(actual, null);
    assertNotEquals(actual, new Object());

    assertTrue(actual.toString().contains(value));
  }

  /**
   * Build authorities.
   */
  @Test
  void buildAuthorities() {
    SimpleUser user = new SimpleUser();
    String[] authorities = user.buildAuthorities();
    assertNotNull(authorities);
    assertEquals(1, authorities.length);
    assertEquals(AuthorityConstants.USER_ROLE_NAME, authorities[0]);

    String value = UUID.randomUUID().toString();
    user.setAuthorities(Collections.singletonList(value));
    authorities = user.buildAuthorities();
    assertNotNull(authorities);
    assertEquals(1, authorities.length);
    assertEquals(value, authorities[0]);
  }

}
