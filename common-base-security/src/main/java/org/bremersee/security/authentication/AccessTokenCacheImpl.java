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
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.BiFunction;
import javax.validation.constraints.NotNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;

/**
 * The access token cache implementation.
 *
 * @author Christian Bremer
 */
@Slf4j
public class AccessTokenCacheImpl implements AccessTokenCache, DisposableBean {

  private final Cache cache;

  private Timer internalCacheTimer;

  @Setter
  private Duration accessTokenThreshold = Duration.ofSeconds(20L);

  @Setter
  @NotNull
  private BiFunction<String, Duration, Boolean> expiredBiFn = AccessTokenCache::isExpired;

  /**
   * Instantiates a new access token cache.
   */
  public AccessTokenCacheImpl() {
    this(null);
  }

  /**
   * Instantiates a new access token cache.
   *
   * @param cache the external cache
   */
  public AccessTokenCacheImpl(Cache cache) {
    if (cache != null) {
      log.info("Creating access token cache with given cache.");
      this.cache = cache;
    } else {
      log.info("Creating access token cache with internal in memory cache.");
      this.cache = createInternalCache();
    }
  }

  private ConcurrentMapCache createInternalCache() {
    final long period = 1000L * 60L * 30L;
    ConcurrentMapCache internalCache = new ConcurrentMapCache(CACHE_NAME);
    internalCacheTimer = new Timer();
    internalCacheTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        Set<Object> keys = internalCache.getNativeCache().keySet();
        log.trace("Removing obsolete jwt entries from internal cache (sze = {}).", keys.size());
        keys.forEach(key -> findAccessToken(String.valueOf(key))
            .filter(token -> expiredBiFn.apply(token, accessTokenThreshold))
            .ifPresent(token -> internalCache.evict(key)));
      }
    }, period, period);
    return internalCache;
  }

  @Override
  public Optional<String> findAccessToken(String key) {
    try {
      return Optional.ofNullable(cache.get(key, String.class))
          .filter(token -> !expiredBiFn.apply(token, accessTokenThreshold));

    } catch (RuntimeException e) {
      log.error("Getting access token from cache failed.", e);
      return Optional.empty();
    }
  }

  @Override
  public void putAccessToken(String key, String accessToken) {
    try {
      cache.put(key, accessToken);

    } catch (RuntimeException e) {
      log.error("Putting access token into the cache failed.", e);
    }
  }

  @Override
  public void destroy() {
    if (internalCacheTimer != null) {
      internalCacheTimer.cancel();
    }
  }

  @Override
  public String toString() {
    return "AccessTokenCacheImpl {cache = "
        + (internalCacheTimer != null ? "INTERNAL" : "EXTERNAL")
        + '}';
  }
}
