package org.bremersee.security.authentication;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.bremersee.security.ReactiveIpAddressMatcher;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

/**
 * The role or ip based authorization manager.
 */
public class RoleOrIpBasedAuthorizationManager extends RoleBasedAuthorizationManager {

  private final Set<String> ipAddresses;

  /**
   * Instantiates a new Role or ip based authorization manager.
   *
   * @param roles the roles
   * @param ipAddresses the ip addresses
   */
  public RoleOrIpBasedAuthorizationManager(
      Collection<String> roles,
      Collection<String> ipAddresses) {
    this(roles, null, ipAddresses);
  }

  /**
   * Instantiates a new Role or ip based authorization manager.
   *
   * @param roles the roles
   * @param rolePrefix the role prefix
   * @param ipAddresses the ip addresses
   */
  public RoleOrIpBasedAuthorizationManager(
      Collection<String> roles,
      String rolePrefix,
      Collection<String> ipAddresses) {
    super(roles, rolePrefix);
    this.ipAddresses = Optional.ofNullable(ipAddresses)
        .map(col -> col.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()))
        .orElseGet(Collections::emptySet);
  }

  @Override
  public Mono<AuthorizationDecision> check(
      Mono<Authentication> authentication,
      AuthorizationContext authorizationContext) {

    return super.check(authentication, authorizationContext)
        .filter(AuthorizationDecision::isGranted)
        .defaultIfEmpty(new AuthorizationDecision(isWhiteListedIp(authorizationContext)));
  }

  private boolean isWhiteListedIp(AuthorizationContext context) {
    return ipAddresses.stream()
        .anyMatch(ip -> new ReactiveIpAddressMatcher(ip)
            .matchesRemoteAddress(context.getExchange()));
  }

}
