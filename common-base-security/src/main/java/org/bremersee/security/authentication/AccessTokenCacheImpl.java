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
import org.springframework.util.StringUtils;

/**
 * The access token cache implementation.
 *
 * @author Christian Bremer
 */
@Slf4j
public class AccessTokenCacheImpl implements AccessTokenCache, DisposableBean {

  private Timer internalCacheTimer;

  private final Cache cache;

  private final Duration expirationTimeThreshold;

  private final String keyPrefix;

  @Setter
  @NotNull
  private BiFunction<String, Duration, Boolean> expiredBiFn = AccessTokenCache::isExpired;

  /**
   * Instantiates a new access token cache.
   */
  @SuppressWarnings("unused")
  public AccessTokenCacheImpl() {
    this(null, null);
  }

  /**
   * Instantiates a new access token cache.
   *
   * @param expirationTimeThreshold the expiration time threshold
   * @param keyPrefix the key prefix
   */
  public AccessTokenCacheImpl(Duration expirationTimeThreshold, String keyPrefix) {
    this(null, expirationTimeThreshold, keyPrefix);
  }

  /**
   * Instantiates a new access token cache.
   *
   * @param cache the external cache
   */
  @SuppressWarnings("unused")
  public AccessTokenCacheImpl(Cache cache) {
    this(cache, null, null);
  }

  /**
   * Instantiates a new access token cache.
   *
   * @param cache the cache
   * @param expirationTimeThreshold the expiration time threshold
   * @param keyPrefix the key prefix
   */
  public AccessTokenCacheImpl(
      Cache cache,
      Duration expirationTimeThreshold,
      String keyPrefix) {
    if (cache != null) {
      log.info("Creating access token cache with given cache.");
      this.cache = cache;
    } else {
      log.info("Creating access token cache with internal in memory cache.");
      this.cache = createInternalCache();
    }
    this.expirationTimeThreshold = Objects
        .requireNonNullElseGet(expirationTimeThreshold, () -> Duration.ofSeconds(20L));
    this.keyPrefix = keyPrefix;
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
            .filter(token -> expiredBiFn.apply(token, expirationTimeThreshold))
            .ifPresent(token -> internalCache.evict(key)));
      }
    }, period, period);
    return internalCache;
  }

  private String addKeyPrefix(String givenKey) {
    if (StringUtils.hasText(keyPrefix) && !givenKey.startsWith(keyPrefix)) {
      return keyPrefix + givenKey;
    }
    return givenKey;
  }

  @Override
  public Optional<String> findAccessToken(String key) {
    try {
      return Optional.ofNullable(cache.get(addKeyPrefix(key), String.class))
          .filter(token -> !expiredBiFn.apply(token, expirationTimeThreshold));

    } catch (RuntimeException e) {
      log.error("Getting access token from cache failed.", e);
      return Optional.empty();
    }
  }

  @Override
  public void putAccessToken(String key, String accessToken) {
    try {
      cache.put(addKeyPrefix(key), accessToken);

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
        + ", keyPrefix = " + keyPrefix
        + ", expirationTimeThreshold (in secs) = " + expirationTimeThreshold.toSeconds()
        + '}';
  }
}
