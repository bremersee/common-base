/*
 * Copyright 2018 the original author or authors.
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

import lombok.AccessLevel;
import lombok.Getter;
import org.bremersee.security.OAuth2Helper;
import org.bremersee.security.OAuth2Properties;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

/**
 * Abstract base implementation of a password flow authentication manager.
 *
 * @author Christian Bremer
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractPasswordFlowAuthenticationManager {

  @Getter(AccessLevel.PROTECTED)
  private final OAuth2Properties oauth2Properties;

  /**
   * Instantiates a new password flow authentication manager.
   *
   * @param oauth2Properties the oauth2 properties
   */
  public AbstractPasswordFlowAuthenticationManager(
      OAuth2Properties oauth2Properties) {

    Assert.notNull(oauth2Properties, "OAuth2 properties must be present.");
    Assert.notNull(oauth2Properties.getPasswordFlow(),
        "OAuth2 password flow properties must be present.");
    Assert.hasText(oauth2Properties.getPasswordFlow().getClientId(),
        "Client ID must be present.");
    this.oauth2Properties = oauth2Properties;
  }

  /**
   * Create body of the oauth2 password flow.
   *
   * @param authentication the authentication
   * @return the body of the oauth2 password flow
   */
  protected MultiValueMap<String, String> createPasswordFlowBody(
      final Authentication authentication) {

    return OAuth2Helper.createPasswordFlowBody(
        oauth2Properties.getPasswordFlow().getClientId(),
        oauth2Properties.getPasswordFlow().getClientSecret(),
        authentication.getName(),
        (String) authentication.getCredentials());
  }

}
