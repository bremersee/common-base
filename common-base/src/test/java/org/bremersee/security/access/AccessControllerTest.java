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

package org.bremersee.security.access;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import org.bremersee.common.model.AccessControlEntry;
import org.bremersee.common.model.AccessControlList;
import org.junit.jupiter.api.Test;

/**
 * The access controller test.
 *
 * @author Christian Bremer
 */
class AccessControllerTest {

  /**
   * From.
   */
  @Test
  void from() {
    assertNotNull(AccessController.from((Acl<?>) null));
    assertNotNull(AccessController.from(new AclImpl("owner", null)));

    assertNotNull(AccessController.from((AccessControlList) null));
    assertNotNull(AccessController.from(AccessControlList.builder().build()));
  }

  /**
   * Has any permission.
   */
  @Test
  void hasAnyPermission() {
    assertFalse(AccessController
        .from(new AclImpl("owner", new HashMap<>()))
        .hasAnyPermission("user", Collections.emptySet(), Collections.emptySet()));

    assertFalse(AccessController
        .from(new AclImpl("owner", new HashMap<>()))
        .hasAnyPermission("user", null, null));

    assertFalse(AccessController
        .from(new AclImpl("owner", new HashMap<>()))
        .hasAnyPermission("user", Collections.emptySet(), null));

    AccessControlList acl = AccessControlList
        .builder()
        .owner("owner")
        .entries(Arrays.asList(
            AccessControlEntry
                .builder()
                .permission("write")
                .groups(Collections.singletonList("group"))
                .roles(Collections.singletonList("role"))
                .users(Collections.singletonList("user"))
                .build(),
            AccessControlEntry
                .builder()
                .permission("read")
                .groups(new ArrayList<>())
                .roles(new ArrayList<>())
                .users(new ArrayList<>())
                .guest(true)
                .build()
        ))
        .build();
    assertFalse(
        AccessController
            .from(acl)
            .hasPermission(
                "test",
                null,
                null,
                null));
    assertFalse(
        AccessController
            .from((AccessControlList) null)
            .hasPermission(
                "test",
                null,
                null,
                "read"));
    assertTrue(
        AccessController
            .from(acl)
            .hasAnyPermission(
                "test",
                Collections.emptyList(),
                Collections.emptyList(),
                "read"));
    assertTrue(
        AccessController
            .from(acl)
            .hasAnyPermission(
                "user",
                Collections.emptyList(),
                Collections.emptyList(),
                "write"));
    assertTrue(
        AccessController
            .from(acl)
            .hasAnyPermission(
                "test",
                Collections.singletonList("role"),
                Collections.emptyList(),
                "write"));
    assertTrue(
        AccessController
            .from(acl)
            .hasAnyPermission(
                "test",
                Collections.emptyList(),
                Collections.singleton("group"),
                "write"));
    assertTrue(
        AccessController
            .from(acl)
            .hasAnyPermission(
                "owner",
                Collections.emptyList(),
                Collections.emptyList(),
                "delete"));
    assertFalse(
        AccessController
            .from(acl)
            .hasAnyPermission(
                "test",
                Collections.emptyList(),
                Collections.emptyList(),
                "write"));
    assertFalse(
        AccessController
            .from(acl)
            .hasAnyPermission(
                "user",
                Collections.emptyList(),
                Collections.emptyList(),
                "delete"));
  }

  /**
   * Has all permissions.
   */
  @Test
  void hasAllPermissions() {
    assertFalse(AccessController
        .from(new AclImpl("owner", new HashMap<>()))
        .hasAllPermissions("user", Collections.emptySet(), Collections.emptySet()));

    assertFalse(AccessController
        .from(new AclImpl("owner", new HashMap<>()))
        .hasAllPermissions("user", null, null));

    assertFalse(AccessController
        .from(new AclImpl("owner", new HashMap<>()))
        .hasAllPermissions("user", Collections.emptySet(), null));

    AccessControlList acl = AccessControlList
        .builder()
        .owner("owner")
        .entries(Arrays.asList(
            AccessControlEntry
                .builder()
                .permission("write")
                .groups(Collections.singletonList("group"))
                .roles(Collections.singletonList("role"))
                .users(Collections.singletonList("user"))
                .build(),
            AccessControlEntry
                .builder()
                .permission("read")
                .groups(new ArrayList<>())
                .roles(new ArrayList<>())
                .users(new ArrayList<>())
                .guest(true)
                .build()
        ))
        .build();
    assertTrue(
        AccessController
            .from(acl)
            .hasAllPermissions(
                "test",
                Collections.emptyList(),
                Collections.singleton("group"),
                "read", "write"));
    assertFalse(
        AccessController
            .from(acl)
            .hasAllPermissions(
                "test",
                Collections.emptyList(),
                Collections.singleton("test"),
                "read", "write"));
  }

}