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

package org.bremersee.web.reactive.function.client;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * This exchange filter appends the JWT access token from the current authentication {@link
 * JwtAuthenticationToken} to the request headers. The header name will be {@code Authentication},
 * the header value will be {@code Bearer [ACCESS_TOKEN]}.
 *
 * @author Christian Bremer
 */
@SuppressWarnings("unused")
public class JwtAuthenticationTokenAppender implements ExchangeFilterFunction {

  @Override
  public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .filter(authentication -> authentication instanceof JwtAuthenticationToken)
        .cast(JwtAuthenticationToken.class)
        .map(JwtAuthenticationToken::getToken)
        .map(Jwt::getTokenValue)
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

}
