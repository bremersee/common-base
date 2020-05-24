/*
 * Copyright 2020 the original author or authors.
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

import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

/**
 * The reactive access token cache interface.
 *
 * @author Christian Bremer
 */
@Validated
public interface ReactiveAccessTokenCache {

  /**
   * The constant CACHE_NAME.
   */
  @SuppressWarnings("unused")
  String CACHE_NAME = AccessTokenCache.CACHE_NAME;

  /**
   * Find not expired access token from cache.
   *
   * @param key the key
   * @return the access token
   */
  Mono<String> findAccessToken(@NotNull String key);

  /**
   * Put new access token into the cache.
   *
   * @param key the key
   * @param accessToken the access token
   * @return the access token
   */
  Mono<String> putAccessToken(@NotNull String key, @NotNull String accessToken);

  /**
   * Creates a reactive cache from the given access token cache.
   *
   * @param accessTokenCache the access token cache
   * @return the reactive access token cache
   */
  static ReactiveAccessTokenCache from(@NotNull AccessTokenCache accessTokenCache) {
    return new ReactiveAccessTokenCache() {
      @Override
      public Mono<String> findAccessToken(@NotNull String key) {
        return accessTokenCache.findAccessToken(key).map(Mono::just).orElse(Mono.empty());
      }

      @Override
      public Mono<String> putAccessToken(@NotNull String key, @NotNull String accessToken) {
        accessTokenCache.putAccessToken(key, accessToken);
        return Mono.just(accessToken);
      }

      @Override
      public String toString() {
        return "Reactive wrapper of " + accessTokenCache;
      }
    };
  }

}
