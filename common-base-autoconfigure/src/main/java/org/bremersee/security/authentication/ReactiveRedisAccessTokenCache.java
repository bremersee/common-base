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
import lombok.Setter;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

/**
 * The reactive redis access token cache.
 *
 * @author Christian Bremer
 */
public class ReactiveRedisAccessTokenCache implements ReactiveAccessTokenCache {

  private final ReactiveRedisTemplate<String, String> redis;

  @Setter
  private Duration accessTokenThreshold = Duration.ofSeconds(20L);

  /**
   * Instantiates a new reactive redis access token cache.
   *
   * @param connectionFactory the connection factory
   */
  public ReactiveRedisAccessTokenCache(
      ReactiveRedisConnectionFactory connectionFactory) {
    Assert.notNull(connectionFactory, "Redis connection factory must be present.");
    this.redis = new ReactiveRedisTemplate<>(
        connectionFactory,
        RedisSerializationContext.string());
  }

  @Override
  public Mono<String> findAccessToken(String key) {
    return redis.opsForValue().get(key);
  }

  @Override
  public Mono<String> putAccessToken(String key, String accessToken) {
    Duration duration = Objects
        .requireNonNullElseGet(accessTokenThreshold, () -> Duration.ofSeconds(20L));
    long millis = System.currentTimeMillis() + duration.toMillis();
    return Mono.justOrEmpty(AccessTokenCache.getExpirationTime(accessToken))
        .filter(expirationTime -> expirationTime.getTime() > millis)
        .flatMap(expirationTime -> redis.opsForValue().set(key, accessToken)
            .flatMap(success -> success
                ? redis.expireAt(key, expirationTime.toInstant().minus(accessTokenThreshold))
                : Mono.just(false)))
        .map(result -> accessToken)
        .defaultIfEmpty(accessToken);
  }

}
