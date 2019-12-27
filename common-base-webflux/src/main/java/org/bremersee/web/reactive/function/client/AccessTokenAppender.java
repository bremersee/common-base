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

/**
 * The access token appender.
 *
 * @author Christian Bremer
 */
public class AccessTokenAppender implements ExchangeFilterFunction {

  private final AccessTokenProvider<Mono<String>> accessTokenProvider;

  /**
   * Instantiates a new access token appender.
   *
   * @param accessTokenProvider the access token provider
   */
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

  /**
   * From authentication access token appender.
   *
   * @return the access token appender
   */
  public static AccessTokenAppender fromAuthentication() {
    return new AccessTokenAppender(ReactiveAccessTokenProviders.fromAuthentication());
  }

  /**
   * With access token retriever access token appender.
   *
   * @param properties the properties
   * @return the access token appender
   */
  @SuppressWarnings("unused")
  public static AccessTokenAppender withAccessTokenRetriever(
      final AccessTokenRetrieverProperties properties) {
    return new AccessTokenAppender(
        ReactiveAccessTokenProviders.withAccessTokenRetriever(properties));
  }

  /**
   * With access token retriever access token appender.
   *
   * @param retriever  the retriever
   * @param properties the properties
   * @return the access token appender
   */
  public static AccessTokenAppender withAccessTokenRetriever(
      final AccessTokenRetriever<Mono<String>> retriever,
      final AccessTokenRetrieverProperties properties) {
    return new AccessTokenAppender(
        ReactiveAccessTokenProviders.withAccessTokenRetriever(retriever, properties));
  }

}
