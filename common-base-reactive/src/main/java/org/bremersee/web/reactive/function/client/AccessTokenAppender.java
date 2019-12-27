package org.bremersee.web.reactive.function.client;

import org.bremersee.security.authentication.AccessTokenProvider;
import org.bremersee.security.authentication.AccessTokenRetriever;
import org.bremersee.security.authentication.AccessTokenRetrieverProperties;
import org.bremersee.security.authentication.ReactiveAccessTokenProviders;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

public class AccessTokenAppender implements ExchangeFilterFunction {

  private final AccessTokenProvider<Mono<String>> accessTokenProvider;

  public AccessTokenAppender(AccessTokenProvider<Mono<String>> accessTokenProvider) {
    Assert.notNull(accessTokenProvider, "Access token provider must be present.");
    this.accessTokenProvider = accessTokenProvider;
  }

  @Override
  public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
    return accessTokenProvider.getAccessToken()
        .switchIfEmpty(Mono.just(""))
        .flatMap(tokenValue -> exchangeWithToken(request, tokenValue, next));
  }

  private Mono<ClientResponse> exchangeWithToken(
      ClientRequest request,
      String tokenValue,
      ExchangeFunction next) {

    if (StringUtils.hasText(tokenValue)) {
      return next.exchange(ClientRequest
          .from(request)
          .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValue))
          .build());
    } else {
      return next.exchange(request);
    }
  }

  public static AccessTokenAppender fromAuthentication() {
    return new AccessTokenAppender(ReactiveAccessTokenProviders.fromAuthentication());
  }

  public static AccessTokenAppender withAccessTokenRetriever(
      final AccessTokenRetrieverProperties properties) {
    return new AccessTokenAppender(
        ReactiveAccessTokenProviders.withAccessTokenRetriever(properties));
  }

  public static AccessTokenAppender withAccessTokenRetriever(
      final AccessTokenRetriever<Mono<String>> retriever,
      final AccessTokenRetrieverProperties properties) {
    return new AccessTokenAppender(
        ReactiveAccessTokenProviders.withAccessTokenRetriever(retriever, properties));
  }

}
