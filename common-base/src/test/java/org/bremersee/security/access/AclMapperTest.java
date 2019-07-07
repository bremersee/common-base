package org.bremersee.security.access;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.bremersee.common.model.AccessControlEntry;
import org.bremersee.common.model.AccessControlList;
import org.bremersee.security.core.AuthorityConstants;
import org.junit.Test;

/**
 * The acl mapper test.
 *
 * @author Christian Bremer
 */
public class AclMapperTest {

  private static AclMapper<AclImpl> mapper = accessControlList -> new AclMapperImpl<>(AclImpl::new)
      .map(accessControlList);

  /**
   * Tests default access control list.
   */
  @Test
  public void defaultAccessControlList() {
    AccessControlList acl = mapper.defaultAccessControlList("owner");
    assertNotNull(acl);
    assertEquals("owner", acl.getOwner());
  }

  /**
   * Tests map.
   */
  @Test
  public void map() {
    AccessControlList source = AccessControlList
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

    Acl<? extends Ace> destination = mapper.map(source);
    assertNotNull(destination);
    assertFalse(
        destination.entryMap().keySet().containsAll(Arrays.asList(PermissionConstants.ALL)));
    assertEquals(source.getOwner(), destination.getOwner());
    assertTrue(destination.entryMap().get("write").getUsers().contains("user"));
    assertFalse(destination
        .entryMap()
        .get("write")
        .getRoles()
        .containsAll(Arrays.asList("role", AuthorityConstants.ADMIN_ROLE_NAME)));
    assertTrue(destination.entryMap().get("write").getGroups().contains("group"));
  }

  /**
   * Tests map with factory.
   */
  @Test
  public void mapWithFactory() {
    AccessControlList source = AccessControlList
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

    Acl<? extends Ace> destination = mapper.map(source, AclImpl::new);
    assertNotNull(destination);
    assertFalse(
        destination.entryMap().keySet().containsAll(Arrays.asList(PermissionConstants.ALL)));
    assertEquals(source.getOwner(), destination.getOwner());
    assertTrue(destination.entryMap().get("write").getUsers().contains("user"));
    assertFalse(destination
        .entryMap()
        .get("write")
        .getRoles()
        .containsAll(Arrays.asList("role", AuthorityConstants.ADMIN_ROLE_NAME)));
    assertTrue(destination.entryMap().get("write").getGroups().contains("group"));
  }

  /**
   * Tests default acl.
   */
  @Test
  public void defaultAcl() {
    AclImpl acl = mapper.defaultAcl("owner");
    assertNotNull(acl);
    assertEquals("owner", acl.getOwner());

    acl = mapper.defaultAcl(null);
    assertNotNull(acl);
    assertNull(acl.getOwner());

    AclMapper<AclImpl> nullMapper = accessControlList -> null;
    acl = nullMapper.defaultAcl("owner");
    assertNull(acl);

  }
}