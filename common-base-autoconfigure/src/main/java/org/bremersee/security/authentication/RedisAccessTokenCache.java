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

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import lombok.Setter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

/**
 * The redis access token cache.
 *
 * @author Christian Bremer
 */
public class RedisAccessTokenCache implements AccessTokenCache {

  private final StringRedisTemplate redis;

  @Setter
  private Duration accessTokenThreshold = Duration.ofSeconds(20L);

  /**
   * Instantiates a new redis access token cache.
   *
   * @param connectionFactory the connection factory
   */
  public RedisAccessTokenCache(
      RedisConnectionFactory connectionFactory) {
    Assert.notNull(connectionFactory, "Redis connection factory must be present.");
    this.redis = new StringRedisTemplate();
    this.redis.setConnectionFactory(connectionFactory);
    this.redis.afterPropertiesSet();
  }

  @Override
  public Optional<String> findAccessToken(String key) {
    return Optional.ofNullable(redis.opsForValue().get(key));
  }

  @Override
  public void putAccessToken(String key, String accessToken) {
    Duration duration = Objects
        .requireNonNullElseGet(accessTokenThreshold, () -> Duration.ofSeconds(20L));
    long millis = System.currentTimeMillis() + duration.toMillis();
    Optional.ofNullable(AccessTokenCache.getExpirationTime(accessToken))
        .filter(expirationTime -> expirationTime.getTime() > millis)
        .ifPresent(expirationTime -> {
          redis.opsForValue().set(key, accessToken);
          redis.expireAt(key, expirationTime);
        });
  }

}
