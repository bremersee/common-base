/*
 * Copyright 2020 the original author or authors.
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

package org.bremersee.security.core;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

/**
 * The user context.
 *
 * @author Christian Bremer
 */
@Validated
public interface UserContext extends Principal {

  /**
   * Gets user id.
   *
   * @return the user id
   */
  String getUserId();

  @Override
  default String getName() {
    return getUserId();
  }

  /**
   * Gets roles.
   *
   * @return the roles
   */
  @NotNull
  default Set<String> getRoles() {
    return Collections.emptySet();
  }

  /**
   * Gets groups.
   *
   * @return the groups
   */
  @NotNull
  default Set<String> getGroups() {
    return Collections.emptySet();
  }

  /**
   * New instance user context.
   *
   * @return the user context
   */
  static UserContext newInstance() {
    return new Impl();
  }

  /**
   * New instance user context.
   *
   * @param authentication the authentication
   * @param groups the groups
   * @return the user context
   */
  static UserContext newInstance(
      @Nullable Authentication authentication,
      @Nullable Collection<String> groups) {
    return new Impl(authentication, groups);
  }

  /**
   * New instance user context.
   *
   * @param userId the user id
   * @param roles the roles
   * @param groups the groups
   * @return the user context
   */
  static UserContext newInstance(
      @Nullable String userId,
      @Nullable Collection<String> roles,
      @Nullable Collection<String> groups) {
    return new Impl(userId, roles, groups);
  }

  /**
   * Determines whether the user id is present or not.
   *
   * @return the boolean
   */
  default boolean isUserIdPresent() {
    return StringUtils.hasText(getUserId());
  }

  /**
   * Has role boolean.
   *
   * @param role the role
   * @return the boolean
   */
  default boolean hasRole(@Nullable String role) {
    return role != null && getRoles().contains(role);
  }

  /**
   * Has any role boolean.
   *
   * @param roles the roles
   * @return the boolean
   */
  default boolean hasAnyRole(@Nullable Collection<String> roles) {
    return Optional.ofNullable(roles)
        .map(col -> col.stream().anyMatch(this::hasRole))
        .orElse(false);
  }

  /**
   * Has any role boolean.
   *
   * @param roles the roles
   * @return the boolean
   */
  default boolean hasAnyRole(@Nullable String... roles) {
    return Optional.ofNullable(roles)
        .map(arr -> hasAnyRole(Arrays.asList(arr)))
        .orElse(false);
  }

  /**
   * Is in group boolean.
   *
   * @param group the group
   * @return the boolean
   */
  default boolean isInGroup(@Nullable String group) {
    return group != null && getGroups().contains(group);
  }

  /**
   * Is in any group boolean.
   *
   * @param groups the groups
   * @return the boolean
   */
  default boolean isInAnyGroup(@Nullable Collection<String> groups) {
    return Optional.ofNullable(groups)
        .map(col -> col.stream().anyMatch(this::isInGroup))
        .orElse(false);
  }

  /**
   * Is in any group boolean.
   *
   * @param groups the groups
   * @return the boolean
   */
  default boolean isInAnyGroup(@Nullable String... groups) {
    return Optional.ofNullable(groups)
        .map(arr -> isInAnyGroup(Arrays.asList(arr)))
        .orElse(false);
  }

  /**
   * The default implementation.
   */
  @ToString
  @EqualsAndHashCode
  class Impl implements UserContext {

    @Getter
    private final String userId;

    @Getter
    private final Set<String> roles;

    @Getter
    private final Set<String> groups;

    /**
     * Instantiates a new user context.
     */
    public Impl() {
      this(null, null, null);
    }

    /**
     * Instantiates a new user context.
     *
     * @param authentication the authentication
     * @param groups the groups
     */
    public Impl(Authentication authentication, Collection<String> groups) {
      this(
          Optional.ofNullable(authentication).map(Principal::getName).orElse(null),
          Optional.ofNullable(authentication)
              .map(auth -> auth.getAuthorities().stream()
                  .map(GrantedAuthority::getAuthority)
                  .collect(Collectors.toSet()))
              .orElse(null),
          groups);
    }

    /**
     * Instantiates a new user context.
     *
     * @param userId the user id
     * @param roles the roles
     * @param groups the groups
     */
    public Impl(String userId, Collection<String> roles, Collection<String> groups) {
      this.userId = userId;
      this.roles = roles != null ? Set.copyOf(roles) : Collections.emptySet();
      this.groups = groups != null ? Set.copyOf(groups) : Collections.emptySet();
    }
  }


}
