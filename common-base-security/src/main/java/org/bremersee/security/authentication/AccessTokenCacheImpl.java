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

import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
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
  @Positive
  private long expirationToleranceAmount = 30L;

  @Setter
  @NotNull
  private ChronoUnit expirationToleranceUnit = ChronoUnit.SECONDS;

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
            .filter(token -> isExpired(token))
            .ifPresent(token -> internalCache.evict(key)));
      }
    }, period, period);
    return internalCache;
  }

  @Override
  public Optional<String> findAccessToken(String key) {
    return Optional.ofNullable(cache.get(key, String.class))
        .filter(this::isNotExpired);
  }

  @Override
  public void putAccessToken(String key, String accessToken) {
    cache.put(key, accessToken);
  }

  @Override
  public void destroy() {
    if (internalCacheTimer != null) {
      internalCacheTimer.cancel();
    }
  }

  private boolean isExpired(@NotNull String tokenValue) {
    return !isNotExpired(tokenValue);
  }

  private boolean isNotExpired(@NotNull String tokenValue) {
    JWT jwt = parse(tokenValue);
    try {
      long millis = System.currentTimeMillis() + Duration
          .of(expirationToleranceAmount, expirationToleranceUnit).toMillis();
      return jwt.getJWTClaimsSet() != null
          && jwt.getJWTClaimsSet().getExpirationTime() != null
          && jwt.getJWTClaimsSet().getExpirationTime().after(new Date(millis));

    } catch (ParseException e) {
      log.warn("Parsing claim set failed. Returning false.");
      return false;
    }
  }

  private JWT parse(@NotNull String tokenValue) {
    try {
      return SignedJWT.parse(tokenValue);
    } catch (Exception e0) {
      try {
        log.warn("Parsing signed jwt failed. Trying to parse encrypted jwt ...", e0);
        return EncryptedJWT.parse(tokenValue);
      } catch (Exception e1) {
        try {
          log.warn("Parsing encrypted jwt failed. Trying to parse plain jwt ...", e1);
          return PlainJWT.parse(tokenValue);
        } catch (Exception e2) {
          log.error("Parsing plan jwt failed. Throwing internal server error.", e2);
          throw ServiceException.internalServerError("Parsing jwt failed.");
        }
      }
    }
  }
}
