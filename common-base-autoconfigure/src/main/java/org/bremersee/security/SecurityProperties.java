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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
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
  @ToString(exclude = {"roleDefinitions", "ipDefinitions"})
  @EqualsAndHashCode
  public static class AuthenticationProperties {

    private static final PasswordEncoder passwordEncoder = PasswordEncoderFactories
        .createDelegatingPasswordEncoder();

    /**
     * Specifies whether the security of a resource server should be done automatically or not.
     * Default is {@code false}.
     */
    private boolean resourceServerAutoConfiguration = false;

    /**
     * Specifies whether JWT beans should be created or not. Default is {@code false}.
     *
     * @see AuthenticationSupportAutoConfiguration
     * @see ReactiveAuthenticationSupportAutoConfiguration
     */
    private boolean enableJwtSupport = false;

    /**
     * The role prefix to add.
     */
    private String rolePrefix = "ROLE_";

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
     * The json path in the JWT to the user name.
     */
    private String nameJsonPath = "$.preferred_username";

    private Map<String, List<String>> roleDefinitions = new LinkedHashMap<>();

    private Map<String, List<String>> ipDefinitions = new LinkedHashMap<>();

    private List<PathMatcherProperties> pathMatchers = new ArrayList<>();

    private AccessMode anyAccessMode = AccessMode.AUTHENTICATED;

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
    private ClientCredentialsFlow clientCredentialsFlow = new ClientCredentialsFlow();

    /**
     * A list of in-memory users, that can login with basic authentication for testing purposes.
     */
    private List<SimpleUser> basicAuthUsers = new ArrayList<>();

    /**
     * Path matchers set.
     *
     * @return the set
     */
    public Set<PathMatcherProperties> pathMatchers() {
      boolean containsAny = false;
      TreeSet<PathMatcherProperties> pathMatcherSet = new TreeSet<>();
      for (PathMatcherProperties props : pathMatchers) {
        pathMatcherSet.add(props);
        containsAny = containsAny || (PathMatcherProperties.ANY_PATH.equals(props.getAntPattern())
            && PathMatcherProperties.ALL_HTTP_METHODS.equals(props.getHttpMethod()));
      }
      if (!containsAny) {
        PathMatcherProperties any = new PathMatcherProperties();
        any.setAccessMode(anyAccessMode);
        pathMatcherSet.add(any);
      }
      return pathMatcherSet;
    }

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
     * The access mode.
     */
    public enum AccessMode {

      /**
       * Permit all access mode.
       */
      PERMIT_ALL(AccessExpressionUtils.PERMIT_ALL),

      /**
       * Authenticated access mode.
       */
      AUTHENTICATED(AccessExpressionUtils.IS_AUTHENTICATED),

      /**
       * Deny all access mode.
       */
      DENY_ALL(AccessExpressionUtils.DENY_ALL);

      @Getter
      private String expressionValue;

      AccessMode(String expressionValue) {
        this.expressionValue = expressionValue;
      }
    }

    /**
     * The type Path matcher properties.
     */
    @Setter
    @EqualsAndHashCode(exclude = {"accessMode", "roles", "ipAddresses"})
    @NoArgsConstructor
    public static class PathMatcherProperties implements Comparable<PathMatcherProperties> {

      /**
       * The constant ALL_HTTP_METHODS.
       */
      public static final String ALL_HTTP_METHODS = "*";

      /**
       * The constant ANY_PATH.
       */
      public static final String ANY_PATH = "/**";

      private String httpMethod = ALL_HTTP_METHODS;

      private String antPattern = ANY_PATH;

      @Getter
      private AccessMode accessMode = AccessMode.AUTHENTICATED;

      @Getter
      private List<String> roles = new ArrayList<>();

      @Getter
      private List<String> ipAddresses = new ArrayList<>();

      /**
       * Gets http method.
       *
       * @return the http method
       */
      public String getHttpMethod() {
        if (StringUtils.hasText(httpMethod)
            && HttpMethod.resolve(httpMethod.toUpperCase()) != null) {
          return httpMethod.toUpperCase();
        }
        return ALL_HTTP_METHODS;
      }

      /**
       * Gets ant pattern.
       *
       * @return the ant pattern
       */
      public String getAntPattern() {
        return StringUtils.hasText(antPattern) ? antPattern : ANY_PATH;
      }

      /**
       * Http method http method.
       *
       * @return the http method
       */
      public HttpMethod httpMethod() {
        return HttpMethod.resolve(getHttpMethod());
      }

      private int countPathSegments() {
        return new StringTokenizer(getAntPattern(), "/").countTokens();
      }

      /**
       * Access expression string.
       *
       * @param ensureRolePrefixFunction the ensure role prefix function
       * @return the string
       */
      public String accessExpression(Function<String, String> ensureRolePrefixFunction) {
        return AccessExpressionUtils.buildAccessExpression(this, ensureRolePrefixFunction);
      }

      /**
       * Roles set.
       *
       * @param ensureRolePrefixFunction the ensure role prefix function
       * @return the set
       */
      public Set<String> roles(Function<String, String> ensureRolePrefixFunction) {
        return roles.stream()
            .filter(StringUtils::hasText)
            .map(role -> ensureRolePrefixFunction != null
                ? ensureRolePrefixFunction.apply(role)
                : role)
            .collect(Collectors.toSet());
      }

      @Override
      public int compareTo(PathMatcherProperties o) {

        // 1.: compare the size of the path segments
        int c1 = countPathSegments();
        int c2 = o.countPathSegments();
        int result = Integer.compare(c2, c1);
        if (result != 0) {
          return result;
        }

        // 2.: compare the ant path
        result = getAntPattern().compareToIgnoreCase(o.getAntPattern());
        if (result != 0) {
          if (ANY_PATH.equals(getAntPattern())) {
            return 1;
          }
          if (ANY_PATH.equals(o.getAntPattern())) {
            return -1;
          }
          return result;
        }

        // 3. compare the http method
        String m1 = getHttpMethod();
        String m2 = o.getHttpMethod();
        result = m1.compareTo(m2);
        if (result != 0) {
          if (m1.equals(ALL_HTTP_METHODS)) {
            return 1;
          }
          if (m2.equals(ALL_HTTP_METHODS)) {
            return -1;
          }
        }
        return result;
      }

      @Override
      public String toString() {
        final String path;
        if (ALL_HTTP_METHODS.equals(getHttpMethod())) {
          path = getAntPattern();
        } else {
          path = getHttpMethod() + ": " + getAntPattern();
        }
        return path + " with access = " + accessExpression(null);
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
        return AccessExpressionUtils.buildAccessExpression(this, ensureRolePrefixFunction);
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
    public static class ClientCredentialsFlow implements ClientCredentialsFlowPropertiesProvider {

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
    private boolean enable = true;

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
      if (enable && allowAll && configs.isEmpty()) {
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
