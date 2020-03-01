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

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

/**
 * The reactive access token providers.
 *
 * @author Christian Bremer
 */
public interface ReactiveAccessTokenProviders {

  /**
   * Provider that gets the access token from the authentication.
   *
   * @return the access token provider
   */
  static AccessTokenProvider<Mono<String>> fromAuthentication() {
    return () -> ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .filter(authentication -> authentication instanceof JwtAuthenticationToken)
        .cast(JwtAuthenticationToken.class)
        .map(JwtAuthenticationToken::getToken)
        .map(Jwt::getTokenValue);
  }

  /**
   * Provider that retrieves the access token from an OpenId server.
   *
   * @param properties the properties
   * @return the access token provider
   */
  static AccessTokenProvider<Mono<String>> withAccessTokenRetriever(
      final AccessTokenRetrieverProperties properties) {
    return withAccessTokenRetriever(new WebClientAccessTokenRetriever(), properties);
  }

  /**
   * Provider that retrieves the access token from an OpenId server.
   *
   * @param retriever  the retriever
   * @param properties the properties
   * @return the access token provider
   */
  static AccessTokenProvider<Mono<String>> withAccessTokenRetriever(
      final AccessTokenRetriever<Mono<String>> retriever,
      final AccessTokenRetrieverProperties properties) {
    return () -> retriever.retrieveAccessToken(properties);
  }

}
