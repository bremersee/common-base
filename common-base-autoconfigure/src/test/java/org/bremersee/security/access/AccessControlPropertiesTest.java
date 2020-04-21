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

package org.bremersee.security.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import org.bremersee.security.core.AuthorityConstants;
import org.junit.jupiter.api.Test;

/**
 * The access control properties test.
 *
 * @author Christian Bremer
 */
class AccessControlPropertiesTest {

  /**
   * Gets default admin roles.
   */
  @Test
  void getDefaultAdminRoles() {
    AccessControlProperties properties = new AccessControlProperties();
    assertNotNull(properties.getAdminRoles());
    assertTrue(properties.getAdminRoles().contains(AuthorityConstants.ADMIN_ROLE_NAME));

    assertEquals(properties, properties);
    assertEquals(properties, new AccessControlProperties());
    assertNotEquals(properties, null);
    assertNotEquals(properties, new Object());
    assertEquals(properties.hashCode(), new AccessControlProperties().hashCode());
    assertEquals(properties.toString(), new AccessControlProperties().toString());
  }

  /**
   * Gets admin roles.
   */
  @Test
  void getAdminRoles() {
    AccessControlProperties properties = new AccessControlProperties();
    properties.setAdminRoles(Collections.singleton(AuthorityConstants.ACTUATOR_ADMIN_ROLE_NAME));
    assertEquals(
        Collections.singleton(AuthorityConstants.ACTUATOR_ADMIN_ROLE_NAME),
        properties.getAdminRoles());
  }

  /**
   * Is switch admin access.
   */
  @Test
  void isSwitchAdminAccess() {
    AccessControlProperties properties = new AccessControlProperties();
    assertTrue(properties.isSwitchAdminAccess()); // default

    properties.setSwitchAdminAccess(false);
    assertFalse(properties.isSwitchAdminAccess());
  }

  /**
   * Is return null.
   */
  @Test
  void isReturnNull() {
    AccessControlProperties properties = new AccessControlProperties();
    assertFalse(properties.isReturnNull()); // default

    properties.setReturnNull(true);
    assertTrue(properties.isReturnNull());
  }

  /**
   * Gets default permissions.
   */
  @Test
  void getDefaultPermissions() {
    AccessControlProperties properties = new AccessControlProperties();
    assertNotNull(properties.getDefaultPermissions());
    assertTrue(
        properties.getDefaultPermissions().containsAll(Arrays.asList(PermissionConstants.ALL)));

    properties.setDefaultPermissions(Collections.singleton(PermissionConstants.READ));
    assertEquals(
        Collections.singleton(PermissionConstants.READ),
        properties.getDefaultPermissions());
  }
}