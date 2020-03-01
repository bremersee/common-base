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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.bremersee.common.model.AccessControlEntry;
import org.bremersee.common.model.AccessControlList;
import org.bremersee.security.core.AuthorityConstants;
import org.junit.jupiter.api.Test;

/**
 * The acl mapper test.
 *
 * @author Christian Bremer
 */
class AclMapperTest {

  private static AclMapper<AclImpl> mapper = accessControlList -> new AclMapperImpl<>(AclImpl::new)
      .map(accessControlList);

  /**
   * Tests default access control list.
   */
  @Test
  void defaultAccessControlList() {
    AccessControlList acl = mapper.defaultAccessControlList("owner");
    assertNotNull(acl);
    assertEquals("owner", acl.getOwner());
  }

  /**
   * Tests map acl.
   */
  @Test
  void mapAcl() {
    AccessControlList accessControlList = mapper.map(new AclImpl("owner", null));
    assertEquals("owner", accessControlList.getOwner());
  }


  /**
   * Tests map.
   */
  @Test
  void mapAccessControlList() {
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
  void mapWithFactory() {
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
  void defaultAcl() {
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