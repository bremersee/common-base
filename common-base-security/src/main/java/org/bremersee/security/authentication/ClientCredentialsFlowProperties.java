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

import java.util.Arrays;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * The client credentials flow properties.
 *
 * @author Christian Bremer
 */
public interface ClientCredentialsFlowProperties extends AccessTokenRetrieverProperties {

  /**
   * Gets client id.
   *
   * @return the client id
   */
  String getClientId();

  /**
   * Gets client secret.
   *
   * @return the client secret
   */
  String getClientSecret();

  /**
   * Gets additional properties.
   *
   * @return the additional properties
   */
  default MultiValueMap<String, String> getAdditionalProperties() {
    return new LinkedMultiValueMap<>();
  }

  @Override
  default Optional<BasicAuthProperties> getBasicAuthProperties() {
    return Optional.of(BasicAuthProperties.builder()
        .username(getClientId())
        .password(getClientSecret())
        .build());
  }

  @Override
  default MultiValueMap<String, String> createBody() {
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    if (getAdditionalProperties() != null) {
      body.addAll(getAdditionalProperties());
    }
    body.set("grant_type", "client_credentials");
    return body;
  }

  /**
   * Returns a builder for client credentials flow properties.
   *
   * @return the builder
   */
  static Builder builder() {
    return new Builder();
  }

  /**
   * The builder implementation.
   */
  @ToString(exclude = {"clientSecret"})
  @EqualsAndHashCode(exclude = {"clientSecret"})
  class Builder {

    private String tokenEndpoint;

    private String clientId;

    private String clientSecret;

    private final MultiValueMap<String, String> additionalProperties = new LinkedMultiValueMap<>();

    /**
     * Sets token endpoint on builder.
     *
     * @param tokenEndpoint the token endpoint
     * @return the builder
     */
    public Builder tokenEndpoint(String tokenEndpoint) {
      this.tokenEndpoint = tokenEndpoint;
      return this;
    }

    /**
     * Sets client id on builder.
     *
     * @param clientId the client id
     * @return the builder
     */
    public Builder clientId(String clientId) {
      this.clientId = clientId;
      return this;
    }

    /**
     * Sets client secret on builder.
     *
     * @param clientSecret the client secret
     * @return the builder
     */
    public Builder clientSecret(String clientSecret) {
      this.clientSecret = clientSecret;
      return this;
    }

    /**
     * Adds an additional property on builder.
     *
     * @param key the key
     * @param values the values
     * @return the builder
     */
    public Builder add(String key, String... values) {
      if (StringUtils.hasText(key) && values != null) {
        additionalProperties.addAll(key, Arrays.asList(values));
      }
      return this;
    }

    /**
     * Gets the values from the given client credentials flow properties.
     *
     * @param properties the client credentials flow properties
     * @return the builder
     */
    public Builder from(ClientCredentialsFlowProperties properties) {
      if (properties != null) {
        if (properties.getAdditionalProperties() != null) {
          this.additionalProperties.addAll(properties.getAdditionalProperties());
        }
        return tokenEndpoint(properties.getTokenEndpoint())
            .clientId(properties.getClientId())
            .clientSecret(properties.getClientSecret());
      }
      return this;
    }

    /**
     * Build client credentials flow properties.
     *
     * @return the client credentials flow properties
     */
    public ClientCredentialsFlowProperties build() {
      return new Impl(
          tokenEndpoint,
          clientId,
          clientSecret,
          additionalProperties);
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @ToString(exclude = {"clientSecret"})
    @EqualsAndHashCode(exclude = {"clientSecret"})
    private static class Impl implements ClientCredentialsFlowProperties {

      private final String tokenEndpoint;

      private final String clientId;

      private final String clientSecret;

      private final MultiValueMap<String, String> additionalProperties;
    }
  }

}
