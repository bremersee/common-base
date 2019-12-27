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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

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

  // TODO do we want a switch?
  //private boolean usePasswordFlowAuthenticationManager = true;

  /**
   * The properties for the oauth2 password flow.
   */
  private PasswordFlow passwordFlow = new PasswordFlow();

  /**
   * OAuth2 password flow configuration properties.
   */
  @Getter
  @Setter
  @ToString(exclude = {"clientSecret", "systemPassword"})
  @EqualsAndHashCode(exclude = {"clientSecret", "systemPassword"})
  public static class PasswordFlow {

    private String tokenEndpoint;

    private String clientId;

    private String clientSecret;

    private String systemUsername;

    private String systemPassword;

    public PasswordFlowProperties toProperties() {
      return this.toProperties(systemUsername, systemPassword);
    }

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

}
