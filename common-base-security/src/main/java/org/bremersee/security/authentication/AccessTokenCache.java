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

import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.bremersee.exception.ServiceException;
import org.springframework.cache.Cache;
import org.springframework.validation.annotation.Validated;

/**
 * The access token cache interface.
 *
 * @author Christian Bremer
 */
@Validated
public interface AccessTokenCache {

  /**
   * The constant CACHE_NAME.
   */
  String CACHE_NAME = "jwt";

  /**
   * Find not expired access token from cache.
   *
   * @param key the key
   * @return the access token
   */
  Optional<String> findAccessToken(@NotNull String key);

  /**
   * Put new access token into the cache.
   *
   * @param key the key
   * @param accessToken the access token
   */
  void putAccessToken(@NotNull String key, @NotNull String accessToken);

  /**
   * Checks whether the access token is expired. If no expiration claim is present, the result will always be {@code
   * true}.
   *
   * @param tokenValue the token value
   * @param accessTokenThreshold the access token threshold
   * @return the boolean
   */
  static boolean isExpired(@NotNull String tokenValue, Duration accessTokenThreshold) {
    Duration duration = Objects
        .requireNonNullElseGet(accessTokenThreshold, () -> Duration.ofSeconds(20L));
    long millis = System.currentTimeMillis() + duration.toMillis();
    return Optional.ofNullable(getExpirationTime(tokenValue))
        .map(date -> date.getTime() < millis)
        .orElse(true);
  }

  /**
   * Gets expiration time.
   *
   * @param tokenValue the token value
   * @return the expiration time or {@code null} if there is no expiration claim
   */
  static Date getExpirationTime(@NotNull String tokenValue) {
    JWT jwt = parse(tokenValue);
    try {
      if (jwt.getJWTClaimsSet() != null
          && jwt.getJWTClaimsSet().getExpirationTime() != null) {
        return jwt.getJWTClaimsSet().getExpirationTime();
      }

    } catch (ParseException e) {
      // ignored
    }
    return null;
  }

  /**
   * Parse jwt.
   *
   * @param tokenValue the token value
   * @return the jwt
   */
  static JWT parse(@NotNull String tokenValue) {
    try {
      return SignedJWT.parse(tokenValue);
    } catch (Exception e0) {
      try {
        return EncryptedJWT.parse(tokenValue);
      } catch (Exception e1) {
        try {
          return PlainJWT.parse(tokenValue);
        } catch (Exception e2) {
          throw ServiceException.internalServerError("Parsing jwt failed.");
        }
      }
    }
  }

  /**
   * Creates a new builder.
   *
   * @return the builder
   */
  static Builder builder() {
    return new Builder.Impl();
  }

  /**
   * The builder interface.
   */
  interface Builder {

    /**
     * With external cache.
     *
     * @param externalCache the external cache
     * @return the builder
     */
    Builder withExternalCache(Cache externalCache);

    /**
     * With expiration time threshold.
     *
     * @param duration the duration
     * @return the builder
     */
    Builder withExpirationTimeThreshold(Duration duration);

    /**
     * With key prefix.
     *
     * @param keyPrefix the key prefix
     * @return the builder
     */
    Builder withKeyPrefix(String keyPrefix);

    /**
     * Build access token cache.
     *
     * @return the access token cache
     */
    AccessTokenCache build();

    /**
     * The builder implementation.
     */
    class Impl implements Builder {

      private Cache externalCache;

      private Duration expirationTimeThreshold;

      private String keyPrefix;

      @Override
      public Builder withExternalCache(Cache externalCache) {
        this.externalCache = externalCache;
        return this;
      }

      @Override
      public Builder withExpirationTimeThreshold(Duration duration) {
        this.expirationTimeThreshold = duration;
        return this;
      }

      @Override
      public Builder withKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
        return this;
      }

      @Override
      public AccessTokenCache build() {
        return new AccessTokenCacheImpl(externalCache, expirationTimeThreshold, keyPrefix);
      }
    }
  }
}
