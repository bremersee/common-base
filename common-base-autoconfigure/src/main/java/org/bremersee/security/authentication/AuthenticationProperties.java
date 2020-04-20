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

package org.bremersee.security.authentication;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bremersee.security.core.AuthorityConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Authentication properties.
 *
 * @author Christian Bremer
 */
@ConfigurationProperties(prefix = "bremersee.security.authentication")
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class AuthenticationProperties implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final String IS_AUTHENTICATED = "isAuthenticated()";

  private static final String HAS_AUTHORITY_TEMPLATE = "hasAuthority('%s')";

  private static final String HAS_ANY_AUTHORITY_TEMPLATE = "hasAnyAuthority(%s)";

  private static final String HAS_IP_ADDRESS_TEMPLATE = "hasIpAddress('%s')";

  /**
   * Specifies whether JWT beans should be created or not. Default is {@code false}.
   *
   * @see AuthenticationSupportAutoConfiguration
   * @see ReactiveAuthenticationSupportAutoConfiguration
   */
  private boolean enableJwtSupport = false;

  /**
   * The json path in the JWT to the roles.
   */
  private String rolesJsonPath = "$.realm_access.roles";

  /**
   * Specifies whether the roles value is a list (json array) or a simple string.
   */
  private boolean rolesValueList = true;

  /**
   * The role value separator to use if the role value is a simple string.
   */
  private String rolesValueSeparator = " ";

  /**
   * The role prefix to add.
   */
  private String rolePrefix = "ROLE_";

  /**
   * The json path in the JWT to the user name.
   */
  private String nameJsonPath = "$.preferred_username";

  /**
   * Properties for accessing the application.
   */
  private ApplicationAccessProperties application = new ApplicationAccessProperties();

  /**
   * Properties for actuator endpoints.
   */
  private ActuatorAccessProperties actuator = new ActuatorAccessProperties();

  /**
   * Properties for eureka endpoints.
   */
  private EurekaAccessProperties eureka = new EurekaAccessProperties();

  /**
   * The properties for the oauth2 password flow.
   */
  private PasswordFlow passwordFlow = new PasswordFlow();

  /**
   * The properties for the client credentials flow.
   */
  private ClientCredentialFlow clientCredentialsFlow = new ClientCredentialFlow();

  /**
   * A list of in-memory users, that can login with basic authentication for testing purposes.
   */
  private List<SimpleUser> basicAuthUsers = new ArrayList<>();

  /**
   * Gets role prefix.
   *
   * @return the role prefix
   */
  public String getRolePrefix() {
    return rolePrefix != null ? rolePrefix.trim() : "";
  }

  /**
   * Build basic auth user details.
   *
   * @return the user details
   */
  public UserDetails[] buildBasicAuthUserDetails() {
    final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    return getBasicAuthUsers().stream().map(
        simpleUser -> User.builder()
            .username(simpleUser.getName())
            .password(simpleUser.getPassword())
            .authorities(simpleUser.buildAuthorities())
            .passwordEncoder(encoder::encode)
            .build())
        .toArray(UserDetails[]::new);
  }

  /**
   * Ensure role prefix.
   *
   * @param role the role
   * @return the role with prefix
   */
  public String ensureRolePrefix(String role) {
    Assert.hasText(role, "Role must be present.");
    final String prefix = getRolePrefix();
    return StringUtils.hasText(prefix) && role.startsWith(prefix) ? role : prefix + role;
  }

  /**
   * Has authority expr string.
   *
   * @param role the role
   * @param ensurePrefixFunction the ensure prefix function
   * @return the string
   */
  public static String hasAuthorityExpr(String role,
      Function<String, String> ensurePrefixFunction) {
    return String.format(
        HAS_AUTHORITY_TEMPLATE,
        Optional.ofNullable(ensurePrefixFunction)
            .map(f -> f.apply(role))
            .orElse(role));
  }

  /**
   * Has any authority expr string.
   *
   * @param roles the roles
   * @param ensurePrefixFunction the ensure prefix function
   * @return the string
   */
  public static String hasAnyAuthorityExpr(
      Collection<String> roles,
      Function<String, String> ensurePrefixFunction) {

    return Optional.ofNullable(roles)
        .filter(list -> !list.isEmpty())
        .map(list -> list.stream()
            .map(role -> Optional.ofNullable(ensurePrefixFunction)
                .map(f -> f.apply(role))
                .orElse(role))
            .map(role -> "'" + role + "'")
            .collect(Collectors.joining(",")))
        .map(value -> String.format(HAS_ANY_AUTHORITY_TEMPLATE, value))
        .orElse("");
  }

  /**
   * Has ip address expr string.
   *
   * @param ip the ip
   * @return the string
   */
  public static String hasIpAddressExpr(String ip) {
    Assert.hasText(ip, "IP address must be present.");
    return String.format(HAS_IP_ADDRESS_TEMPLATE, ip);
  }

  /**
   * Has ip address expr string.
   *
   * @param ips the ips
   * @return the string
   */
  public static String hasIpAddressExpr(Collection<String> ips) {
    return Optional.ofNullable(ips)
        .map(list -> list.stream()
            .map(AuthenticationProperties::hasIpAddressExpr)
            .collect(Collectors.joining(" or ")))
        .orElse("");
  }

  /**
   * Has authority or ip address expr string.
   *
   * @param roles the roles
   * @param ips the ips
   * @param ensurePrefixFunction the ensure prefix function
   * @return the string
   */
  public static String hasAuthorityOrIpAddressExpr(
      Collection<String> roles,
      Collection<String> ips,
      Function<String, String> ensurePrefixFunction) {

    TreeSet<String> roleSet = roles instanceof TreeSet
        ? (TreeSet<String>) roles
        : (roles == null ? new TreeSet<>() : new TreeSet<>(roles));
    StringBuilder sb = new StringBuilder();
    if (roleSet.size() > 1) {
      sb.append(hasAnyAuthorityExpr(roleSet, ensurePrefixFunction));
    } else if (roleSet.size() == 1) {
      sb.append(hasAuthorityExpr(roleSet.first(), ensurePrefixFunction));
    }
    Set<String> ipSet = ips instanceof Set
        ? (Set<String>) ips
        : (ips == null ? Collections.emptySet() : new LinkedHashSet<>(ips));
    if (!ipSet.isEmpty()) {
      if (sb.length() > 0) {
        sb.append(" or ");
      }
      sb.append(hasIpAddressExpr(ipSet));
    }
    return sb.toString();
  }

  /**
   * The application access properties.
   */
  @Getter
  @Setter
  @ToString
  @EqualsAndHashCode
  @NoArgsConstructor
  public static class ApplicationAccessProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    private String defaultAccessExpression = IS_AUTHENTICATED;

    private List<String> ipAddresses = new ArrayList<>();

    private List<String> userRoles = new ArrayList<>();

    private List<String> adminRoles = new ArrayList<>();

    /**
     * Gets default access expression.
     *
     * @return the default access expression
     */
    public String getDefaultAccessExpression() {
      return StringUtils.hasText(defaultAccessExpression)
          ? defaultAccessExpression
          : IS_AUTHENTICATED;
    }

    /**
     * User roles or defaults list.
     *
     * @param defaultRoles the default roles
     * @return the list
     */
    public List<String> userRolesOrDefaults(String... defaultRoles) {
      return !userRoles.isEmpty() || defaultRoles == null
          ? userRoles
          : Arrays.asList(defaultRoles);
    }

    /**
     * Admin roles or defaults list.
     *
     * @param defaultRoles the default roles
     * @return the list
     */
    public List<String> adminRolesOrDefaults(String... defaultRoles) {
      return !adminRoles.isEmpty() || defaultRoles == null
          ? adminRoles
          : Arrays.asList(defaultRoles);
    }

    /**
     * Build access expression string.
     *
     * @param withDefaultExpression the default expression
     * @param withIpAddresses the with ip addresses
     * @param withUserRoles the with user roles
     * @param withAdminRoles the with admin roles
     * @param ensurePrefixFunction function to ensure role prefix
     * @param defaultRoles the default roles
     * @return the string
     */
    public String buildAccessExpression(
        boolean withDefaultExpression,
        boolean withIpAddresses,
        boolean withUserRoles,
        boolean withAdminRoles,
        Function<String, String> ensurePrefixFunction,
        String... defaultRoles) {

      Set<String> ips = withIpAddresses ? new LinkedHashSet<>(ipAddresses) : Collections.emptySet();
      TreeSet<String> roles = withAdminRoles
          ? new TreeSet<>(adminRolesOrDefaults(defaultRoles))
          : new TreeSet<>();
      if (withUserRoles) {
        roles.addAll(userRolesOrDefaults(defaultRoles));
      }
      if (ips.isEmpty() && roles.isEmpty()) {
        return getDefaultAccessExpression();
      }
      String expr = hasAuthorityOrIpAddressExpr(roles, ips, ensurePrefixFunction);
      if (withDefaultExpression && !expr.contains(getDefaultAccessExpression())) {
        expr = expr + " or " + getDefaultAccessExpression();
      }
      return expr;
    }
  }

  /**
   * The actuator access properties.
   */
  @Getter
  @Setter
  @ToString
  @EqualsAndHashCode
  @NoArgsConstructor
  public static class ActuatorAccessProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The roles which can write to protected actuator endpoints. The role names normally start with
     * {@code ROLE_}.
     */
    private List<String> adminRoles = new ArrayList<>();

    /**
     * The roles which can read protected actuator endpoints. The role names normally start with
     * {@code ROLE_}.
     */
    private List<String> roles = new ArrayList<>();

    /**
     * The IP addresses which can access protected actuator endpoints without authentication.
     */
    private List<String> ipAddresses = new ArrayList<>();

    /**
     * Build access expression (SpEL) for actuator endpoints.
     *
     * @param ensurePrefixFunction function to ensure role prefix
     * @return the access expression (SpEL) for actuator endpoints
     */
    public String buildAccessExpression(Function<String, String> ensurePrefixFunction) {
      final TreeSet<String> roleSet = new TreeSet<>(roles);
      if (roleSet.isEmpty()) {
        roleSet.add(AuthorityConstants.ACTUATOR_ROLE_NAME);
        roleSet.add(AuthorityConstants.ADMIN_ROLE_NAME);
      }
      return hasAuthorityOrIpAddressExpr(roleSet, ipAddresses, ensurePrefixFunction);
    }

    /**
     * Build access expression (SpEL) for admin actuator endpoints.
     *
     * @param ensurePrefixFunction function to ensure role prefix
     * @return the access expression (SpEL) for admin actuator endpoints
     */
    public String buildAdminAccessExpression(Function<String, String> ensurePrefixFunction) {
      final Set<String> roleSet = new TreeSet<>(adminRoles);
      if (roleSet.isEmpty()) {
        roleSet.add(AuthorityConstants.ACTUATOR_ADMIN_ROLE_NAME);
        roleSet.add(AuthorityConstants.ADMIN_ROLE_NAME);
      }
      return hasAuthorityOrIpAddressExpr(roleSet, null, ensurePrefixFunction);
    }

  }

  /**
   * The eureka access properties.
   */
  @Getter
  @Setter
  @ToString(exclude = {"password"})
  @EqualsAndHashCode
  @NoArgsConstructor
  public static class EurekaAccessProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;

    private String password;

    private String role = AuthorityConstants.EUREKA_ROLE_NAME;

    /**
     * The IP addresses which can access protected eureka endpoints without authentication.
     */
    private List<String> ipAddresses = new ArrayList<>();

    /**
     * Build access expression.
     *
     * @param ensurePrefixFunction function to ensure role prefix
     * @return the access expression
     */
    public String buildAccessExpression(Function<String, String> ensurePrefixFunction) {
      if (StringUtils.hasText(role)) {
        return hasAuthorityOrIpAddressExpr(
            Collections.singleton(role),
            ipAddresses,
            ensurePrefixFunction);
      }
      String ipsExpr = hasIpAddressExpr(ipAddresses);
      return StringUtils.hasText(ipsExpr) ? IS_AUTHENTICATED + " or " + ipsExpr : IS_AUTHENTICATED;
    }

    /**
     * Build basic auth user details.
     *
     * @param otherUserDetails the other user details
     * @return the user details
     */
    public UserDetails[] buildBasicAuthUserDetails(UserDetails... otherUserDetails) {
      if (!StringUtils.hasText(username)) {
        return Optional.ofNullable(otherUserDetails).orElse(new UserDetails[0]);
      }
      final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
      final SimpleUser user = new SimpleUser();
      user.setName(username);
      user.setPassword(StringUtils.hasText(password) ? password : "");
      if (StringUtils.hasText(role)) {
        user.setAuthorities(Collections.singletonList(role));
      }
      final UserDetails userDetails = User.builder()
          .username(user.getName())
          .password(user.getPassword())
          .authorities(user.buildAuthorities())
          .passwordEncoder(encoder::encode)
          .build();
      List<UserDetails> users = new ArrayList<>();
      if (otherUserDetails != null) {
        users.addAll(Arrays.asList(otherUserDetails));
      }
      users.add(userDetails);
      return users.toArray(new UserDetails[0]);
    }
  }

  /**
   * The client credential flow.
   */
  @Getter
  @Setter
  @ToString(exclude = {"clientSecret"})
  @EqualsAndHashCode(exclude = {"clientSecret"})
  public static class ClientCredentialFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tokenEndpoint;

    private String clientId;

    private String clientSecret;

    /**
     * To properties client credentials flow properties.
     *
     * @return the client credentials flow properties
     */
    public ClientCredentialsFlowProperties toProperties() {
      return ClientCredentialsFlowProperties.builder()
          .tokenEndpoint(tokenEndpoint)
          .clientId(clientId)
          .clientSecret(clientSecret)
          .build();
    }
  }

  /**
   * OAuth2 password flow configuration properties.
   */
  @Getter
  @Setter
  @ToString(exclude = {"clientSecret", "systemPassword"})
  @EqualsAndHashCode(exclude = {"clientSecret", "systemPassword"})
  public static class PasswordFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tokenEndpoint;

    private String clientId;

    private String clientSecret;

    private String systemUsername;

    private String systemPassword;

    /**
     * Create password flow properties for the system user.
     *
     * @return the password flow properties
     */
    public PasswordFlowProperties toProperties() {
      return this.toProperties(systemUsername, systemPassword);
    }

    /**
     * Create password flow properties for the given user.
     *
     * @param username the username
     * @param password the password
     * @return the password flow properties
     */
    public PasswordFlowProperties toProperties(String username, String password) {
      return PasswordFlowProperties.builder()
          .username(username)
          .clientSecret(clientSecret)
          .clientId(clientId)
          .tokenEndpoint(tokenEndpoint)
          .password(password)
          .build();
    }
  }

  /**
   * A simple user.
   */
  @Getter
  @Setter
  @ToString(exclude = "password")
  @EqualsAndHashCode(exclude = "password")
  @NoArgsConstructor
  public static class SimpleUser implements Serializable, Principal {

    private static final long serialVersionUID = 1L;

    /**
     * The user name of the user.
     */
    private String name;

    /**
     * The password of the user.
     */
    private String password;

    /**
     * The granted authorities of the user. The names of the authorities starts with {@code ROLE_}.
     */
    private List<String> authorities = new ArrayList<>();

    /**
     * Build authorities.
     *
     * @return the authorities
     */
    String[] buildAuthorities() {
      if (authorities == null || authorities.isEmpty()) {
        return new String[]{
            AuthorityConstants.USER_ROLE_NAME
        };
      }
      return authorities.toArray(new String[0]);
    }
  }

}
