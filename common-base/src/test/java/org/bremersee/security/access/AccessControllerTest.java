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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.bremersee.common.model.AccessControlEntry;
import org.bremersee.common.model.AccessControlList;
import org.junit.Test;

/**
 * The access controller test.
 *
 * @author Christian Bremer
 */
public class AccessControllerTest {

  /**
   * Has any permission.
   */
  @Test
  public void hasAnyPermission() {
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
  public void hasAllPermissions() {
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