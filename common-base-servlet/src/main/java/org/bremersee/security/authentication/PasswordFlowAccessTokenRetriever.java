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

import java.io.IOException;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bremersee.exception.PasswordFlowAuthenticationException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * A traditional (blocking) implementation of the {@link AccessTokenRetriever}.
 *
 * @author Christian Bremer
 */
public class PasswordFlowAccessTokenRetriever
    implements AccessTokenRetriever<MultiValueMap<String, String>, String> {

  private final RestTemplate restTemplate;

  private final String tokenEndpoint;

  /**
   * Instantiates a new password flow access token retriever.
   *
   * @param restTemplateBuilder the rest template builder
   * @param tokenEndpoint       the token endpoint
   */
  public PasswordFlowAccessTokenRetriever(
      final RestTemplateBuilder restTemplateBuilder,
      final String tokenEndpoint) {
    this(restTemplateBuilder.build(), tokenEndpoint);
  }

  /**
   * Instantiates a new password flow access token retriever.
   *
   * @param restTemplate  the rest template
   * @param tokenEndpoint the token endpoint
   */
  public PasswordFlowAccessTokenRetriever(
      final RestTemplate restTemplate,
      final String tokenEndpoint) {
    this.restTemplate = restTemplate;
    this.restTemplate.setErrorHandler(new ErrorHandler());
    this.tokenEndpoint = tokenEndpoint;
  }

  @Override
  public String retrieveAccessToken(final MultiValueMap<String, String> body) {
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    final HttpEntity<?> request = new HttpEntity<>(body, headers);
    final String response = restTemplate.exchange(
        tokenEndpoint,
        HttpMethod.POST,
        request,
        String.class)
        .getBody();
    final JSONObject json = (JSONObject) JSONValue.parse(response);
    final String accessToken = json.getAsString("access_token");
    if (StringUtils.hasText(accessToken)) {
      return accessToken;
    }
    throw new PasswordFlowAuthenticationException(HttpStatus.UNAUTHORIZED,
        "There is no access token in the response: " + accessToken);
  }

  private static class ErrorHandler extends DefaultResponseErrorHandler {

    @Override
    protected void handleError(final ClientHttpResponse response, final HttpStatus statusCode)
        throws IOException {
      final String statusText = response.getStatusText();
      throw new PasswordFlowAuthenticationException(statusCode, statusText);
    }
  }

}
