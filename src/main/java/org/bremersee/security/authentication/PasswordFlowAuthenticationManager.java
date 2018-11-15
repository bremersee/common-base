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
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
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
import org.springframework.util.MultiValueMap;

/**
 * An authentication manager that make an OAuth2 Password Flow to authenticate the user. This might
 * be useful if you want to use Basic Auth with an OAuth2 identity provider.
 *
 * @author Christian Bremer
 */
@Slf4j
@SuppressWarnings("unused")
public class PasswordFlowAuthenticationManager
    extends AbstractPasswordFlowAuthenticationManager
    implements AuthenticationManager, AuthenticationProvider {

  private static final OAuth2Error DEFAULT_INVALID_TOKEN =
      invalidToken("An error occurred while attempting to decode the Jwt: Invalid token");

  @Setter
  private AccessTokenRetriever<MultiValueMap<String, String>, String> accessTokenRetriever;

  @Setter
  private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter
      = new JwtAuthenticationConverter();

  private final JwtDecoder jwtDecoder;

  /**
   * Instantiates a new password flow authentication manager.
   *
   * @param oauth2Properties    the oauth 2 properties
   * @param jwtDecoder          the jwt decoder
   * @param restTemplateBuilder the rest template builder
   */
  public PasswordFlowAuthenticationManager(
      final OAuth2Properties oauth2Properties,
      final JwtDecoder jwtDecoder,
      final RestTemplateBuilder restTemplateBuilder) {

    super(oauth2Properties);
    this.jwtDecoder = jwtDecoder;
    this.accessTokenRetriever = new PasswordFlowAccessTokenRetriever(
        restTemplateBuilder,
        oauth2Properties.getPasswordFlow().getTokenEndpoint());
  }

  @Override
  public Authentication authenticate(final Authentication authentication)
      throws AuthenticationException {

    final String tokenStr = accessTokenRetriever
        .retrieveAccessToken(createPasswordFlowBody(authentication));
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
    return (UsernamePasswordAuthenticationToken.class
        .isAssignableFrom(authentication));
  }

  private static OAuth2Error invalidToken(String message) {
    try {
      return new BearerTokenError(
          BearerTokenErrorCodes.INVALID_TOKEN,
          HttpStatus.UNAUTHORIZED,
          message,
          "https://tools.ietf.org/html/rfc6750#section-3.1");
    } catch (IllegalArgumentException malformed) {
      // some third-party library error messages are not suitable for RFC 6750's error message charset
      return DEFAULT_INVALID_TOKEN;
    }
  }

}
