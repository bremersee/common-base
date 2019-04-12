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