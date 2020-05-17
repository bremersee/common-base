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

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.ClassUtils;

/**
 * The access token cache auto configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.ANY)
@Configuration
@ConditionalOnClass({
    CacheManager.class
})
@Conditional(JwtSupportCondition.class)
@Slf4j
public class AccessTokenCacheAutoConfiguration {

  /**
   * Init.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName());
  }

  /**
   * Creates access token cache.
   *
   * @param cacheManagers the cache managers
   * @return the access token cache
   */
  @ConditionalOnMissingBean(AccessTokenCache.class)
  @Bean
  public AccessTokenCache accessTokenCache(
      ObjectProvider<List<CacheManager>> cacheManagers) {

    return Optional.ofNullable(cacheManagers.getIfAvailable())
        .flatMap(managers -> managers.stream()
            .filter(manager -> manager.getCacheNames().contains(AccessTokenCache.CACHE_NAME))
            .findFirst())
        .map(cacheManager -> {
          Cache cache = cacheManager.getCache(AccessTokenCache.CACHE_NAME);
          log.info("Creating access token cache with external cache {} from cache manager {}",
              cache, cacheManager);
          return cache;
        })
        .map(AccessTokenCacheImpl::new)
        .orElseGet(AccessTokenCacheImpl::new);
  }

}
