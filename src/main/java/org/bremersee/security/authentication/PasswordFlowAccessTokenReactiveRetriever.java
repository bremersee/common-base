/*
 * Copyright 2017 the original author or authors.
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
 * @author Christian Bremer
 */
public class PasswordFlowAccessTokenReactiveRetriever
    implements AccessTokenRetriever<MultiValueMap<String, String>, Mono<String>> {

  private final AuthenticationExceptionCreator exceptionCreator
      = new AuthenticationExceptionCreator();

  private final WebClient webClient;

  @SuppressWarnings("WeakerAccess")
  public PasswordFlowAccessTokenReactiveRetriever(
      final WebClient webClient) {
    this.webClient = webClient;
  }

  @Override
  public Mono<String> retrieveAccessToken(final MultiValueMap<String, String> body) {
    return webClient
        .method(HttpMethod.POST)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData(body))
        .retrieve()
        .onStatus(ErrorDetectors.DEFAULT, exceptionCreator)
        .bodyToMono(String.class)
        .map(s -> ((JSONObject) JSONValue.parse(s)).getAsString("access_token"));
  }

  private static class AuthenticationExceptionCreator
      extends AbstractWebClientErrorDecoder<AuthenticationException> {

    @Override
    public AuthenticationException buildException(
        final ClientResponse clientResponse, final String response) {
      return new PasswordFlowAuthenticationException(clientResponse.statusCode(), response);
    }
  }

}
