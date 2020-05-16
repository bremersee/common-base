/*
 * Copyright 2019-2020 the original author or authors.
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

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
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
public class PasswordFlowReactiveAuthenticationManager
    implements ReactiveAuthenticationManager, DisposableBean {

  private final ClientCredentialsFlowProperties passwordFlowProperties;

  private final ReactiveJwtDecoder jwtDecoder;

  private Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtConverter;

  private final AccessTokenRetriever<Mono<String>> retriever;

  private final JwtCache jwtCache;

  /**
   * Instantiates a new password flow reactive authentication manager.
   *
   * @param passwordFlowProperties the password flow properties
   * @param jwtDecoder the jwt decoder
   * @param jwtConverter the jwt converter
   * @param retriever the retriever
   */
  public PasswordFlowReactiveAuthenticationManager(
      ClientCredentialsFlowProperties passwordFlowProperties,
      ReactiveJwtDecoder jwtDecoder,
      @Nullable Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtConverter,
      @Nullable AccessTokenRetriever<Mono<String>> retriever) {
    this.passwordFlowProperties = passwordFlowProperties;
    this.jwtDecoder = jwtDecoder;
    this.jwtConverter = Objects.requireNonNullElseGet(
        jwtConverter,
        () -> new ReactiveJwtAuthenticationConverterAdapter(new JwtAuthenticationConverter()));
    this.retriever = Objects.requireNonNullElseGet(
        retriever,
        WebClientAccessTokenRetriever::new);
    // TODO customize
    this.jwtCache = new JwtCache();
  }

  /**
   * Sets jwt authentication converter.
   *
   * @param jwtConverter the jwt converter
   */
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

    final PasswordFlowProperties properties = PasswordFlowProperties.builder()
        .from(passwordFlowProperties)
        .username(authentication.getName())
        .password((String) authentication.getCredentials())
        .build();
    final Object cacheKey = properties.createCacheKeyHashed();
    return Mono.just(properties)
        .flatMap(props -> jwtCache.findJwt(cacheKey)
            .map(Mono::just)
            .orElseGet(() -> retriever.retrieveAccessToken(properties)
                .flatMap(token -> jwtDecoder.decode(token)
                    .map(newJwt -> {
                      jwtCache.put(cacheKey, newJwt);
                      return newJwt;
                    }))))
        .flatMap(jwt -> Objects.requireNonNull(jwtConverter.convert(jwt)))
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

  @Override
  public void destroy() {
    jwtCache.destroy();
  }
}
