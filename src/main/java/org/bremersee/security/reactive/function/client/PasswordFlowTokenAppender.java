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

package org.bremersee.security.reactive.function.client;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import java.util.Date;
import java.util.function.Function;
import org.bremersee.security.OAuth2Helper;
import org.bremersee.security.OAuth2Properties;
import org.bremersee.security.authentication.AccessTokenRetriever;
import org.bremersee.security.authentication.PasswordFlowAccessTokenReactiveRetriever;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * @author Christian Bremer
 */
public class PasswordFlowTokenAppender implements ExchangeFilterFunction {

  private OAuth2Properties properties;

  private AccessTokenRetriever<MultiValueMap<String, String>, Mono<String>> accessTokenRetriever;

  private String accessToken;

  private Date expirationTime;

  public PasswordFlowTokenAppender(OAuth2Properties properties) {
    this.properties = properties;
    this.accessTokenRetriever = new PasswordFlowAccessTokenReactiveRetriever(
        WebClient
            .builder()
            .baseUrl(properties.getPasswordFlow().getTokenEndpoint())
            .build());
  }

  public PasswordFlowTokenAppender(
      OAuth2Properties properties,
      AccessTokenRetriever<MultiValueMap<String, String>, Mono<String>> accessTokenRetriever) {
    this.properties = properties;
    this.accessTokenRetriever = accessTokenRetriever;
  }

  @Override
  public Mono<ClientResponse> filter(final ClientRequest request, final ExchangeFunction next) {

    final long remainingMillis = properties.getPasswordFlow().getExpirationTimeRemainsMillis();
    if (accessToken == null
        || expirationTime == null
        || expirationTime.after(new Date(System.currentTimeMillis() - remainingMillis))) {

      return accessTokenRetriever
          .retrieveAccessToken(OAuth2Helper
              .createPasswordFlowBody(
                  properties.getPasswordFlow().getClientId(),
                  properties.getPasswordFlow().getClientSecret(),
                  properties.getPasswordFlow().getSystemUsername(),
                  properties.getPasswordFlow().getSystemPassword()))
          .map(this::parse)
          .flatMap((Function<Mono<String>, Mono<ClientResponse>>) accessToken -> {
            request.headers().set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            return next.exchange(request);
          });

    } else {

      return Mono.just(accessToken)
          .flatMap((Function<String, Mono<ClientResponse>>) tokenValue -> {
            request.headers().set(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValue);
            return next.exchange(request);
          });
    }
  }

  private Mono<String> parse(String token) {
    try {
      final JWT jwt = JWTParser.parse(token);
      this.accessToken = token;
      this.expirationTime = jwt.getJWTClaimsSet().getExpirationTime();
      return Mono.just(token);

    } catch (Exception ex) {
      throw new JwtException(
          "An error occurred while attempting to decode the Jwt: " + ex.getMessage(), ex);
    }
  }

}
