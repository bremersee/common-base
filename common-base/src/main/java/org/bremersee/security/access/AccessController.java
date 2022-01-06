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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.bremersee.common.model.AccessControlList;
import org.springframework.lang.Nullable;

/**
 * The access controller.
 *
 * @author Christian Bremer
 */
public interface AccessController {

  /**
   * Creates an access controller from the given access control list.
   *
   * @param acl the access control list
   * @return the access controller
   */
  static AccessController from(@Nullable AccessControlList acl) {
    return acl == null ? new Impl() : new Impl(AclBuilder.builder().from(acl).buildAcl());
  }

  /**
   * Creates an access controller from the given access control list.
   *
   * @param acl the access control list
   * @return the access controller
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  static AccessController from(@Nullable Acl acl) {
    return acl == null ? new Impl() : new Impl(AclBuilder.builder().from(acl).buildAcl());
  }

  /**
   * Determines whether the given user with the given roles and groups has the specified permission.
   *
   * @param user the user
   * @param roles the roles
   * @param groups the groups
   * @param permission the permission
   * @return {@code true} if the user has the permission, otherwise {@code false}
   */
  boolean hasPermission(
      @Nullable String user,
      @Nullable Collection<String> roles,
      @Nullable Collection<String> groups,
      @Nullable String permission);

  /**
   * Determines whether the given user with the given roles and groups has at least one of the specified permissions.
   *
   * @param user the user
   * @param roles the roles
   * @param groups the groups
   * @param permissions the permissions
   * @return {@code true} if the user has at least one permission, otherwise {@code false}
   */
  default boolean hasAnyPermission(
      @Nullable String user,
      @Nullable Collection<String> roles,
      @Nullable Collection<String> groups,
      @Nullable String... permissions) {

    return hasAnyPermission(
        user,
        roles,
        groups,
        permissions == null ? Collections.emptyList() : Arrays.asList(permissions));
  }

  /**
   * Determines whether the given user with the given roles and groups has at least one of the specified permissions.
   *
   * @param user the user
   * @param roles the roles
   * @param groups the groups
   * @param permissions the permissions
   * @return {@code true} if the user has at least one permission, otherwise {@code false}
   */
  default boolean hasAnyPermission(
      @Nullable String user,
      @Nullable Collection<String> roles,
      @Nullable Collection<String> groups,
      @Nullable Collection<String> permissions) {

    return permissions != null
        && !permissions.isEmpty()
        && permissions
        .stream()
        .anyMatch(permission -> hasPermission(user, roles, groups, permission));
  }

  /**
   * Determines whether the given user with the given roles and groups has all specified permissions.
   *
   * @param user the user
   * @param roles the roles
   * @param groups the groups
   * @param permissions the permissions
   * @return {@code true} if the user has all permissions, otherwise {@code false}
   */
  default boolean hasAllPermissions(
      String user,
      Collection<String> roles,
      Collection<String> groups,
      String... permissions) {

    return hasAllPermissions(
        user,
        roles,
        groups,
        permissions == null ? Collections.emptyList() : Arrays.asList(permissions));
  }

  /**
   * Determines whether the given user with the given roles and groups has all specified permissions.
   *
   * @param user the user
   * @param roles the roles
   * @param groups the groups
   * @param permissions the permissions
   * @return {@code true} if the user has all permissions, otherwise {@code false}
   */
  default boolean hasAllPermissions(
      String user,
      Collection<String> roles,
      Collection<String> groups,
      Collection<String> permissions) {

    return permissions != null
        && !permissions.isEmpty()
        && permissions
        .stream()
        .allMatch(permission -> hasPermission(user, roles, groups, permission));
  }

  /**
   * The default access controller implementation.
   */
  class Impl implements AccessController {

    private final Acl<? extends Ace> acl;

    /**
     * Instantiates a new access controller.
     */
    Impl() {
      acl = null;
    }

    /**
     * Instantiates a new access controller.
     *
     * @param acl the acl
     */
    Impl(final Acl<? extends Ace> acl) {
      this.acl = acl;
    }

    @Override
    public boolean hasPermission(
        final String user,
        final Collection<String> roles,
        final Collection<String> groups,
        final String permission) {

      if (acl == null) {
        return false;
      }
      if (permission == null) {
        return false;
      }
      if (user != null && user.equals(acl.getOwner())) {
        return true;
      }
      final Map<String, ? extends Ace> map = acl.entryMap();
      if (map == null) {
        return false;
      }
      final Ace ace = map.get(permission.toLowerCase());
      if (ace == null) {
        return false;
      }
      if (ace.isGuest()) {
        return true;
      }
      if (ace.getUsers().contains(user)) {
        return true;
      }
      if (roles != null && roles.stream().anyMatch(role -> ace.getRoles().contains(role))) {
        return true;
      }
      return groups != null && groups.stream().anyMatch(group -> ace.getGroups().contains(group));
    }
  }

}
