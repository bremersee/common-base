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
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.bremersee.security.ReactiveIpAddressMatcher;
import org.springframework.security.authorization.AuthenticatedReactiveAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

/**
 * The ip based authorization manager.
 *
 * @author Christian Bremer
 */
public class IpBasedAuthorizationManager
    implements ReactiveAuthorizationManager<AuthorizationContext> {

  private final Set<String> ipAddresses;

  private final boolean withAuthenticatedFallback;

  /**
   * Instantiates a new ip based authorization manager.
   *
   * @param ipAddresses the ip addresses
   */
  public IpBasedAuthorizationManager(
      Collection<String> ipAddresses) {
    this(ipAddresses, true);
  }

  /**
   * Instantiates a new ip based authorization manager.
   *
   * @param ipAddresses the ip addresses
   * @param withAuthenticatedFallback the with authenticated fallback flag
   */
  public IpBasedAuthorizationManager(
      Collection<String> ipAddresses,
      boolean withAuthenticatedFallback) {
    this.ipAddresses = Optional.ofNullable(ipAddresses)
        .map(col -> col.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()))
        .orElseGet(Collections::emptySet);
    this.withAuthenticatedFallback = withAuthenticatedFallback;
  }

  @Override
  public Mono<AuthorizationDecision> check(
      Mono<Authentication> authentication,
      AuthorizationContext authorizationContext) {

    return Mono.just(new AuthorizationDecision(isWhiteListedIp(authorizationContext)))
        .flatMap(decision -> {
          if (decision.isGranted()) {
            return Mono.just(decision);
          }
          if (withAuthenticatedFallback) {
            return AuthenticatedReactiveAuthorizationManager.authenticated()
                .check(authentication, authorizationContext);
          }
          return Mono.just(new AuthorizationDecision(false));
        });
  }

  private boolean isWhiteListedIp(AuthorizationContext context) {
    return ipAddresses.stream()
        .anyMatch(ip -> new ReactiveIpAddressMatcher(ip)
            .matchesRemoteAddress(context.getExchange()));
  }

}
