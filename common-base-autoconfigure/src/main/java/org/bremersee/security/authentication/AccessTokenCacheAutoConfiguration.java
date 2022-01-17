/*
 * Copyright 2020-2022 the original author or authors.
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

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * The access token cache autoconfiguration.
 *
 * @author Christian Bremer
 */
@ConditionalOnClass(name = {
    "org.bremersee.security.authentication.AccessTokenCache"
})
@ConditionalOnWebApplication(type = Type.SERVLET)
@Conditional(JwtSupportCondition.class)
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
@Slf4j
public class AccessTokenCacheAutoConfiguration {

  /**
   * The default configuration.
   */
  @ConditionalOnClass(name = "org.springframework.cache.CacheManager")
  @ConditionalOnMissingBean(value = {
      AccessTokenCache.class
  }, type = {
      "org.springframework.data.redis.connection.jedis.JedisConnectionFactory",
      "org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory"
  })
  @Configuration
  @EnableConfigurationProperties(AuthProperties.class)
  static class Default {

    private final AuthProperties authProperties;

    /**
     * Instantiates a new default configuration.
     *
     * @param authProperties the auth properties
     */
    public Default(AuthProperties authProperties) {
      this.authProperties = authProperties;
    }

    /**
     * Init.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
      log.info("\n"
              + "*******************************************************************************\n"
              + "* {}.{}\n"
              + "*******************************************************************************",
          ClassUtils.getUserClass(AccessTokenCacheAutoConfiguration.class).getSimpleName(),
          ClassUtils.getUserClass(getClass()).getSimpleName());
    }

    /**
     * Creates an access token cache.
     *
     * @param cacheManagers the cache managers
     * @return the access token cache
     */
    @Bean
    public AccessTokenCache accessTokenCache(
        ObjectProvider<List<CacheManager>> cacheManagers) {

      return findJwtCache(cacheManagers.getIfAvailable())
          .map(externalCache -> AccessTokenCache.builder().withExternalCache(externalCache))
          .orElseGet(AccessTokenCache::builder)
          .withExpirationTimeThreshold(authProperties.getJwtCache().getExpirationTimeThreshold())
          .withKeyPrefix(authProperties.getJwtCache().getKeyPrefix())
          .build();
    }

    private Optional<Cache> findJwtCache(List<CacheManager> cacheManagers) {
      final String cacheName = authProperties.getJwtCache().getExternalCacheName();
      return Optional.ofNullable(cacheManagers)
          .flatMap(managers -> managers.stream()
              .filter(manager -> manager.getCacheNames().contains(cacheName))
              .findFirst())
          .map(cacheManager -> {
            Cache externalCache = cacheManager.getCache(cacheName);
            log.info("Creating access token cache with external cache {} from cache manager {}",
                externalCache, cacheManager);
            return externalCache;
          });
    }
  }

  /**
   * The configuration with redis.
   */
  @ConditionalOnClass(
      name = "org.springframework.data.redis.connection.RedisConnectionFactory")
  @Conditional(RedisCondition.class)
  @ConditionalOnMissingBean(AccessTokenCache.class)
  @Configuration
  @EnableConfigurationProperties(AuthProperties.class)
  static class WithRedis {

    private final AuthProperties authProperties;

    /**
     * Instantiates a new configuration with redis.
     *
     * @param authProperties the auth properties
     */
    public WithRedis(AuthProperties authProperties) {
      this.authProperties = authProperties;
    }

    /**
     * Init.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
      log.info("\n"
              + "*******************************************************************************\n"
              + "* {}.{}\n"
              + "*******************************************************************************",
          ClassUtils.getUserClass(AccessTokenCacheAutoConfiguration.class).getSimpleName(),
          ClassUtils.getUserClass(getClass()).getSimpleName());
    }

    /**
     * Creates an access token cache that uses Redis.
     *
     * @param connectionFactoryProvider the connection factory provider
     * @return the access token cache
     */
    @Bean
    public AccessTokenCache redisAccessTokenCache(
        ObjectProvider<RedisConnectionFactory> connectionFactoryProvider) {

      RedisConnectionFactory connectionFactory = connectionFactoryProvider.getIfAvailable();
      Assert.notNull(connectionFactory, "Redis connection factory must not be null.");
      log.info("Creating {} with {} ...", RedisAccessTokenCache.class.getSimpleName(),
          ClassUtils.getUserClass(connectionFactory).getSimpleName());
      return new RedisAccessTokenCache(authProperties.getJwtCache(), connectionFactory);
    }
  }

}
