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
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bremersee.exception.AccessTokenRetrieverAuthenticationException;
import org.bremersee.web.ErrorDetectors;
import org.bremersee.web.reactive.function.client.AbstractWebClientErrorDecoder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * A reactive implementation of the {@link AccessTokenRetriever}.
 *
 * @author Christian Bremer
 */
@Slf4j
public class WebClientAccessTokenRetriever
    extends AbstractWebClientErrorDecoder<AuthenticationException>
    implements AccessTokenRetriever<Mono<String>> {

  private final WebClient webClient;

  private final ReactiveAccessTokenCache accessTokenCache;

  /**
   * Instantiates a new access token retriever that uses spring's web client.
   */
  public WebClientAccessTokenRetriever() {
    this(null, null);
  }

  /**
   * Instantiates a new access token retriever that uses spring's web client.
   *
   * @param accessTokenCache the access token cache
   */
  @SuppressWarnings("unused")
  public WebClientAccessTokenRetriever(ReactiveAccessTokenCache accessTokenCache) {
    this(null, accessTokenCache);
  }

  /**
   * Instantiates a new access token retriever that uses spring's web client.
   *
   * @param webClient the web client
   */
  public WebClientAccessTokenRetriever(
      WebClient webClient) {
    this(webClient, null);
  }

  /**
   * Instantiates a new access token retriever that uses spring's web client.
   *
   * @param webClient the web client
   * @param accessTokenCache the access token cache
   */
  public WebClientAccessTokenRetriever(
      WebClient webClient,
      ReactiveAccessTokenCache accessTokenCache) {
    this.webClient = webClient != null ? webClient : WebClient.builder().build();
    this.accessTokenCache = accessTokenCache;
  }

  @Override
  public Mono<String> retrieveAccessToken(final AccessTokenRetrieverProperties properties) {
    if (log.isDebugEnabled()) {
      log.debug("Retrieving access token with password flow, properties = {}", properties);
    }
    final String cacheKey = properties.createCacheKeyHashed();
    return Mono.justOrEmpty(accessTokenCache)
        .flatMap(cache -> cache.findAccessToken(cacheKey))
        .switchIfEmpty(webClient
            .method(HttpMethod.POST)
            .uri(properties.getTokenEndpoint())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .headers(headers -> properties.getBasicAuthProperties()
                .ifPresent(basicAuthProperties -> headers.setBasicAuth(
                    basicAuthProperties.getUsername(),
                    basicAuthProperties.getPassword())))
            .body(BodyInserters.fromFormData(properties.createBody()))
            .retrieve()
            .onStatus(ErrorDetectors.DEFAULT, this)
            .bodyToMono(String.class)
            .map(response -> ((JSONObject) JSONValue.parse(response)).getAsString("access_token"))
            .flatMap(accessToken -> accessTokenCache != null
                ? accessTokenCache.putAccessToken(cacheKey, accessToken)
                : Mono.just(accessToken)));
  }

  @Override
  public AuthenticationException buildException(
      final ClientResponse clientResponse, final String response) {
    final AccessTokenRetrieverAuthenticationException exception
        = new AccessTokenRetrieverAuthenticationException(clientResponse.statusCode(), response);
    log.error("msg=[Retrieving access token with password flow failed.]", exception);
    return exception;
  }

}
