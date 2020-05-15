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

package org.bremersee.actuator.security.authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bremersee.security.authentication.AccessExpressionUtils;
import org.bremersee.security.authentication.AutoSecurityMode;
import org.bremersee.security.authentication.ClientCredentialsFlowProperties;
import org.bremersee.security.core.AuthorityConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

/**
 * Actuator authentication and authorization properties.
 *
 * @author Christian Bremer
 */
@ConfigurationProperties(prefix = "bremersee.actuator.auth")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Validated
public class ActuatorAuthProperties {

  /**
   * Specifies the behaviour of the actuator security auto configuration.
   */
  @NotNull
  private AutoSecurityMode enable = AutoSecurityMode.OTHER;

  /**
   * The order of the actuator security auto configuration.
   */
  private int order = 61;

  /**
   * Specifies whether cors should be enabled for the actuator endpoints or not.
   */
  private boolean enableCors = true;

  /**
   * A list with unauthenticated actuator endpoints.
   */
  @NotNull
  private List<Class<?>> unauthenticatedEndpoints = new ArrayList<>();

  /**
   * The roles which can write to protected actuator endpoints. The role names normally start with
   * {@code ROLE_}.
   */
  @NotNull
  private List<String> adminRoles = new ArrayList<>();

  /**
   * The roles which can read protected actuator endpoints. The role names normally start with
   * {@code ROLE_}.
   */
  @NotNull
  private List<String> roles = new ArrayList<>();

  /**
   * The IP addresses which can access protected actuator endpoints without authentication.
   */
  @NotNull
  private List<String> ipAddresses = new ArrayList<>();

  /**
   * The JWK uri.
   */
  private String jwkSetUri;

  /**
   * The JWS algorithm
   */
  @NotEmpty
  private String jwsAlgorithm = "RS256";

  /**
   * The issuer uri.
   */
  private String issuerUri;

  /**
   * The json path in the JWT to the roles.
   */
  @NotEmpty
  private String rolesJsonPath = "$.realm_access.roles";

  /**
   * Specifies whether the roles value is a list (json array) or a simple string.
   */
  private boolean rolesValueList = true;

  /**
   * The role value separator to use if the role value is a simple string.
   */
  @NotNull
  private String rolesValueSeparator = " ";

  /**
   * The role prefix to add.
   */
  @NotNull
  private String rolePrefix = "ROLE_";

  /**
   * The json path in the JWT to the user name.
   */
  @NotEmpty
  private String nameJsonPath = "$.preferred_username";

  /**
   * The password flow properties for the actuator endpoints.
   */
  @NotNull
  private ActuatorPasswordFlow passwordFlow = new ActuatorPasswordFlow();

  /**
   * Gets unauthenticated endpoints or defaults (these are the health and info endpoints).
   *
   * @return the unauthenticated endpoints
   */
  @NotNull
  public List<Class<?>> unauthenticatedEndpointsOrDefaults() {
    if (unauthenticatedEndpoints.isEmpty()) {
      unauthenticatedEndpoints.add(
          org.springframework.boot.actuate.health.HealthEndpoint.class);
      unauthenticatedEndpoints.add(
          org.springframework.boot.actuate.info.InfoEndpoint.class);
    }
    return unauthenticatedEndpoints;
  }

  /**
   * Roles or defaults.
   *
   * @return the roles
   */
  @NotNull
  public Set<String> rolesOrDefaults() {
    final TreeSet<String> roleSet = new TreeSet<>(roles);
    if (roleSet.isEmpty()) {
      roleSet.add(AuthorityConstants.ACTUATOR_ROLE_NAME);
      roleSet.add(AuthorityConstants.ACTUATOR_ADMIN_ROLE_NAME);
      roleSet.add(AuthorityConstants.ADMIN_ROLE_NAME);
    }
    return roleSet.stream()
        .map(this::ensureRolePrefix)
        .collect(Collectors.toSet());
  }

  /**
   * Admin roles or defaults.
   *
   * @return the admin roles
   */
  @NotNull
  public Set<String> adminRolesOrDefaults() {
    final TreeSet<String> roleSet = new TreeSet<>(adminRoles);
    if (roleSet.isEmpty()) {
      roleSet.add(AuthorityConstants.ACTUATOR_ADMIN_ROLE_NAME);
      roleSet.add(AuthorityConstants.ADMIN_ROLE_NAME);
    }
    return roleSet.stream()
        .map(this::ensureRolePrefix)
        .collect(Collectors.toSet());
  }

  /**
   * Build access expression (SpEL) for actuator endpoints.
   *
   * @return the access expression (SpEL) for actuator endpoints
   */
  @NotNull
  public String buildAccessExpression() {
    return AccessExpressionUtils.hasAuthorityOrIpAddressExpr(
        rolesOrDefaults(), null, ipAddresses);
  }

  /**
   * Build access expression (SpEL) for admin actuator endpoints.
   *
   * @return the access expression (SpEL) for admin actuator endpoints
   */
  @NotNull
  public String buildAdminAccessExpression() {
    return AccessExpressionUtils.hasAuthorityOrIpAddressExpr(adminRolesOrDefaults(), null, null);
  }

  /**
   * Ensure role prefix.
   *
   * @param role the role
   * @return the role with prefix
   */
  @NotNull
  public String ensureRolePrefix(@NotNull String role) {
    final String prefix = rolePrefix.trim();
    return StringUtils.hasText(prefix) && role.startsWith(prefix) ? role : prefix + role;
  }

  /**
   * OAuth2 password flow configuration properties.
   */
  @Getter
  @Setter
  @ToString(exclude = {"clientSecret"})
  @EqualsAndHashCode(exclude = {"clientSecret"})
  public static class ActuatorPasswordFlow implements ClientCredentialsFlowProperties {

    private String tokenEndpoint;

    private String clientId;

    private String clientSecret;
  }
}
