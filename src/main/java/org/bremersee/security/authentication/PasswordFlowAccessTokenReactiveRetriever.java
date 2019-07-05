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

package org.bremersee.security.authentication;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bremersee.exception.PasswordFlowAuthenticationException;
import org.bremersee.web.ErrorDetectors;
import org.bremersee.web.reactive.function.client.AbstractWebClientErrorDecoder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.MultiValueMap;
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
public class PasswordFlowAccessTokenReactiveRetriever
    extends AbstractWebClientErrorDecoder<AuthenticationException>
    implements AccessTokenRetriever<MultiValueMap<String, String>, Mono<String>> {

  private final WebClient webClient;

  /**
   * Instantiates a new password flow access token retriever.
   *
   * @param webClient the web client
   */
  public PasswordFlowAccessTokenReactiveRetriever(
      final WebClient webClient) {
    this.webClient = webClient;
  }

  /**
   * Instantiates a new password flow access token reactive retriever.
   *
   * @param tokenUrl the token url
   */
  @SuppressWarnings("unused")
  public PasswordFlowAccessTokenReactiveRetriever(
      final String tokenUrl) {
    this.webClient = WebClient.builder()
        .baseUrl(tokenUrl)
        .build();
  }

  @Override
  public Mono<String> retrieveAccessToken(final MultiValueMap<String, String> body) {
    if (log.isDebugEnabled()) {
      log.debug("msg=[Retrieving access token with password flow.]");
    }
    return webClient
        .method(HttpMethod.POST)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData(body))
        .retrieve()
        .onStatus(ErrorDetectors.DEFAULT, this)
        .bodyToMono(String.class)
        .map(s -> ((JSONObject) JSONValue.parse(s)).getAsString("access_token"));
  }

  @Override
  public AuthenticationException buildException(
      final ClientResponse clientResponse, final String response) {
    final PasswordFlowAuthenticationException exception = new PasswordFlowAuthenticationException(
        clientResponse.statusCode(), response);
    log.error("msg=[Retrieving access token with password flow failed.]", exception);
    return exception;
  }

}
