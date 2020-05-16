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
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.ClassUtils;

/**
 * The type Access token cache auto configuration.
 */
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
   * @param jwtDecoder the jwt decoder
   * @param cacheManagers the cache managers
   * @return the access token cache
   */
  @ConditionalOnMissingBean
  @Bean
  public AccessTokenCache accessTokenCache(
      ObjectProvider<JwtDecoder> jwtDecoder,
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
        .map(cache -> new AccessTokenCacheImpl(jwtDecoder.getIfAvailable(), cache))
        .orElseGet(() -> new AccessTokenCacheImpl(jwtDecoder.getIfAvailable()));
  }

}
