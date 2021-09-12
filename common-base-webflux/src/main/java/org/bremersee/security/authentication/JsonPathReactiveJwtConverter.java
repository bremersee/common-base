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
import lombok.ToString;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

/**
 * The json path reactive jwt converter.
 *
 * @author Christian Bremer
 */
@ToString(doNotUseGetters = true)
@EqualsAndHashCode(doNotUseGetters = true)
public class JsonPathReactiveJwtConverter implements Converter<Jwt, Mono<JwtAuthenticationToken>> {

  private final JsonPathJwtConverter converter;

  /**
   * Instantiates a new json path reactive jwt converter.
   */
  @SuppressWarnings("unused")
  public JsonPathReactiveJwtConverter() {
    this(null);
  }

  /**
   * Instantiates a new json path reactive jwt converter.
   *
   * @param converter the converter
   */
  public JsonPathReactiveJwtConverter(JsonPathJwtConverter converter) {
    this.converter = converter != null ? converter : new JsonPathJwtConverter();
  }

  @Override
  public Mono<JwtAuthenticationToken> convert(@NonNull Jwt jwt) {
    //noinspection NullableInLambdaInTransform
    return Mono.just(jwt).map(this.converter::convert);
  }

}
