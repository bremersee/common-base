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

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import reactor.core.publisher.Mono;

/**
 * The password flow reactive authentication manager.
 *
 * @author Christian Bremer
 */
@Slf4j
public class PasswordFlowReactiveAuthenticationManager implements ReactiveAuthenticationManager {

  private AuthenticationProperties properties;

  private ReactiveJwtDecoder jwtDecoder;

  private Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtConverter;

  private AccessTokenRetriever<Mono<String>> retriever;

  /**
   * Instantiates a new password flow reactive authentication manager.
   *
   * @param properties the properties
   * @param jwtDecoder the jwt decoder
   * @param jwtConverter the jwt converter
   * @param retriever the retriever
   */
  public PasswordFlowReactiveAuthenticationManager(
      AuthenticationProperties properties,
      ReactiveJwtDecoder jwtDecoder,
      @Nullable Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtConverter,
      @Nullable AccessTokenRetriever<Mono<String>> retriever) {
    this.properties = properties;
    this.jwtDecoder = jwtDecoder;
    this.jwtConverter = jwtConverter != null
        ? jwtConverter
        : new ReactiveJwtAuthenticationConverterAdapter(new JwtAuthenticationConverter());
    this.retriever = retriever != null ? retriever : new WebClientAccessTokenRetriever();
  }

  /**
   * Sets jwt authentication converter.
   *
   * @param jwtConverter the jwt converter
   */
  @SuppressWarnings("unused")
  public void setJwtAuthenticationConverter(
      Converter<Jwt, ? extends AbstractAuthenticationToken> jwtConverter) {
    if (jwtConverter != null) {
      //noinspection unchecked
      this.jwtConverter = new ReactiveJwtAuthenticationConverterAdapter(
          (Converter<Jwt, AbstractAuthenticationToken>) jwtConverter);
    }
  }

  @Override
  public Mono<Authentication> authenticate(final Authentication authentication) {

    final String username = authentication.getName();
    final String password = (String) authentication.getCredentials();
    //noinspection NullableInLambdaInTransform
    return Mono.just(properties.getPasswordFlow().toProperties(username, password))
        .flatMap(retriever::retrieveAccessToken)
        .flatMap(jwtDecoder::decode)
        .flatMap(jwtConverter::convert)
        .cast(Authentication.class)
        .onErrorMap(JwtException.class, this::onError);
  }

  private OAuth2AuthenticationException onError(JwtException e) {
    log.error("msg=[Basic authentication with password flow failed.]", e);
    OAuth2Error invalidRequest = invalidToken(e.getMessage());
    return new OAuth2AuthenticationException(invalidRequest, e.getMessage());
  }

  private static OAuth2Error invalidToken(String message) {
    return new BearerTokenError(
        BearerTokenErrorCodes.INVALID_TOKEN,
        HttpStatus.UNAUTHORIZED,
        message,
        "https://tools.ietf.org/html/rfc6750#section-3.1");
  }

}
