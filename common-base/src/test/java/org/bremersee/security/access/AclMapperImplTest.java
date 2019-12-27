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
 * The acl mapper impl test.
 *
 * @author Christian Bremer
 */
public class AclMapperImplTest {

  /**
   * Map with defaults and admin switch.
   */
  @Test
  public void mapWithDefaultsAndAdminSwitch() {
    AclMapper<Acl<? extends Ace>> mapper = new AclMapperImpl<>(
        AclImpl::new,
        PermissionConstants.ALL,
        true,
        false
    );

    assertNotNull(mapper.map((Acl<? extends Ace>) null));
    assertNotNull(mapper.map((AccessControlList) null));

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
    assertTrue(destination.entryMap().keySet().containsAll(Arrays.asList(PermissionConstants.ALL)));
    assertEquals(source.getOwner(), destination.getOwner());
    assertTrue(destination.entryMap().get("write").getUsers().contains("user"));
    assertTrue(destination
        .entryMap()
        .get("write")
        .getRoles()
        .containsAll(Arrays.asList("role", AuthorityConstants.ADMIN_ROLE_NAME)));
    assertTrue(destination.entryMap().get("write").getGroups().contains("group"));

    source = mapper.map(destination);
    assertNotNull(source);
    assertEquals(destination.getOwner(), source.getOwner());
    for (String permission : PermissionConstants.ALL) {
      assertTrue(source
          .getEntries()
          .stream()
          .anyMatch(ace -> ace.getPermission().equals(permission)));
    }
    AccessControlEntry ace = source
        .getEntries()
        .stream()
        .filter(entry -> entry.getPermission().equals("write"))
        .findFirst()
        .orElse(null);
    assertNotNull(ace);
    assertTrue(ace.getGroups().contains("group"));
    assertTrue(ace.getRoles().contains("role"));
    assertTrue(ace.getUsers().contains("user"));
    assertFalse(ace.getRoles().contains(AuthorityConstants.ADMIN_ROLE_NAME));

    AccessControlList accessControlList = mapper.defaultAccessControlList("owner");
    assertNotNull(accessControlList);
    assertEquals("owner", accessControlList.getOwner());

    Acl<? extends Ace> acl = mapper.defaultAcl("owner");
    assertNotNull(acl);
    assertEquals("owner", acl.getOwner());
  }

  /**
   * Map.
   */
  @Test
  public void map() {
    AclMapper<Acl<? extends Ace>> mapper = new AclMapperImpl<>(
        AclImpl::new,
        null,
        false,
        true
    );

    assertNull(mapper.map((Acl<? extends Ace>) null));
    assertNull(mapper.map((AccessControlList) null));

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

    source = mapper.map(destination);
    assertNotNull(source);
    assertEquals(destination.getOwner(), source.getOwner());
    assertFalse(source
        .getEntries()
        .stream()
        .anyMatch(ace -> ace.getPermission().equals(PermissionConstants.DELETE)));
    AccessControlEntry ace = source
        .getEntries()
        .stream()
        .filter(entry -> entry.getPermission().equals("write"))
        .findFirst()
        .orElse(null);
    assertNotNull(ace);
    assertTrue(ace.getGroups().contains("group"));
    assertTrue(ace.getRoles().contains("role"));
    assertTrue(ace.getUsers().contains("user"));
    assertFalse(ace.getRoles().contains(AuthorityConstants.ADMIN_ROLE_NAME));
  }

}