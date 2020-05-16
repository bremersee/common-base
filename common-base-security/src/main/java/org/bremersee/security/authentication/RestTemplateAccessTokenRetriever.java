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

import java.io.IOException;
import java.util.Optional;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bremersee.exception.AccessTokenRetrieverAuthenticationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * The rest template access token retriever.
 *
 * @author Christian Bremer
 */
public class RestTemplateAccessTokenRetriever implements AccessTokenRetriever<String> {

  private final RestTemplate restTemplate;

  private final AccessTokenCache accessTokenCache;

  /**
   * Instantiates a new rest template access token retriever.
   *
   * @param restTemplate the rest template
   */
  public RestTemplateAccessTokenRetriever(RestTemplate restTemplate) {
    this(restTemplate, null);
  }

  /**
   * Instantiates a new rest template access token retriever.
   *
   * @param restTemplate the rest template
   * @param accessTokenCache the access token cache
   */
  public RestTemplateAccessTokenRetriever(
      RestTemplate restTemplate,
      AccessTokenCache accessTokenCache) {

    this.restTemplate = restTemplate;
    this.restTemplate.setErrorHandler(new ErrorHandler());
    this.accessTokenCache = accessTokenCache;
  }

  @Override
  public String retrieveAccessToken(AccessTokenRetrieverProperties input) {
    final String cacheKey = input.createCacheKeyHashed();
    return Optional.ofNullable(accessTokenCache)
        .flatMap(cache -> cache.findAccessToken(cacheKey))
        .orElseGet(() -> {
          final HttpHeaders headers = new HttpHeaders();
          headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
          input.getBasicAuthProperties()
              .ifPresent(basicAuthProperties -> headers.setBasicAuth(
                  basicAuthProperties.getUsername(),
                  basicAuthProperties.getPassword()));
          final HttpEntity<?> request = new HttpEntity<>(input.createBody(), headers);
          final String response = restTemplate.exchange(
              input.getTokenEndpoint(),
              HttpMethod.POST,
              request,
              String.class)
              .getBody();
          final JSONObject json = (JSONObject) JSONValue.parse(response);
          final String accessToken = json.getAsString("access_token");
          if (StringUtils.hasText(accessToken)) {
            if (accessTokenCache != null) {
              accessTokenCache.put(cacheKey, accessToken);
            }
            return accessToken;
          }
          throw new AccessTokenRetrieverAuthenticationException(HttpStatus.UNAUTHORIZED,
              "There is no access token in the response: " + accessToken);
        });
  }

  private static class ErrorHandler extends DefaultResponseErrorHandler {

    @Override
    protected void handleError(final ClientHttpResponse response, final HttpStatus statusCode)
        throws IOException {
      final String statusText = response.getStatusText();
      throw new AccessTokenRetrieverAuthenticationException(statusCode, statusText);
    }
  }

}
