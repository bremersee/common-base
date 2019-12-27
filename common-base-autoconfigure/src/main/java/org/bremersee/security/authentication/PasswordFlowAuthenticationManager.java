package org.bremersee.security.authentication;

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

public class PasswordFlowAuthenticationManager
    implements AuthenticationManager, AuthenticationProvider {

  private static final OAuth2Error DEFAULT_INVALID_TOKEN =
      invalidToken("An error occurred while attempting to decode the Jwt: Invalid token");

  private AuthenticationProperties properties;

  private AccessTokenRetriever<String> accessTokenRetriever;

  private JwtDecoder jwtDecoder;

  private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter;

  public PasswordFlowAuthenticationManager(
      AuthenticationProperties properties,
      JwtDecoder jwtDecoder,
      @Nullable Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter,
      AccessTokenRetriever<String> accessTokenRetriever) {
    this.properties = properties;
    this.jwtDecoder = jwtDecoder;
    this.jwtAuthenticationConverter = jwtAuthenticationConverter != null
        ? jwtAuthenticationConverter
        : new JwtAuthenticationConverter();
    this.accessTokenRetriever = accessTokenRetriever;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    final String username = authentication.getName();
    final String password = (String) authentication.getCredentials();
    final String tokenStr = accessTokenRetriever
        .retrieveAccessToken(properties.getPasswordFlow().toProperties(username, password));
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
