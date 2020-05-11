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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * The user context.
 *
 * @author Christian Bremer
 */
@ToString
@EqualsAndHashCode
public class UserContext {

  @Getter
  private final String userId;

  @Getter
  private final Set<String> roles;

  @Getter
  private final Set<String> groups;

  /**
   * Instantiates a new user context.
   */
  public UserContext() {
    this(null, null, null);
  }

  /**
   * Instantiates a new user context.
   *
   * @param authentication the authentication
   * @param groupsSupplier the groups supplier
   */
  public UserContext(Authentication authentication, Supplier<Collection<String>> groupsSupplier) {
    this(authentication, groupsSupplier != null ? groupsSupplier.get() : null);
  }

  /**
   * Instantiates a new user context.
   *
   * @param authentication the authentication
   * @param groups the groups
   */
  public UserContext(Authentication authentication, Collection<String> groups) {
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
  public UserContext(String userId, Collection<String> roles, Collection<String> groups) {
    this.userId = userId;
    this.roles = roles != null ? Set.copyOf(roles) : Collections.emptySet();
    this.groups = groups != null ? Set.copyOf(groups) : Collections.emptySet();
  }

  /**
   * Has role boolean.
   *
   * @param role the role
   * @return the boolean
   */
  public boolean hasRole(String role) {
    return role != null && roles.contains(role);
  }

  /**
   * Has any role boolean.
   *
   * @param roles the roles
   * @return the boolean
   */
  public boolean hasAnyRole(Collection<String> roles) {
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
  public boolean hasAnyRole(String... roles) {
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
  public boolean isInGroup(String group) {
    return group != null && groups.contains(group);
  }

  /**
   * Is in any group boolean.
   *
   * @param groups the groups
   * @return the boolean
   */
  public boolean isInAnyGroup(Collection<String> groups) {
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
  public boolean isInAnyGroup(String... groups) {
    return Optional.ofNullable(groups)
        .map(arr -> isInAnyGroup(Arrays.asList(arr)))
        .orElse(false);
  }

}
