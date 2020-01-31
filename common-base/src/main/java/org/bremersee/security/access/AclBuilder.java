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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.bremersee.common.model.AccessControlList;
import org.bremersee.security.core.AuthorityConstants;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

/**
 * The access control list builder.
 *
 * @author Christian Bremer
 */
@Validated
public interface AclBuilder {

  /**
   * Instantiates a new access control list builder.
   *
   * @return the acl builder
   */
  static AclBuilder builder() {
    return new Impl();
  }

  /**
   * Reset acl builder.
   *
   * @return the acl builder
   */
  AclBuilder reset();

  /**
   * Add default access control entries for the given permissions.
   *
   * @param permissions the permissions
   * @return the acl builder
   */
  AclBuilder defaults(@Nullable String... permissions);

  /**
   * From access control list DTO.
   *
   * @param acl the acl dto
   * @return the acl builder
   */
  AclBuilder from(@Nullable AccessControlList acl);

  /**
   * From acl (entity).
   *
   * @param acl the acl (entity)
   * @return the acl builder
   */
  AclBuilder from(@Nullable Acl<? extends Ace> acl);

  /**
   * Sets owner.
   *
   * @param owner the owner
   * @return the acl builder
   */
  AclBuilder owner(@Nullable String owner);

  /**
   * Sets guest.
   *
   * @param isPublic is public
   * @param permissions the permissions
   * @return the acl builder
   */
  AclBuilder guest(@Nullable Boolean isPublic, @Nullable String... permissions);

  /**
   * Adds user.
   *
   * @param user the user
   * @param permissions the permissions
   * @return the acl builder
   */
  AclBuilder addUser(@Nullable String user, @Nullable String... permissions);

  /**
   * Adds role.
   *
   * @param role the role
   * @param permissions the permissions
   * @return the acl builder
   */
  AclBuilder addRole(@Nullable String role, @Nullable String... permissions);

  /**
   * Adds group.
   *
   * @param group the group
   * @param permissions the permissions
   * @return the acl builder
   */
  AclBuilder addGroup(@Nullable String group, @Nullable String... permissions);

  /**
   * Removes user.
   *
   * @param user the user
   * @param permissions the permissions
   * @return the acl builder
   */
  AclBuilder removeUser(@Nullable String user, @Nullable String... permissions);

  /**
   * Removes role.
   *
   * @param role the role
   * @param permissions the permissions
   * @return the acl builder
   */
  AclBuilder removeRole(@Nullable String role, @Nullable String... permissions);

  /**
   * Removes group.
   *
   * @param group the group
   * @param permissions the permissions
   * @return the acl builder
   */
  AclBuilder removeGroup(@Nullable String group, @Nullable String... permissions);

  /**
   * Ensures admin access.
   *
   * @return the acl builder
   */
  default AclBuilder ensureAdminAccess() {
    return ensureAdminAccess(AuthorityConstants.ADMIN_ROLE_NAME);
  }

  /**
   * Adds admin access.
   *
   * @param adminRole the admin role
   * @param permissions the permissions
   * @return the acl builder
   */
  default AclBuilder ensureAdminAccess(
      @Nullable String adminRole,
      @Nullable String... permissions) {

    return ensureAdminAccess(
        StringUtils.hasText(adminRole) ? Collections.singleton(adminRole) : null, permissions);
  }

  /**
   * Adds admin access.
   *
   * @param adminRoles the admin roles
   * @param permissions the permissions
   * @return the acl builder
   */
  AclBuilder ensureAdminAccess(
      @Nullable Collection<String> adminRoles,
      @Nullable String... permissions);

  /**
   * Removes admin access.
   *
   * @return the acl builder
   */
  default AclBuilder removeAdminAccess() {
    return removeAdminAccess(AuthorityConstants.ADMIN_ROLE_NAME);
  }

  /**
   * Removes admin access.
   *
   * @param adminRole the admin role
   * @param permissions the permissions
   * @return the acl builder
   */
  default AclBuilder removeAdminAccess(
      @Nullable String adminRole,
      @Nullable String... permissions) {

    return removeAdminAccess(
        StringUtils.hasText(adminRole) ? Collections.singleton(adminRole) : null, permissions);
  }

  /**
   * Removes admin access.
   *
   * @param adminRoles the admin roles
   * @param permissions the permissions
   * @return the acl builder
   */
  AclBuilder removeAdminAccess(
      @Nullable Collection<String> adminRoles,
      @Nullable String... permissions);

  /**
   * Build acl.
   *
   * @param <T> the type of the acl
   * @param factory the factory
   * @return the acl
   */
  <T> T build(@NotNull AclFactory<T> factory);

  /**
   * Build acl.
   *
   * @return the acl
   */
  default Acl<? extends Ace> buildAcl() {
    return build((o, e) -> new AclImpl(o, new HashMap<>(e)));
  }

  /**
   * Build access control list.
   *
   * @return the access control list
   */
  default AccessControlList buildAccessControlList() {
    return build(AclFactory.dtoFactory());
  }

  /**
   * The default access control list builder implementation.
   */
  class Impl implements AclBuilder {

    private String owner;

    private Map<String, Ace> entries = new HashMap<>();

    @Override
    public AclBuilder reset() {
      this.owner = null;
      this.entries = new HashMap<>();
      return this;
    }

    @Override
    public AclBuilder defaults(final String... permissions) {
      if (permissions != null) {
        Arrays.stream(permissions).forEach(permission -> {
          if (!entries.containsKey(permission)) {
            entries.put(permission.toLowerCase(), new AceImpl());
          }
        });
      }
      return this;
    }

    @Override
    public AclBuilder from(final AccessControlList acl) {
      if (acl != null) {
        this.owner = acl.getOwner();
        if (acl.getEntries() != null) {
          acl.getEntries()
              .stream()
              .filter(Objects::nonNull)
              .filter(accessControlEntry -> StringUtils.hasText(accessControlEntry.getPermission()))
              .forEach(accessControlEntry -> {
                guest(accessControlEntry.getGuest(), accessControlEntry.getPermission());
                if (accessControlEntry.getGroups() != null) {
                  accessControlEntry
                      .getGroups()
                      .forEach(group -> addGroup(
                          group, accessControlEntry.getPermission().toLowerCase()));
                }
                if (accessControlEntry.getRoles() != null) {
                  accessControlEntry
                      .getRoles()
                      .forEach(role -> addRole(
                          role, accessControlEntry.getPermission().toLowerCase()));
                }
                if (accessControlEntry.getUsers() != null) {
                  accessControlEntry
                      .getUsers()
                      .forEach(user -> addUser(
                          user, accessControlEntry.getPermission().toLowerCase()));
                }
              });
        }
      }
      return this;
    }

    @Override
    public AclBuilder from(final Acl<? extends Ace> acl) {
      if (acl != null) {
        this.owner = acl.getOwner();
        final Map<String, ? extends Ace> map = acl.entryMap();
        if (map != null) {
          map.entrySet()
              .stream()
              .filter(Objects::nonNull)
              .filter(entry -> StringUtils.hasText(entry.getKey()))
              .filter(entry -> entry.getValue() != null)
              .forEach(entry -> {
                final String permission = entry.getKey().toLowerCase();
                final Ace ace = entry.getValue();
                guest(ace.isGuest(), permission);
                ace.getGroups().forEach(group -> addGroup(group, permission));
                ace.getRoles().forEach(role -> addRole(role, permission));
                ace.getUsers().forEach(user -> addUser(user, permission));
              });
        }
      }
      return this;
    }

    @Override
    public AclBuilder owner(final String owner) {
      this.owner = owner;
      return this;
    }

    @Override
    public AclBuilder guest(final Boolean isPublic, final String... permissions) {
      if (permissions != null) {
        final boolean guest = Boolean.TRUE.equals(isPublic);
        if (guest) {
          Arrays.stream(permissions)
              .filter(StringUtils::hasText)
              .map(String::toLowerCase)
              .forEach(permission -> entries
                  .computeIfAbsent(permission, p -> new AceImpl())
                  .setGuest(true));
        } else {
          Arrays.stream(permissions)
              .filter(StringUtils::hasText)
              .map(String::toLowerCase)
              .forEach(permission -> entries.computeIfPresent(permission,
                  (p, mongoAccessControlEntry) -> {
                    mongoAccessControlEntry.setGuest(false);
                    return mongoAccessControlEntry;
                  }));
        }
      }
      return this;
    }

    @Override
    public AclBuilder addUser(final String user, final String... permissions) {
      if (StringUtils.hasText(user) && permissions != null) {
        Arrays.stream(permissions)
            .filter(StringUtils::hasText)
            .map(String::toLowerCase)
            .forEach(permission -> entries
                .computeIfAbsent(permission, p -> new AceImpl())
                .getUsers()
                .add(user));
      }
      return this;
    }

    @Override
    public AclBuilder addRole(final String role, final String... permissions) {
      if (StringUtils.hasText(role) && permissions != null) {
        Arrays.stream(permissions)
            .filter(StringUtils::hasText)
            .map(String::toLowerCase)
            .forEach(permission -> entries
                .computeIfAbsent(permission, p -> new AceImpl())
                .getRoles()
                .add(role));
      }
      return this;
    }

    @Override
    public AclBuilder addGroup(final String group, final String... permissions) {
      if (StringUtils.hasText(group) && permissions != null) {
        Arrays.stream(permissions)
            .filter(StringUtils::hasText)
            .map(String::toLowerCase)
            .forEach(permission -> entries
                .computeIfAbsent(permission, p -> new AceImpl())
                .getGroups()
                .add(group));
      }
      return this;
    }

    @Override
    public AclBuilder removeUser(final String user, final String... permissions) {
      if (StringUtils.hasText(user) && permissions != null) {
        Arrays.stream(permissions)
            .filter(StringUtils::hasText)
            .map(String::toLowerCase)
            .forEach(permission -> entries.computeIfPresent(permission,
                (p, mongoAccessControlEntry) -> {
                  mongoAccessControlEntry.getUsers().remove(user);
                  return mongoAccessControlEntry;
                }));
      }
      return this;
    }

    @Override
    public AclBuilder removeRole(final String role, final String... permissions) {
      if (StringUtils.hasText(role) && permissions != null) {
        Arrays.stream(permissions)
            .filter(StringUtils::hasText)
            .map(String::toLowerCase)
            .forEach(permission -> entries.computeIfPresent(permission,
                (p, mongoAccessControlEntry) -> {
                  mongoAccessControlEntry.getRoles().remove(role);
                  return mongoAccessControlEntry;
                }));
      }
      return this;
    }

    @Override
    public AclBuilder removeGroup(final String group, final String... permissions) {
      if (StringUtils.hasText(group) && permissions != null) {
        Arrays.stream(permissions)
            .filter(StringUtils::hasText)
            .map(String::toLowerCase)
            .forEach(permission -> entries.computeIfPresent(permission,
                (p, mongoAccessControlEntry) -> {
                  mongoAccessControlEntry.getGroups().remove(group);
                  return mongoAccessControlEntry;
                }));
      }
      return this;
    }

    @Override
    public AclBuilder ensureAdminAccess(
        final Collection<String> adminRoles,
        final String... permissions) {

      if (adminRoles != null && !adminRoles.isEmpty()) {
        if (permissions == null || permissions.length == 0) {
          adminRoles.forEach(adminRole -> entries.keySet()
              .forEach(permission -> addRole(adminRole, permission)));
        } else {
          adminRoles.forEach(adminRole -> addRole(adminRole, permissions));
        }
      }
      return this;
    }

    @Override
    public AclBuilder removeAdminAccess(
        final Collection<String> adminRoles,
        final String... permissions) {

      if (adminRoles != null && !adminRoles.isEmpty()) {
        if (permissions == null || permissions.length == 0) {
          adminRoles.forEach(adminRole -> entries.keySet()
              .forEach(permission -> removeRole(adminRole, permission)));
        } else {
          adminRoles.forEach(adminRole -> removeRole(adminRole, permissions));
        }
      }
      return this;
    }

    @Override
    public <T> T build(AclFactory<T> factory) {
      return factory.createAccessControlList(owner, entries);
    }
  }
}
