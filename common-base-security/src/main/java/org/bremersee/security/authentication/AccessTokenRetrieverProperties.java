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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;
import org.bremersee.exception.ServiceException;
import org.springframework.util.MultiValueMap;

/**
 * The access token retriever properties.
 *
 * @author Christian Bremer
 */
public interface AccessTokenRetrieverProperties {

  /**
   * Gets token endpoint.
   *
   * @return the token endpoint
   */
  String getTokenEndpoint();

  /**
   * Gets basic auth properties.
   *
   * @return the basic auth properties or {@link Optional#empty()}, if no basic auth is required
   */
  default Optional<BasicAuthProperties> getBasicAuthProperties() {
    return Optional.empty();
  }

  /**
   * Create cache key.
   *
   * @return the cache key
   */
  Object createCacheKey();

  /**
   * Create an hashed cache key.
   *
   * @return the hashed cache key
   */
  default Object createCacheKeyHashed() {
    final Object cacheKey = createCacheKey();
    return Optional.ofNullable(cacheKey)
        .filter(key -> key instanceof CharSequence)
        .map(key -> {
          try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(String.valueOf(key).getBytes(StandardCharsets.UTF_8));
            return (Object) Base64.getEncoder().encodeToString(hashBytes);
          } catch (NoSuchAlgorithmException e) {
            throw ServiceException.internalServerError("Creating hash failed.", e);
          }
        })
        .orElse(cacheKey);
  }

  /**
   * Create http request body.
   *
   * @return the multi value map
   */
  MultiValueMap<String, String> createBody();

}
