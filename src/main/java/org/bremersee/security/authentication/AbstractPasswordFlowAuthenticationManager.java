/*
 * Copyright 2017 the original author or authors.
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
import lombok.extern.slf4j.Slf4j;
import org.bremersee.security.OAuth2Properties;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Christian Bremer
 */
@Slf4j
public class AbstractPasswordFlowAuthenticationManager {

  @Getter(AccessLevel.PROTECTED)
  private final OAuth2Properties oauth2Properties;

  public AbstractPasswordFlowAuthenticationManager(
      OAuth2Properties oauth2Properties) {
    this.oauth2Properties = oauth2Properties;
  }

  protected MultiValueMap<String, String> createPasswordFlowBody(Authentication authentication) {
    final String username = authentication.getName();
    final String presentedPassword = (String) authentication.getCredentials();
    final MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "password");
    body.add("client_id", oauth2Properties.getPasswordFlow().getClientId());
    body.add("client_secret", oauth2Properties.getPasswordFlow().getClientSecret());
    body.add("username", username);
    body.add("password", presentedPassword);
    return body;
  }


}
