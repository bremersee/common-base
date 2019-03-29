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

import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.bremersee.common.model.AccessControlList;
import org.bremersee.security.core.AuthorityConstants;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * The acl mapper implementation.
 *
 * @param <T> the acl type
 * @author Christian Bremer
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class AclMapperImpl<T extends Acl<? extends Ace>> implements AclMapper<T> {

  private final AclFactory<T> aclFactory;

  private final String[] defaultPermissions;

  private final boolean switchAdminAccess;

  private final boolean returnNull;

  @Getter
  @Setter
  private String adminRole = AuthorityConstants.ADMIN_ROLE_NAME;

  /**
   * Instantiates a new acl mapper.
   *
   * @param aclFactory the acl factory
   */
  public AclMapperImpl(
      @NotNull AclFactory<T> aclFactory) {
    this(aclFactory, null, false);
  }

  /**
   * Instantiates a new acl mapper.
   *
   * @param aclFactory         the acl factory
   * @param defaultPermissions the default permissions
   * @param switchAdminAccess  the switch admin access
   */
  public AclMapperImpl(
      @NotNull AclFactory<T> aclFactory,
      @Nullable String[] defaultPermissions,
      boolean switchAdminAccess) {
    this(aclFactory, defaultPermissions, switchAdminAccess, false);
  }

  /**
   * Instantiates a new acl mapper.
   *
   * @param aclFactory         the acl factory
   * @param defaultPermissions the default permissions
   * @param switchAdminAccess  the switch admin access
   * @param returnNull         the return null
   */
  public AclMapperImpl(
      @NotNull AclFactory<T> aclFactory,
      @Nullable String[] defaultPermissions,
      boolean switchAdminAccess,
      boolean returnNull) {
    Assert.notNull(aclFactory, "Acl factory must not be null.");
    this.aclFactory = aclFactory;
    this.defaultPermissions = defaultPermissions;
    this.switchAdminAccess = switchAdminAccess;
    this.returnNull = returnNull;
  }

  @Override
  public AccessControlList defaultAccessControlList(String owner) {
    return AclBuilder
        .builder()
        .owner(owner)
        .addUser(owner, defaultPermissions)
        .buildAccessControlList();
  }

  @Override
  public AccessControlList map(Acl acl) {
    if (acl == null && returnNull) {
      return null;
    }
    final AclBuilder aclBuilder = AclBuilder
        .builder()
        .from(acl)
        .defaults(defaultPermissions);
    if (switchAdminAccess) {
      return aclBuilder
          .removeAdminAccess(adminRole)
          .buildAccessControlList();
    }
    return aclBuilder
        .buildAccessControlList();
  }

  @Override
  public T map(AccessControlList accessControlList) {
    if (accessControlList == null && returnNull) {
      return null;
    }
    final AclBuilder aclBuilder = AclBuilder
        .builder()
        .from(accessControlList)
        .defaults(defaultPermissions);
    if (switchAdminAccess) {
      return aclBuilder
          .ensureAdminAccess(adminRole)
          .build(aclFactory);
    }
    return aclBuilder
        .build(aclFactory);
  }

  @Override
  public T defaultAcl(@Nullable String owner) {
    final AclBuilder aclBuilder = AclBuilder
        .builder()
        .owner(owner)
        .addUser(owner, defaultPermissions);
    if (switchAdminAccess) {
      return aclBuilder
          .ensureAdminAccess(adminRole)
          .build(aclFactory);
    }
    return aclBuilder
        .build(aclFactory);
  }

}
