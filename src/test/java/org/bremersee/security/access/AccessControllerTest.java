package org.bremersee.security.access;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import org.bremersee.common.model.AccessControlEntry;
import org.bremersee.common.model.AccessControlList;
import org.junit.Test;

/**
 * @author Christian Bremer
 */
public class AccessControllerTest {

  @Test
  public void hasAnyPermission() {
    AccessControlList acl = new AccessControlList()
        .owner("owner")
        .addEntriesItem(new AccessControlEntry()
            .permission("write")
            .addGroupsItem("group")
            .addRolesItem("role")
            .addUsersItem("user"))
        .addEntriesItem(new AccessControlEntry()
            .permission("read")
            .groups(new ArrayList<>())
            .roles(new ArrayList<>())
            .users(new ArrayList<>())
            .guest(true));
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

  @Test
  public void hasAllPermissions() {
    AccessControlList acl = new AccessControlList()
        .owner("owner")
        .addEntriesItem(new AccessControlEntry()
            .permission("write")
            .addGroupsItem("group")
            .addRolesItem("role")
            .addUsersItem("user"))
        .addEntriesItem(new AccessControlEntry()
            .permission("read")
            .groups(new ArrayList<>())
            .roles(new ArrayList<>())
            .users(new ArrayList<>())
            .guest(true));
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