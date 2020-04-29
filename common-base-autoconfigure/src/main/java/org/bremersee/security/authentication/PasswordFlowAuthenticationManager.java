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
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

/**
 * The password flow authentication manager.
 *
 * @author Christian Bremer
 */
public class PasswordFlowAuthenticationManager
    implements AuthenticationManager, AuthenticationProvider {

  private static final OAuth2Error DEFAULT_INVALID_TOKEN =
      invalidToken("An error occurred while attempting to decode the Jwt: Invalid token");

  private PasswordFlowPropertiesProvider passwordFlowPropertiesProvider;

  private AccessTokenRetriever<String> accessTokenRetriever;

  private JwtDecoder jwtDecoder;

  private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter;

  /**
   * Instantiates a new password flow authentication manager.
   *
   * @param passwordFlowPropertiesProvider the password flow properties provider
   * @param jwtDecoder the jwt decoder
   * @param jwtAuthenticationConverter the jwt authentication converter
   * @param accessTokenRetriever the access token retriever
   */
  public PasswordFlowAuthenticationManager(
      PasswordFlowPropertiesProvider passwordFlowPropertiesProvider,
      JwtDecoder jwtDecoder,
      @Nullable Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter,
      AccessTokenRetriever<String> accessTokenRetriever) {
    this.passwordFlowPropertiesProvider = passwordFlowPropertiesProvider;
    this.jwtDecoder = jwtDecoder;
    this.jwtAuthenticationConverter = Objects.requireNonNullElseGet(
        jwtAuthenticationConverter,
        JwtAuthenticationConverter::new);
    this.accessTokenRetriever = accessTokenRetriever;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    final String username = authentication.getName();
    final String password = (String) authentication.getCredentials();
    final String tokenStr = accessTokenRetriever
        .retrieveAccessToken(passwordFlowPropertiesProvider.toPasswordFlowProperties(username, password));
    Jwt jwt;
    try {
      jwt = this.jwtDecoder.decode(tokenStr);
    } catch (JwtException failed) {
      final OAuth2Error invalidToken = invalidToken(failed.getMessage());
      throw new OAuth2AuthenticationException(invalidToken, invalidToken.getDescription(), failed);
    }

    return this.jwtAuthenticationConverter.convert(jwt);
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class
        .isAssignableFrom(authentication);
  }

  private static OAuth2Error invalidToken(String message) {
    try {
      return new BearerTokenError(
          BearerTokenErrorCodes.INVALID_TOKEN,
          HttpStatus.UNAUTHORIZED,
          message,
          "https://tools.ietf.org/html/rfc6750#section-3.1");
    } catch (IllegalArgumentException malformed) {
      // some third-party library error messages are not suitable for RFC 6750's error message
      // charset
      return DEFAULT_INVALID_TOKEN;
    }
  }

}
