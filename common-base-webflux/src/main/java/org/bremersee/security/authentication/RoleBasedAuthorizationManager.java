package org.bremersee.security.authentication;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import org.springframework.security.authorization.AuthorityReactiveAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

/**
 * The role based authorization manager.
 */
public class RoleBasedAuthorizationManager
    implements ReactiveAuthorizationManager<AuthorizationContext> {

  private static final String DEFAULT_ROLE_PREFIX = "ROLE_";

  private final String[] roles;

  /**
   * Instantiates a new Role or ip based authorization manager.
   *
   * @param roles the roles
   */
  public RoleBasedAuthorizationManager(Collection<String> roles) {
    this(roles, DEFAULT_ROLE_PREFIX);
  }

  /**
   * Instantiates a new role based authorization manager.
   *
   * @param roles the roles
   * @param rolePrefix the role prefix
   */
  public RoleBasedAuthorizationManager(
      Collection<String> roles,
      String rolePrefix) {
    final String prefix = StringUtils.hasText(rolePrefix) ? rolePrefix : DEFAULT_ROLE_PREFIX;
    this.roles = Optional.ofNullable(roles)
        .map(col -> col.stream()
            .filter(Objects::nonNull)
            .map(role -> role.startsWith(prefix) ? role : prefix + role)
            .toArray(String[]::new))
        .orElseGet(() -> new String[0]);
  }

  @Override
  public Mono<AuthorizationDecision> check(
      Mono<Authentication> authentication,
      AuthorizationContext authorizationContext) {

    return AuthorityReactiveAuthorizationManager.hasAnyAuthority(roles)
        .check(authentication, authorizationContext);
  }

}
