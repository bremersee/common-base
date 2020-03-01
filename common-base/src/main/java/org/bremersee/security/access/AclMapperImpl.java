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

import static org.springframework.util.Assert.notNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.bremersee.common.model.AccessControlList;
import org.bremersee.security.core.AuthorityConstants;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * The acl mapper implementation.
 *
 * @param <T> the acl type
 * @author Christian Bremer
 */
public class AclMapperImpl<T extends Acl<? extends Ace>> implements AclMapper<T> {

  private final AclFactory<T> aclFactory;

  private final String[] defaultPermissions;

  private final boolean switchAdminAccess;

  private final boolean returnNull;

  private Set<String> adminRoles;

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
   * @param aclFactory the acl factory
   * @param defaultPermissions the default permissions
   * @param switchAdminAccess the switch admin access
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
   * @param aclFactory the acl factory
   * @param defaultPermissions the default permissions
   * @param switchAdminAccess the switch admin access
   * @param returnNull the return null
   */
  public AclMapperImpl(
      @NotNull AclFactory<T> aclFactory,
      @Nullable String[] defaultPermissions,
      boolean switchAdminAccess,
      boolean returnNull) {
    notNull(aclFactory, "Acl factory must not be null.");
    this.aclFactory = aclFactory;
    this.defaultPermissions = defaultPermissions;
    this.switchAdminAccess = switchAdminAccess;
    this.returnNull = returnNull;
    getAdminRoles().add(AuthorityConstants.ADMIN_ROLE_NAME);
  }

  /**
   * Gets admin role.
   *
   * @return the admin role
   * @deprecated Use {@link #getAdminRoles()} instead.
   */
  @Deprecated
  public String getAdminRole() {
    return adminRoles != null && !adminRoles.isEmpty() ? adminRoles.iterator().next() : null;
  }

  /**
   * Sets admin role.
   *
   * @param adminRole the admin role
   * @deprecated Use {@link #setAdminRoles(Set)} instead.
   */
  @Deprecated
  public void setAdminRole(String adminRole) {
    if (StringUtils.hasText(adminRole)) {
      this.setAdminRoles(Collections.singleton(adminRole));
    } else {
      this.setAdminRoles(Collections.emptySet());
    }
  }

  /**
   * Gets admin roles.
   *
   * @return the admin roles
   */
  public Set<String> getAdminRoles() {
    if (adminRoles == null) {
      adminRoles = new LinkedHashSet<>();
    }
    return adminRoles;
  }

  /**
   * Sets admin roles.
   *
   * @param adminRoles the admin roles
   */
  public void setAdminRoles(Set<String> adminRoles) {
    this.adminRoles = new LinkedHashSet<>(adminRoles != null ? adminRoles : Collections.emptySet());
  }

  @Override
  public AclFactory<T> getAclFactory() {
    return aclFactory;
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
  public AccessControlList map(T acl) {
    if (acl == null && returnNull) {
      return null;
    }
    final AclBuilder aclBuilder = AclBuilder
        .builder()
        .from(acl)
        .defaults(defaultPermissions);
    if (switchAdminAccess) {
      return aclBuilder
          .removeAdminAccess(adminRoles)
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
          .ensureAdminAccess(adminRoles)
          .build(aclFactory);
    }
    return aclBuilder
        .build(aclFactory);
  }

  @Override
  public T defaultAcl(String owner) {
    final AclBuilder aclBuilder = AclBuilder
        .builder()
        .owner(owner)
        .addUser(owner, defaultPermissions);
    if (switchAdminAccess) {
      return aclBuilder
          .ensureAdminAccess(adminRoles)
          .build(aclFactory);
    }
    return aclBuilder
        .build(aclFactory);
  }

}
