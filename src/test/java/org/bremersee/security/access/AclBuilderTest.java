package org.bremersee.security.access;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.bremersee.common.model.AccessControlEntry;
import org.bremersee.common.model.AccessControlList;
import org.bremersee.security.core.AuthorityConstants;
import org.junit.Test;

/**
 * @author Christian Bremer
 */
public class AclBuilderTest {

  private static final AclBuilder aclBuilder = AclBuilder.builder();

  @Test
  public void reset() {
    Acl<? extends Ace> acl = aclBuilder
        .owner("test")
        .addGroup("group", "read")
        .addRole("role", "read")
        .addUser("user", "read")
        .reset()
        .buildAcl();
    assertNotNull(acl);
    assertNull(acl.getOwner());
    assertTrue(acl.entryMap().isEmpty());
  }

  @Test
  public void defaults() {
    Acl<? extends Ace> acl = aclBuilder
        .reset()
        .defaults(PermissionConstants.ADMINISTRATION, PermissionConstants.CREATE)
        .buildAcl();
    assertNotNull(acl);
    assertTrue(acl.entryMap().containsKey(PermissionConstants.ADMINISTRATION));
    assertTrue(acl.entryMap().containsKey(PermissionConstants.CREATE));
    assertFalse(acl.entryMap().containsKey(PermissionConstants.DELETE));
    assertFalse(acl.entryMap().containsKey(PermissionConstants.READ));
    assertFalse(acl.entryMap().containsKey(PermissionConstants.WRITE));

    acl = aclBuilder.reset().defaults(PermissionConstants.ALL).buildAcl();
    assertNotNull(acl);
    assertTrue(acl.entryMap().keySet().containsAll(Arrays.asList(PermissionConstants.ALL)));
  }

  @Test
  public void fromAccessControlList() {
    AccessControlList expected = new AccessControlList()
        .owner("test")
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
    expected.getEntries().sort(new AccessControlEntryComparator());

    AccessControlList actual = aclBuilder
        .reset()
        .from(expected)
        .buildAccessControlList();
    assertNotNull(actual);
    actual.getEntries().sort(new AccessControlEntryComparator());

    System.out.println("Expected: \n" + expected);
    System.out.println("  Actual: \n" + actual);
    assertEquals(expected, actual);
  }

  @Test
  public void fromAcl() {
    AceImpl ace0 = new AceImpl();
    ace0.setGuest(true);
    AceImpl ace1 = new AceImpl();
    ace1.setGuest(false);
    ace1.getGroups().add("group");
    ace1.getRoles().add("role");
    ace1.getUsers().add("user");
    Map<String, AceImpl> entries = new HashMap<>();
    entries.put("read", ace0);
    entries.put("write", ace1);
    AclImpl expected = new AclImpl("test", entries);
    Acl<? extends Ace> actual = aclBuilder
        .reset()
        .from(expected)
        .buildAcl();
    assertNotNull(actual);
    System.out.println("Expected: \n" + expected);
    System.out.println("  Actual: \n" + actual);
    assertEquals(expected, actual);
  }

  @Test
  public void owner() {
    assertEquals(
        "test",
        aclBuilder
            .reset()
            .owner("test")
            .buildAccessControlList().getOwner());
    assertEquals(
        "test",
        aclBuilder
            .reset()
            .owner("test")
            .buildAcl().getOwner());
  }

  @Test
  public void guest() {
    assertTrue(aclBuilder
        .reset()
        .guest(true, "read")
        .buildAcl()
        .entryMap()
        .get("read")
        .isGuest());
    assertFalse(aclBuilder
        .guest(false, "read")
        .buildAcl()
        .entryMap()
        .get("read")
        .isGuest());
  }

  @Test
  public void addAndRemoveUser() {
    assertTrue(aclBuilder
        .reset()
        .addUser("test", "write")
        .buildAcl()
        .entryMap()
        .get("write")
        .getUsers()
        .contains("test"));
    assertFalse(aclBuilder
        .removeUser("test", "write")
        .buildAcl()
        .entryMap()
        .get("write")
        .getUsers()
        .contains("test"));
  }

  @Test
  public void addAndRemoveRole() {
    assertTrue(aclBuilder
        .reset()
        .addRole("test", "write")
        .buildAcl()
        .entryMap()
        .get("write")
        .getRoles()
        .contains("test"));
    assertFalse(aclBuilder
        .removeRole("test", "write")
        .buildAcl()
        .entryMap()
        .get("write")
        .getRoles()
        .contains("test"));
  }

  @Test
  public void addAndRemoveGroup() {
    assertTrue(aclBuilder
        .reset()
        .addGroup("test", "write")
        .buildAcl()
        .entryMap()
        .get("write")
        .getGroups()
        .contains("test"));
    assertFalse(aclBuilder
        .removeGroup("test", "write")
        .buildAcl()
        .entryMap()
        .get("write")
        .getGroups()
        .contains("test"));
  }

  @Test
  public void adminAccess() {
    assertTrue(aclBuilder
        .reset()
        .defaults(PermissionConstants.CREATE)
        .ensureAdminAccess()
        .buildAcl()
        .entryMap()
        .get(PermissionConstants.CREATE)
        .getRoles()
        .contains(AuthorityConstants.ADMIN_ROLE_NAME));
    assertFalse(aclBuilder
        .removeAdminAccess()
        .buildAcl()
        .entryMap()
        .get(PermissionConstants.CREATE)
        .getRoles()
        .contains(AuthorityConstants.ADMIN_ROLE_NAME));

    assertTrue(aclBuilder
        .reset()
        .ensureAdminAccess("ROLE_ADMIN", "delete")
        .buildAcl()
        .entryMap()
        .get("delete")
        .getRoles()
        .contains("ROLE_ADMIN"));
    assertFalse(aclBuilder
        .removeAdminAccess("ROLE_ADMIN", "delete", "write")
        .buildAcl()
        .entryMap()
        .get("delete")
        .getRoles()
        .contains("ROLE_ADMIN"));
  }

}