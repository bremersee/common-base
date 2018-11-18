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

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Christian Bremer
 */
public abstract class OAuth2Helper {

  private OAuth2Helper() {
  }

  public static MultiValueMap<String, String> createPasswordFlowBody(
      final String clientId,
      final String clientSecret,
      final String username,
      final String password) {

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
