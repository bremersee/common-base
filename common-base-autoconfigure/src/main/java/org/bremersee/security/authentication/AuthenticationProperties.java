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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
public class AuthenticationProperties {

  /**
   * Specifies whether Keycloak JWT beans should be created or not. Default is {@code false}.
   *
   * @see AuthenticationSupportAutoConfiguration
   * @see ReactiveAuthenticationSupportAutoConfiguration
   */
  private boolean enableKeycloakSupport = false;

  /**
   * Properties for actuator endpoints.
   */
  private ActuatorAccessProperties actuator = new ActuatorAccessProperties();

  /**
   * The properties for the oauth2 password flow.
   */
  private PasswordFlow passwordFlow = new PasswordFlow();

  /**
   * A list of in-memory users, that can login with basic authentication for testing purposes.
   */
  private List<SimpleUser> basicAuthUsers = new ArrayList<>();

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
   * The type Actuator access properties.
   */
  @Getter
  @Setter
  @ToString
  @EqualsAndHashCode
  @NoArgsConstructor
  public static class ActuatorAccessProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The roles which can access a protected actuator endpoint. The role names normally start with
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
     * @return the access expression (SpEL) for actuator endpoints
     */
    public String buildAccessExpression() {
      Set<String> roleSet = new HashSet<>(roles);
      if (roleSet.isEmpty()) {
        roleSet.add(AuthorityConstants.ACTUATOR_ROLE_NAME);
        roleSet.add(AuthorityConstants.ADMIN_ROLE_NAME);
      }
      final StringBuilder sb = new StringBuilder();
      roleSet.forEach(
          role -> sb.append(" or ").append("hasAuthority('").append(role).append("')"));
      ipAddresses.forEach(
          ipAddress -> sb.append(" or ").append("hasIpAddress('").append(ipAddress).append("')"));
      return sb.toString().substring(" or ".length());
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
    @SuppressWarnings("unused")
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
            AuthorityConstants.USER_ROLE_NAME,
            AuthorityConstants.LOCAL_USER_ROLE_NAME
        };
      }
      return authorities.toArray(new String[0]);
    }
  }

}
