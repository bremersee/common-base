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

package org.bremersee.test.security.authentication;

import org.bremersee.security.authentication.KeycloakJwtConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * The keycloak jwt converter factory.
 *
 * @author Christian Bremer
 */
public class KeycloakJwtConverterFactory implements JwtConverterFactory {

  @Override
  public Converter<Jwt, ? extends AbstractAuthenticationToken> createJwtConverter() {
    return new KeycloakJwtConverter();
  }
}
