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

package org.bremersee.security;

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
import org.bremersee.security.authentication.AuthenticationSupportAutoConfiguration;
import org.bremersee.security.authentication.ClientCredentialsFlowProperties;
import org.bremersee.security.authentication.ClientCredentialsFlowPropertiesProvider;
import org.bremersee.security.authentication.PasswordFlowProperties;
import org.bremersee.security.authentication.PasswordFlowPropertiesProvider;
import org.bremersee.security.authentication.ReactiveAuthenticationSupportAutoConfiguration;
import org.bremersee.security.core.AuthorityConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Security properties.
 *
 * @author Christian Bremer
 */
@ConfigurationProperties(prefix = "bremersee.security")
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class SecurityProperties {

  /**
   * The cors properties.
   */
  private CorsProperties cors = new CorsProperties();

  /**
   * The authentication properties.
   */
  private AuthenticationProperties authentication = new AuthenticationProperties();

  /**
   * Authentication properties.
   *
   * @author Christian Bremer
   */
  @Getter
  @Setter
  @ToString
  @EqualsAndHashCode
  public static class AuthenticationProperties {

    private static final String IS_AUTHENTICATED = "isAuthenticated()";

    private static final String HAS_AUTHORITY_TEMPLATE = "hasAuthority('%s')";

    private static final String HAS_ANY_AUTHORITY_TEMPLATE = "hasAnyAuthority(%s)";

    private static final String HAS_IP_ADDRESS_TEMPLATE = "hasIpAddress('%s')";

    private static final PasswordEncoder passwordEncoder = PasswordEncoderFactories
        .createDelegatingPasswordEncoder();

    /**
     * Specifies whether JWT beans should be created or not. Default is {@code false}.
     *
     * @see AuthenticationSupportAutoConfiguration
     * @see ReactiveAuthenticationSupportAutoConfiguration
     */
    private boolean enableJwtSupport = false;

    private boolean resourceServerAutoConfiguration = false;

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
     * The password encoder.
     *
     * @return the password encoder
     */
    public PasswordEncoder passwordEncoder() {
      return passwordEncoder;
    }

    /**
     * Build user details from basic auth users.
     *
     * @return the user details
     */
    public UserDetails[] buildBasicAuthUserDetails() {
      return getBasicAuthUsers().stream().map(
          simpleUser -> User.builder()
              .username(simpleUser.getName())
              .password(simpleUser.getPassword())
              .authorities(simpleUser.buildAuthorities())
              .passwordEncoder(passwordEncoder()::encode)
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
     * Builds the {@code hasAuthority} expression.
     *
     * @param role the role
     * @param ensurePrefixFunction the ensure prefix function
     * @return the {@code hasAuthority} expression
     */
    public static String hasAuthorityExpr(
        String role,
        Function<String, String> ensurePrefixFunction) {

      return String.format(
          HAS_AUTHORITY_TEMPLATE,
          Optional.ofNullable(ensurePrefixFunction)
              .map(f -> f.apply(role))
              .orElse(role));
    }

    /**
     * Builds the {@code hasAnyAuthority} expression.
     *
     * @param roles the roles
     * @param ensurePrefixFunction the ensure prefix function
     * @return the {@code hasAnyAuthority} expression
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
     * Builds the {@code hasIpAddress} expression.
     *
     * @param ip the ip
     * @return the {@code hasIpAddress} expression
     */
    public static String hasIpAddressExpr(String ip) {
      Assert.hasText(ip, "IP address must be present.");
      return String.format(HAS_IP_ADDRESS_TEMPLATE, ip);
    }

    /**
     * Builds the {@code hasIpAddress} expression.
     *
     * @param ips the ips
     * @return the {@code hasIpAddress} expression
     */
    public static String hasIpAddressExpr(Collection<String> ips) {
      return Optional.ofNullable(ips)
          .map(list -> list.stream()
              .map(AuthenticationProperties::hasIpAddressExpr)
              .collect(Collectors.joining(" or ")))
          .orElse("");
    }

    /**
     * Builds an access expression from the given roles and ip addresses.
     *
     * @param roles the roles
     * @param ips the ips
     * @param ensurePrefixFunction the ensure prefix function
     * @return the access expression
     */
    public static String hasAuthorityOrIpAddressExpr(
        Collection<String> roles,
        Collection<String> ips,
        Function<String, String> ensurePrefixFunction) {

      TreeSet<String> roleSet = roles instanceof TreeSet
          ? (TreeSet<String>) roles
          : roles == null ? new TreeSet<>() : new TreeSet<>(roles);
      StringBuilder sb = new StringBuilder();
      if (roleSet.size() > 1) {
        sb.append(hasAnyAuthorityExpr(roleSet, ensurePrefixFunction));
      } else if (roleSet.size() == 1) {
        sb.append(hasAuthorityExpr(roleSet.first(), ensurePrefixFunction));
      }
      Set<String> ipSet = ips instanceof Set
          ? (Set<String>) ips
          : ips == null ? Collections.emptySet() : new LinkedHashSet<>(ips);
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
    public static class ApplicationAccessProperties {

      public static final String ALL_HTTP_METHODS = "*";

      private String defaultAccessExpression = IS_AUTHENTICATED;

      private List<String> ipAddresses = new ArrayList<>();

      private List<String> userRoles = new ArrayList<>();

      private List<String> adminRoles = new ArrayList<>();

      private List<PathMatcherProperties> permitAllMatchers = new ArrayList<>();

      private List<PathMatcherProperties> adminMatchers = new ArrayList<>();

      private List<PathMatcherProperties> userMatchers = new ArrayList<>();

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

      private Set<String> rolesOrDefaults(
          Collection<String> roleNames,
          Function<String, String> ensureRolePrefixFunction,
          String... defaultRoles) {

        TreeSet<String> roles = new TreeSet<>(roleNames);
        if (roles.isEmpty() && defaultRoles != null) {
          roles.addAll(Arrays.asList(defaultRoles));
        }
        if (ensureRolePrefixFunction != null) {
          return roles.stream()
              .map(ensureRolePrefixFunction)
              .collect(Collectors.toSet());
        }
        return roles;
      }

      /**
       * User roles or defaults.
       *
       * @param ensureRolePrefixFunction the ensure role prefix function
       * @param defaultRoles the default roles
       * @return the user roles
       */
      public Set<String> userRolesOrDefaults(
          Function<String, String> ensureRolePrefixFunction,
          String... defaultRoles) {
        return rolesOrDefaults(userRoles, ensureRolePrefixFunction, defaultRoles);
      }

      /**
       * Admin roles or defaults list.
       *
       * @param ensureRolePrefixFunction the ensure role prefix function
       * @param defaultRoles the default roles
       * @return the list
       */
      public Set<String> adminRolesOrDefaults(
          Function<String, String> ensureRolePrefixFunction,
          String... defaultRoles) {
        return rolesOrDefaults(adminRoles, ensureRolePrefixFunction, defaultRoles);
      }

      /**
       * Builds access expression.
       *
       * @param roles the roles
       * @param ipAddresses the ip addresses
       * @param addDefaultExpressionAlways specifies whether the default expression should be
       *     added always
       * @param defaultExpressionConcatenation the concatenation value, default is {@code 'or'}
       * @return the access expression
       */
      public String buildAccessExpression(
          Collection<String> roles,
          Collection<String> ipAddresses,
          boolean addDefaultExpressionAlways,
          String defaultExpressionConcatenation) {

        StringBuilder sb = new StringBuilder();
        sb.append(hasAuthorityOrIpAddressExpr(roles, ipAddresses, null));
        if (roles == null || roles.isEmpty() || addDefaultExpressionAlways) {
          if (sb.length() > 0) {
            String concatenation = StringUtils.hasText(defaultExpressionConcatenation)
                ? defaultExpressionConcatenation
                : "or";
            if (!concatenation.startsWith(" ")) {
              sb.append(" ");
            }
            sb.append(concatenation);
            if (!concatenation.endsWith(" ")) {
              sb.append(" ");
            }
          }
          sb.append(defaultAccessExpression);
        }
        return sb.toString();
      }

      @Setter
      @EqualsAndHashCode
      @NoArgsConstructor
      public static class PathMatcherProperties implements Comparable<PathMatcherProperties> {

        private String httpMethod = ALL_HTTP_METHODS;

        private String antPattern = "/**";

        public String getHttpMethod() {
          if (StringUtils.hasText(httpMethod)
              && HttpMethod.resolve(httpMethod.toUpperCase()) != null) {
            return httpMethod.toUpperCase();
          }
          return ALL_HTTP_METHODS;
        }

        public String getAntPattern() {
          return StringUtils.hasText(antPattern) ? antPattern : "/**";
        }

        public HttpMethod httpMethod() {
          return HttpMethod.resolve(getHttpMethod());
        }

        @Override
        public int compareTo(PathMatcherProperties o) {
          String o1 = getHttpMethod();
          String o2 = o.getHttpMethod();
          int result = o1.compareTo(o2);
          if (result == 0) {
            return getAntPattern().compareToIgnoreCase(o.getAntPattern());
          }
          if (o1.equals(ALL_HTTP_METHODS)) {
            return 1;
          }
          if (o2.equals(ALL_HTTP_METHODS)) {
            return -1;
          }
          return result;
        }

        @Override
        public String toString() {
          if (ALL_HTTP_METHODS.equals(getHttpMethod())) {
            return getAntPattern();
          }
          return getHttpMethod() + ": " + getAntPattern();
        }
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
    public static class EurekaAccessProperties {

      private String username;

      private String password;

      private String role = AuthorityConstants.EUREKA_ROLE_NAME;

      /**
       * The IP addresses which can access protected eureka endpoints without authentication.
       */
      private List<String> ipAddresses = new ArrayList<>();

      /**
       * Returns the role.
       *
       * @param ensureRolePrefixFunction function to ensure role prefix
       * @return the role
       */
      public String role(Function<String, String> ensureRolePrefixFunction) {
        return Optional.ofNullable(ensureRolePrefixFunction)
            .map(func -> StringUtils.hasText(role) ? func.apply(role) : "")
            .orElse(role);
      }

      /**
       * Build access expression.
       *
       * @param ensureRolePrefixFunction function to ensure role prefix
       * @return the access expression
       */
      public String buildAccessExpression(Function<String, String> ensureRolePrefixFunction) {
        if (StringUtils.hasText(role)) {
          return hasAuthorityOrIpAddressExpr(
              Collections.singleton(role),
              ipAddresses,
              ensureRolePrefixFunction);
        }
        String ipsExpr = hasIpAddressExpr(ipAddresses);
        return StringUtils.hasText(ipsExpr)
            ? IS_AUTHENTICATED + " or " + ipsExpr
            : IS_AUTHENTICATED;
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
    public static class ClientCredentialFlow implements ClientCredentialsFlowPropertiesProvider {

      private String tokenEndpoint;

      private String clientId;

      private String clientSecret;

      @Override
      public ClientCredentialsFlowProperties toClientCredentialsFlowProperties() {
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
    public static class PasswordFlow implements PasswordFlowPropertiesProvider,
        ClientCredentialsFlowPropertiesProvider {

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
      public PasswordFlowProperties toPasswordFlowProperties() {
        return this.toPasswordFlowProperties(systemUsername, systemPassword);
      }

      @Override
      public PasswordFlowProperties toPasswordFlowProperties(String username, String password) {
        return PasswordFlowProperties.builder()
            .username(username)
            .clientSecret(clientSecret)
            .clientId(clientId)
            .tokenEndpoint(tokenEndpoint)
            .password(password)
            .build();
      }

      @Override
      public ClientCredentialsFlowProperties toClientCredentialsFlowProperties() {
        return ClientCredentialsFlowProperties.builder()
            .tokenEndpoint(tokenEndpoint)
            .clientId(clientId)
            .clientSecret(clientSecret)
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
       * The granted authorities of the user. The names of the authorities starts with {@code
       * ROLE_}.
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

  /**
   * The type Cors properties.
   */
  @Setter
  @ToString
  @EqualsAndHashCode
  public static class CorsProperties {

    @Getter
    private boolean disabled;

    @Getter
    private boolean allowAll = true;

    private List<CorsConfiguration> configs = new ArrayList<>();

    /**
     * Allow all configuration.
     *
     * @return the allow all configuration
     */
    public static List<CorsConfiguration> allowAllConfiguration() {
      return Collections.singletonList(CorsConfiguration.allowAllConfiguration());
    }

    /**
     * Gets configs.
     *
     * @return the configs
     */
    public List<CorsConfiguration> getConfigs() {
      if (configs == null) {
        configs = new ArrayList<>();
      }
      if (!disabled && allowAll && configs.isEmpty()) {
        return allowAllConfiguration();
      }
      return configs.stream()
          .filter(config -> StringUtils.hasText(config.getPathPattern()))
          .collect(Collectors.toList());
    }

    /**
     * The cors configuration.
     */
    @Setter
    @ToString
    @EqualsAndHashCode
    public static class CorsConfiguration {

      @Getter
      private String pathPattern;

      private List<String> allowedOrigins = new ArrayList<>();

      private List<String> allowedMethods = new ArrayList<>();

      private List<String> allowedHeaders = new ArrayList<>();

      @Getter
      private List<String> exposedHeaders = new ArrayList<>();

      @Getter
      private boolean allowCredentials;

      @Getter
      private long maxAge = 1800L;

      /**
       * Allow all configuration.
       *
       * @return the allow all configuration
       */
      static CorsConfiguration allowAllConfiguration() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.pathPattern = "/**";
        configuration.allowedOrigins = Collections.singletonList("*");
        configuration.allowedMethods = Collections.singletonList("*");
        configuration.allowedHeaders = Collections.singletonList("*");
        return configuration;
      }

      /**
       * Gets allowed origins.
       *
       * @return the allowed origins
       */
      public List<String> getAllowedOrigins() {
        if (allowedOrigins == null) {
          allowedOrigins = new ArrayList<>();
        }
        if (allowedOrigins.isEmpty()) {
          allowedOrigins.add("*");
        }
        return allowedOrigins;
      }

      /**
       * Gets allowed methods.
       *
       * @return the allowed methods
       */
      public List<String> getAllowedMethods() {
        if (allowedMethods == null) {
          allowedMethods = new ArrayList<>();
        }
        if (allowedMethods.isEmpty()) {
          // allowedMethods.add(HttpMethod.GET.name());
          // allowedMethods.add(HttpMethod.POST.name());
          // allowedMethods.add(HttpMethod.HEAD.name());
          allowedMethods.add("*");
        }
        return allowedMethods;
      }

      /**
       * Gets allowed headers.
       *
       * @return the allowed headers
       */
      public List<String> getAllowedHeaders() {
        if (allowedHeaders == null) {
          allowedHeaders = new ArrayList<>();
        }
        if (allowedHeaders.isEmpty()) {
          allowedHeaders.add("*");
        }
        return allowedHeaders;
      }
    }

  }
}
