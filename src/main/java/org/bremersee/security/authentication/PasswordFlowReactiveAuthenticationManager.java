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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.security.OAuth2Properties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
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
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * A reactive authentication manager that makes an OAuth2 Password Flow to authenticate the user.
 * This might be useful if you want to use Basic Auth with an OAuth2 identity provider.
 *
 * @author Christian Bremer
 */
@SuppressWarnings("unused")
@Slf4j
public class PasswordFlowReactiveAuthenticationManager
    extends AbstractPasswordFlowAuthenticationManager
    implements ReactiveAuthenticationManager {

  private final AccessTokenRetriever<MultiValueMap<String, String>, Mono<String>> accessTokenRetriever;

  private final ReactiveJwtDecoder jwtDecoder;

  @Setter
  private Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverter
      = new ReactiveJwtAuthenticationConverterAdapter(new JwtAuthenticationConverter());

  /**
   * Instantiates a new password flow authentication manager.
   *
   * @param oauth2Properties the oauth 2 properties
   * @param jwtDecoder       the jwt decoder
   */
  public PasswordFlowReactiveAuthenticationManager(
      OAuth2Properties oauth2Properties,
      ReactiveJwtDecoder jwtDecoder) {

    super(oauth2Properties);
    this.jwtDecoder = jwtDecoder;
    this.accessTokenRetriever = new PasswordFlowAccessTokenReactiveRetriever(
        WebClient
            .builder()
            .baseUrl(oauth2Properties.getPasswordFlow().getTokenEndpoint())
            .build());
  }

  /**
   * Instantiates a new password flow reactive authentication manager.
   *
   * @param oauth2Properties     the oauth 2 properties
   * @param jwtDecoder           the jwt decoder
   * @param accessTokenRetriever the access token retriever
   */
  public PasswordFlowReactiveAuthenticationManager(
      OAuth2Properties oauth2Properties,
      ReactiveJwtDecoder jwtDecoder,
      AccessTokenRetriever<MultiValueMap<String, String>, Mono<String>> accessTokenRetriever) {

    super(oauth2Properties);
    this.jwtDecoder = jwtDecoder;
    this.accessTokenRetriever = accessTokenRetriever;
  }

  @Override
  public Mono<Authentication> authenticate(final Authentication authentication) {
    if (log.isDebugEnabled()) {
      log.debug("msg=[Authenticating basic authentication with OAuth2 password flow.]");
    }
    return Mono.just(createPasswordFlowBody(authentication))
        .flatMap(accessTokenRetriever::retrieveAccessToken)
        .flatMap(jwtDecoder::decode)
        .flatMap(jwtAuthenticationConverter::convert)
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
