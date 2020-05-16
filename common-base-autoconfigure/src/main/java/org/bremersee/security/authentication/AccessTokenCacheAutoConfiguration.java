package org.bremersee.security.authentication;

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
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.ClassUtils;

@ConditionalOnWebApplication(type = Type.ANY)
@Configuration
@ConditionalOnClass({
    CacheManager.class
})
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
   * @param cacheManagerProvider the cache manager provider
   * @return the access token cache
   */
  @ConditionalOnMissingBean
  @Bean
  public AccessTokenCache accessTokenCache(ObjectProvider<CacheManager> cacheManagerProvider) {
    return Optional.ofNullable(cacheManagerProvider.getIfAvailable())
        .map(cacheManager -> {
          if (cacheManager.getCacheNames().contains(AccessTokenCache.CACHE_NAME)) {
            Cache cache = cacheManager.getCache(AccessTokenCache.CACHE_NAME);
            log.info("Creating access token cache with external cache {} from cache manager {}",
                cache, cacheManager);
            return cache;
          } else {
            log.info("Creating access token cache: a cache manager was found ({}), but it doesn't "
                    + "contain a cache with name {}. Using internal cache.",
                cacheManager, AccessTokenCache.CACHE_NAME);
            return null;
          }
        })
        .map(AccessTokenCacheImpl::new)
        .orElseGet(AccessTokenCacheImpl::new);
  }

}
