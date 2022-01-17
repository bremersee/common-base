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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * The reactive access token cache autoconfiguration.
 *
 * @author Christian Bremer
 */
@ConditionalOnClass(name = {
    "org.bremersee.security.authentication.AccessTokenCache",
    "org.bremersee.security.authentication.ReactiveAccessTokenCache"
})
@ConditionalOnWebApplication(type = Type.REACTIVE)
@Configuration
@Conditional(JwtSupportCondition.class)
@AutoConfigureAfter(RedisReactiveAutoConfiguration.class)
@Slf4j
public class ReactiveAccessTokenCacheAutoConfiguration {

  /**
   * The default configuration.
   */
  @ConditionalOnClass(name = "org.springframework.cache.CacheManager")
  @ConditionalOnMissingBean(value = {
      ReactiveAccessTokenCache.class
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
          ClassUtils.getUserClass(ReactiveAccessTokenCacheAutoConfiguration.class).getSimpleName(),
          ClassUtils.getUserClass(getClass()).getSimpleName());
    }

    /**
     * Creates an access token cache that will be wrapped into a reactive one.
     *
     * @param cacheManagers the cache managers
     * @return the access token cache
     */
    @ConditionalOnMissingBean
    @Lazy
    @Bean
    public AccessTokenCache accessTokenCache(ObjectProvider<List<CacheManager>> cacheManagers) {
      log.info("Creating {} for reactive application.", AccessTokenCache.class.getSimpleName());
      return new AccessTokenCacheAutoConfiguration.Default(authProperties)
          .accessTokenCache(cacheManagers);
    }

    /**
     * Creates the reactive access token cache.
     *
     * @param accessTokenCache the access token cache
     * @return the reactive access token cache
     */
    @Bean
    public ReactiveAccessTokenCache reactiveAccessTokenCache(
        ObjectProvider<AccessTokenCache> accessTokenCache) {

      log.info("Creating {} ...", ReactiveAccessTokenCache.class.getSimpleName());
      return ReactiveAccessTokenCache.from(accessTokenCache.getIfAvailable());
    }

  }

  /**
   * The configuration with redis.
   */
  @ConditionalOnClass(
      name = "org.springframework.data.redis.connection.ReactiveRedisConnectionFactory")
  @Conditional(RedisCondition.class)
  @ConditionalOnMissingBean(ReactiveAccessTokenCache.class)
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
          ClassUtils.getUserClass(ReactiveAccessTokenCacheAutoConfiguration.class).getSimpleName(),
          ClassUtils.getUserClass(getClass()).getSimpleName());
    }

    /**
     * Creates a reactive access token cache that uses Redis.
     *
     * @param connectionFactoryProvider the connection factory provider
     * @return the reactive access token cache
     */
    @Bean
    public ReactiveAccessTokenCache reactiveRedisAccessTokenCache(
        ObjectProvider<ReactiveRedisConnectionFactory> connectionFactoryProvider) {

      ReactiveRedisConnectionFactory connectionFactory = connectionFactoryProvider.getIfAvailable();
      Assert.notNull(connectionFactory, "Redis connection factory must not be null.");
      log.info("Creating {} with {} ...", RedisAccessTokenCache.class.getSimpleName(),
          ClassUtils.getUserClass(connectionFactory).getSimpleName());
      return new ReactiveRedisAccessTokenCache(authProperties.getJwtCache(), connectionFactory);
    }

  }

}
