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
import org.bremersee.security.OAuth2Helper;
import org.bremersee.security.OAuth2Properties;
import org.bremersee.security.authentication.AccessTokenRetriever;
import org.bremersee.security.authentication.PasswordFlowAccessTokenReactiveRetriever;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * This exchange filter makes an oauth2 password flow and appends the retrieved access token to the
 * current http request.
 *
 * @author Christian Bremer
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class PasswordFlowTokenAppender implements ExchangeFilterFunction {

  private OAuth2Properties properties;

  private AccessTokenRetriever<MultiValueMap<String, String>, Mono<String>> accessTokenRetriever;

  private String accessToken;

  private Date expirationTime;

  /**
   * Instantiates a new password flow token appender.
   *
   * @param properties the properties
   */
  public PasswordFlowTokenAppender(final OAuth2Properties properties) {
    Assert.notNull(properties, "OAuth2 properties must not be null.");
    this.properties = properties;
    this.accessTokenRetriever = new PasswordFlowAccessTokenReactiveRetriever(
        WebClient
            .builder()
            .baseUrl(properties.getPasswordFlow().getTokenEndpoint())
            .build());
  }

  /**
   * Instantiates a new password flow token appender.
   *
   * @param properties           the properties
   * @param accessTokenRetriever the access token retriever
   */
  public PasswordFlowTokenAppender(
      OAuth2Properties properties,
      AccessTokenRetriever<MultiValueMap<String, String>, Mono<String>> accessTokenRetriever) {
    Assert.notNull(properties, "OAuth2 properties must not be null.");
    Assert.notNull(accessTokenRetriever, "Access token retriever must not be null.");
    this.properties = properties;
    this.accessTokenRetriever = accessTokenRetriever;
  }

  @Override
  public Mono<ClientResponse> filter(final ClientRequest request, final ExchangeFunction next) {

    final long remainingMillis = properties.getPasswordFlow().getExpirationTimeRemainsMillis();
    if (accessToken == null
        || expirationTime == null
        || expirationTime.before(new Date(System.currentTimeMillis() - remainingMillis))) {

      return accessTokenRetriever
          .retrieveAccessToken(OAuth2Helper
              .createPasswordFlowBody(
                  properties.getPasswordFlow().getClientId(),
                  properties.getPasswordFlow().getClientSecret(),
                  properties.getPasswordFlow().getSystemUsername(),
                  properties.getPasswordFlow().getSystemPassword()))
          .map(this::parse)
          .flatMap(accessToken -> next.exchange(ClientRequest
              .from(request)
              .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
              .build()));

    } else {

      return Mono.just(accessToken)
          .flatMap(accessToken -> next.exchange(ClientRequest
              .from(request)
              .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
              .build()));
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
