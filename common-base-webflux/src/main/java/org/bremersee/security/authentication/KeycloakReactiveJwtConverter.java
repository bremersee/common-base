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

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

/**
 * A reactive JWT converter for the keycloak identity provider. It use the json path {@code
 * $.realm_access.roles} to extract the roles from the token.
 *
 * @author Christian Bremer
 */
public class KeycloakReactiveJwtConverter implements Converter<Jwt, Mono<JwtAuthenticationToken>> {

  private final KeycloakJwtConverter converter;

  /**
   * Instantiates a new keycloak reactive jwt converter.
   */
  public KeycloakReactiveJwtConverter() {
    this.converter = new KeycloakJwtConverter();
  }

  @Override
  public Mono<JwtAuthenticationToken> convert(final Jwt jwt) {
    //noinspection NullableInLambdaInTransform
    return Mono.just(jwt).map(this.converter::convert);
  }

}
