package org.bremersee.security.authentication;

import java.util.Objects;
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

@Slf4j
public class PasswordFlowReactiveAuthenticationManager implements ReactiveAuthenticationManager {

  private AuthenticationProperties properties;

  private ReactiveJwtDecoder jwtDecoder;

  private Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtConverter;

  private AccessTokenRetriever<Mono<String>> retriever;

  public PasswordFlowReactiveAuthenticationManager(
      AuthenticationProperties properties,
      ReactiveJwtDecoder jwtDecoder,
      @Nullable Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtConverter,
      @Nullable AccessTokenRetriever<Mono<String>> retriever) {
    this.properties = properties;
    this.jwtDecoder = jwtDecoder;
    this.jwtConverter = Objects.requireNonNullElseGet(
        jwtConverter,
        () -> new ReactiveJwtAuthenticationConverterAdapter(new JwtAuthenticationConverter()));
    this.retriever = Objects.requireNonNullElseGet(
        retriever,
        WebClientAccessTokenRetriever::new);
  }

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
