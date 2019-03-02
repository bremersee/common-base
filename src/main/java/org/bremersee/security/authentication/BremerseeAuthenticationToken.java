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

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * The bremersee authentication token interface.
 *
 * @author Christian Bremer
 */
@SuppressWarnings("unused")
public interface BremerseeAuthenticationToken extends Authentication {

  /**
   * Gets preferred name.
   * <p>
   * Some identity providers have two names: The ID of the user, that is normally returned by {@link
   * #getName()}, and the user name of the user, that can be returned here.
   *
   * @return the preferred name
   */
  default String getPreferredName() {
    return getName();
  }

  /**
   * Gets token.
   *
   * @return the token
   */
  Jwt getToken();

  /**
   * Gets token attributes.
   *
   * @return the token attributes
   */
  Map<String, Object> getTokenAttributes();

}
