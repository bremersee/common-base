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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bremersee.security.core.AuthorityConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

/**
 * Authentication and authorization properties.
 *
 * @author Christian Bremer
 */
@ConfigurationProperties(prefix = "bremersee.auth")
@Getter
@Setter
@ToString(exclude = {"roleDefinitions", "ipDefinitions"})
@EqualsAndHashCode
@Validated
public class AuthProperties {

  /**
   * Specifies the behaviour of the resource server security auto configuration.
   */
  @NotNull
  private AutoSecurityMode resourceServer = AutoSecurityMode.OTHER;

  /**
   * The order of the resource server security auto configuration.
   */
  private int resourceServerOrder = 51;

  /**
   * The role prefix to add.
   */
  @NotNull
  private String rolePrefix = "ROLE_";

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
   * The json path in the JWT to the user name.
   */
  @NotEmpty
  private String nameJsonPath = "$.preferred_username";

  @NotNull
  private Map<String, List<String>> roleDefinitions = new LinkedHashMap<>();

  @NotNull
  private Map<String, List<String>> ipDefinitions = new LinkedHashMap<>();

  @NotNull
  private List<PathMatcherProperties> pathMatchers = new ArrayList<>();

  @NotNull
  private AccessMode anyAccessMode = AccessMode.AUTHENTICATED;

  /**
   * Properties for eureka endpoints.
   */
  @NotNull
  private EurekaAccessProperties eureka = new EurekaAccessProperties();

  /**
   * The properties for the oauth2 password flow.
   */
  @NotNull
  private PasswordFlow passwordFlow = new PasswordFlow();

  /**
   * The properties for the client credentials flow.
   */
  @NotNull
  private ClientCredentialsFlow clientCredentialsFlow = new ClientCredentialsFlow();

  /**
   * A list of in-memory users, that can login with basic authentication for testing purposes.
   */
  @NotNull
  private List<SimpleUser> inMemoryUsers = new ArrayList<>();

  /**
   * Path matchers.
   *
   * @return the path matchers
   */
  @NotNull
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
   * Build user details from in memory users.
   *
   * @param passwordEncoder the password encoder
   * @return the user details
   */
  @NotNull
  public UserDetails[] buildBasicAuthUserDetails(@Nullable PasswordEncoder passwordEncoder) {
    final PasswordEncoder encoder = Optional.ofNullable(passwordEncoder)
        .orElseGet(PasswordEncoderFactories::createDelegatingPasswordEncoder);
    return getInMemoryUsers().stream().map(
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
  @NotEmpty
  public String ensureRolePrefix(@NotEmpty String role) {
    final String prefix = rolePrefix.trim();
    return StringUtils.hasText(prefix) && role.startsWith(prefix) ? role : prefix + role;
  }

  /**
   * The path matcher properties.
   */
  @Setter
  @EqualsAndHashCode(exclude = {"accessMode", "roles", "ipAddresses"})
  @NoArgsConstructor
  @Validated
  public static class PathMatcherProperties implements Comparable<PathMatcherProperties> {

    /**
     * The constant ALL_HTTP_METHODS.
     */
    public static final String ALL_HTTP_METHODS = "*";

    /**
     * The constant ANY_PATH.
     */
    public static final String ANY_PATH = "/**";

    @NotNull
    private String httpMethod = ALL_HTTP_METHODS;

    @NotNull
    private String antPattern = ANY_PATH;

    @Getter
    @NotNull
    private AccessMode accessMode = AccessMode.AUTHENTICATED;

    @Getter
    @NotNull
    private List<String> roles = new ArrayList<>();

    @Getter
    @NotNull
    private List<String> ipAddresses = new ArrayList<>();

    /**
     * Gets http method.
     *
     * @return the http method
     */
    @NotEmpty
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
    @NotEmpty
    public String getAntPattern() {
      return StringUtils.hasText(antPattern) ? antPattern : ANY_PATH;
    }

    /**
     * Http method http method.
     *
     * @return the http method
     */
    @Nullable
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
    @NotEmpty
    public String accessExpression(
        @Nullable Function<String, String> ensureRolePrefixFunction) {
      return AccessExpressionUtils.buildAccessExpression(this, ensureRolePrefixFunction);
    }

    /**
     * Returns valid roles.
     *
     * @param ensureRolePrefixFunction the ensure role prefix function
     * @return the valid roles
     */
    public Set<String> roles(@Nullable Function<String, String> ensureRolePrefixFunction) {
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
  @Validated
  public static class EurekaAccessProperties {

    private String username;

    private String password;

    private String role = AuthorityConstants.EUREKA_ROLE_NAME;

    /**
     * The IP addresses which can access protected eureka endpoints without authentication.
     */
    @NotNull
    private List<String> ipAddresses = new ArrayList<>();

    /**
     * Returns the role.
     *
     * @param ensureRolePrefixFunction function to ensure role prefix
     * @return the role
     */
    @Nullable
    public String role(@Nullable Function<String, String> ensureRolePrefixFunction) {
      return Optional.ofNullable(ensureRolePrefixFunction)
          .map(func -> StringUtils.hasText(role) ? func.apply(role) : role)
          .orElse(role);
    }

    /**
     * Build access expression.
     *
     * @param ensureRolePrefixFunction function to ensure role prefix
     * @return the access expression
     */
    @NotEmpty
    public String buildAccessExpression(
        @Nullable Function<String, String> ensureRolePrefixFunction) {
      return AccessExpressionUtils.buildAccessExpression(this, ensureRolePrefixFunction);
    }

    /**
     * Build basic auth user details.
     *
     * @param otherUserDetails the other user details
     * @return the user details
     */
    @NotNull
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
   * The client credentials flow.
   */
  @Getter
  @Setter
  @ToString(exclude = {"clientSecret"})
  @EqualsAndHashCode(exclude = {"clientSecret"})
  public static class ClientCredentialsFlow implements ClientCredentialsFlowProperties {

    private String tokenEndpoint;

    private String clientId;

    private String clientSecret;
  }

  /**
   * OAuth2 password flow configuration properties.
   */
  @Getter
  @Setter
  @ToString(exclude = {"clientSecret", "password"})
  @EqualsAndHashCode(exclude = {"clientSecret", "password"})
  public static class PasswordFlow implements PasswordFlowProperties {

    private String tokenEndpoint;

    private String clientId;

    private String clientSecret;

    private String username;

    private String password;
  }

  /**
   * A simple user.
   */
  @Getter
  @Setter
  @ToString(exclude = "password")
  @EqualsAndHashCode(exclude = "password")
  @NoArgsConstructor
  @Validated
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
    @NotNull
    private List<String> authorities = new ArrayList<>();

    /**
     * Build authorities.
     *
     * @return the authorities
     */
    String[] buildAuthorities() {
      if (authorities.isEmpty()) {
        return new String[]{
            AuthorityConstants.USER_ROLE_NAME
        };
      }
      return authorities.toArray(new String[0]);
    }
  }

}
