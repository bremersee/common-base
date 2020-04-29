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
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

/**
 * The role or ip based authorization manager.
 *
 * @author Christian Bremer
 */
public class RoleOrIpBasedAuthorizationManager extends RoleBasedAuthorizationManager {

  private IpBasedAuthorizationManager ipBasedAuthorizationManager;

  /**
   * Instantiates a new role or ip based authorization manager.
   *
   * @param roles the roles
   * @param ipAddresses the ip addresses
   */
  public RoleOrIpBasedAuthorizationManager(
      Collection<String> roles,
      Collection<String> ipAddresses) {
    this(roles, ipAddresses, true);
  }

  /**
   * Instantiates a new role or ip based authorization manager.
   *
   * @param roles the roles
   * @param ipAddresses the ip addresses
   * @param withAuthenticatedFallback the with authenticated fallback flag
   */
  public RoleOrIpBasedAuthorizationManager(
      Collection<String> roles,
      Collection<String> ipAddresses,
      boolean withAuthenticatedFallback) {
    super(roles, withAuthenticatedFallback);
    this.ipBasedAuthorizationManager = new IpBasedAuthorizationManager(ipAddresses, false);
  }

  @Override
  public Mono<AuthorizationDecision> check(
      Mono<Authentication> authentication,
      AuthorizationContext authorizationContext) {

    return ipBasedAuthorizationManager.check(authentication, authorizationContext)
        .filter(AuthorizationDecision::isGranted)
        .switchIfEmpty(super.check(authentication, authorizationContext));
  }

}
