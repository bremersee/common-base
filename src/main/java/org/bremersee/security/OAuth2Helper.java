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

package org.bremersee.security;

import javax.validation.constraints.NotNull;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;

/**
 * The oauth2 helper.
 *
 * @author Christian Bremer
 */
@Validated
public abstract class OAuth2Helper {

  private OAuth2Helper() {
  }

  /**
   * Creates a password flow body.
   *
   * @param clientId     the client id
   * @param clientSecret the client secret
   * @param username     the username
   * @param password     the password
   * @return the multi value map of a password flow body
   */
  public static MultiValueMap<String, String> createPasswordFlowBody(
      @NotNull final String clientId,
      @Nullable final String clientSecret,
      @NotNull final String username,
      @NotNull final String password) {

    final MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "password");
    body.add("client_id", clientId);
    if (clientSecret != null) {
      body.add("client_secret", clientSecret);
    } else {
      body.add("client_secret", "");
    }
    body.add("username", username);
    body.add("password", password);
    return body;
  }

}
