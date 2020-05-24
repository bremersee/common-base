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
import java.util.Date;
import java.util.function.Function;
import javax.validation.constraints.NotNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.security.authentication.AuthProperties.JwtCache;
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
@Slf4j
public class ReactiveRedisAccessTokenCache implements ReactiveAccessTokenCache {

  private final JwtCache jwtCacheProperties;

  private final ReactiveRedisTemplate<String, String> redis;

  @Setter
  @NotNull
  private Function<String, Date> findExpirationTimeFn = AccessTokenCache::getExpirationTime;

  /**
   * Instantiates a new reactive redis access token cache.
   *
   * @param jwtCacheProperties the jwt cache properties
   * @param connectionFactory the connection factory
   */
  public ReactiveRedisAccessTokenCache(
      JwtCache jwtCacheProperties,
      ReactiveRedisConnectionFactory connectionFactory) {

    Assert.notNull(jwtCacheProperties, "Jwt cache properties must be present.");
    Assert.notNull(connectionFactory, "Redis connection factory must be present.");
    this.jwtCacheProperties = jwtCacheProperties;
    this.redis = new ReactiveRedisTemplate<>(
        connectionFactory,
        RedisSerializationContext.string());
  }

  @Override
  public Mono<String> findAccessToken(String key) {
    return redis.opsForValue().get(key)
        .onErrorResume(
            throwable -> throwable instanceof RuntimeException,
            throwable -> {
              log.error("Getting access token from redis cache failed.", throwable);
              return Mono.empty();
            });
  }

  @Override
  public Mono<String> putAccessToken(String key, String accessToken) {
    Duration duration = jwtCacheProperties.getExpirationTimeThreshold();
    long millis = System.currentTimeMillis() + duration.toMillis();
    String dbKey = jwtCacheProperties.addKeyPrefix(key);
    return Mono.justOrEmpty(findExpirationTimeFn.apply(accessToken))
        .filter(expirationTime -> expirationTime.getTime() > millis)
        .flatMap(expirationTime -> redis.opsForValue().set(dbKey, accessToken)
            .flatMap(success -> success
                ? redis.expireAt(dbKey, expirationTime.toInstant().minus(duration))
                : Mono.just(false)))
        .map(result -> accessToken)
        .onErrorResume(
            throwable -> throwable instanceof RuntimeException,
            throwable -> {
              log.error("Putting access token into redis cache failed.", throwable);
              return Mono.just(accessToken);
            })
        .defaultIfEmpty(accessToken);
  }

}
