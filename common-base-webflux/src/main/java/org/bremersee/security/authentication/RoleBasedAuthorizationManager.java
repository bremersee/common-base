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

package org.bremersee.security.authentication;

import java.util.Collection;
import java.util.Optional;
import org.springframework.security.authorization.AuthenticatedReactiveAuthorizationManager;
import org.springframework.security.authorization.AuthorityReactiveAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

/**
 * The role based authorization manager.
 *
 * @author Christian Bremer
 */
public class RoleBasedAuthorizationManager
    implements ReactiveAuthorizationManager<AuthorizationContext> {

  private final String[] roles;

  private final boolean withAuthenticatedFallback;

  /**
   * Instantiates a new role based authorization manager.
   *
   * @param roles the roles
   */
  public RoleBasedAuthorizationManager(Collection<String> roles) {
    this(roles, true);
  }

  /**
   * Instantiates a new role based authorization manager.
   *
   * @param roles the roles
   * @param withAuthenticatedFallback the with authenticated fallback flag
   */
  public RoleBasedAuthorizationManager(
      Collection<String> roles,
      boolean withAuthenticatedFallback) {

    this.roles = Optional.ofNullable(roles)
        .map(col -> col.toArray(new String[0]))
        .orElseGet(() -> new String[0]);
    this.withAuthenticatedFallback = withAuthenticatedFallback;
  }

  @Override
  public Mono<AuthorizationDecision> check(
      Mono<Authentication> authentication,
      AuthorizationContext authorizationContext) {

    if (roles.length == 0 && withAuthenticatedFallback) {
      return AuthenticatedReactiveAuthorizationManager
          .authenticated().check(authentication, authorizationContext);
    } else if (roles.length == 0) {
      return Mono.just(new AuthorizationDecision(false));
    } else {
      return AuthorityReactiveAuthorizationManager.hasAnyAuthority(roles)
          .check(authentication, authorizationContext);
    }
  }

}
