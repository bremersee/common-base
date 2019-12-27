package org.bremersee.security.authentication;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

public interface ReactiveAccessTokenProviders {

  static AccessTokenProvider<Mono<String>> fromAuthentication() {
    return () -> ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .filter(authentication -> authentication instanceof JwtAuthenticationToken)
        .cast(JwtAuthenticationToken.class)
        .map(JwtAuthenticationToken::getToken)
        .map(Jwt::getTokenValue);
  }

  static AccessTokenProvider<Mono<String>> withAccessTokenRetriever(
      final AccessTokenRetrieverProperties properties) {
    return withAccessTokenRetriever(new WebClientAccessTokenRetriever(), properties);
  }

  static AccessTokenProvider<Mono<String>> withAccessTokenRetriever(
      final AccessTokenRetriever<Mono<String>> retriever,
      final AccessTokenRetrieverProperties properties) {
    return () -> retriever.retrieveAccessToken(properties);
  }

}
