package org.bremersee.security.authentication;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.Assert;

/**
 * The access token cache implementation.
 */
@Slf4j
public class AccessTokenCacheImpl implements AccessTokenCache, DisposableBean {

  private ConcurrentMapCache internalCache;

  private Timer internalCacheTimer;

  private boolean internalCacheTimerCanceled = false;

  private final JwtDecoder jwtDecoder;

  private final Cache cache;

  @Setter
  @Positive
  private long expirationToleranceAmount = 30L;

  @Setter
  @NotNull
  private ChronoUnit expirationToleranceUnit = ChronoUnit.SECONDS;

  /**
   * Instantiates a new access token cache.
   *
   * @param jwtDecoder the jwt decoder
   */
  public AccessTokenCacheImpl(JwtDecoder jwtDecoder) {
    this(jwtDecoder, null);
  }

  /**
   * Instantiates a new access token cache.
   *
   * @param jwtDecoder the jwt decoder
   * @param cache the external cache
   */
  public AccessTokenCacheImpl(JwtDecoder jwtDecoder, Cache cache) {
    Assert.notNull(jwtDecoder, "Jwt decoder must not be present.");
    this.jwtDecoder = jwtDecoder;
    if (cache != null) {
      log.info("Creating a jwt cache with given cache.");
      this.cache = cache;
    } else {
      log.info("Creating a jwt cache with internal in memory cache.");
      this.cache = getInternalCache();
    }
  }

  private ConcurrentMapCache getInternalCache() {
    synchronized (CACHE_NAME) {
      if (internalCache == null) {
        final long period = 1000L * 60L * 30L;
        internalCache = new ConcurrentMapCache(CACHE_NAME);
        internalCacheTimer = new Timer();
        internalCacheTimer.schedule(new TimerTask() {
          @Override
          public void run() {
            Set<Object> keys = internalCache.getNativeCache().keySet();
            log.trace("Removing obsolete jwt entries from internal cache (sze = {}).", keys.size());
            keys.forEach(key -> findJwt(String.valueOf(key))
                .ifPresent(jwt -> {
                  if (jwt.getExpiresAt() == null || jwt.getExpiresAt().isBefore(Instant.now())) {
                    internalCache.evict(key);
                  }
                }));
          }
        }, period, period);
      }
      return internalCache;
    }
  }

  @Override
  public Optional<String> findAccessToken(String key) {
    return findJwt(key).map(Jwt::getTokenValue);
  }

  @Override
  public Optional<Jwt> findJwt(String key) {
    return Optional.ofNullable(cache.get(key, String.class))
        .map(jwtDecoder::decode)
        .filter(jwt -> jwt.getExpiresAt() != null && jwt.getExpiresAt()
            .isAfter(Instant.now().plus(expirationToleranceAmount, expirationToleranceUnit)));
  }

  @Override
  public void putAccessToken(String key, String accessToken) {
    putJwt(key, jwtDecoder.decode(accessToken));
  }

  @Override
  public void putJwt(String key, Jwt jwt) {
    if (jwt.getExpiresAt() != null) {
      cache.put(key, jwt.getTokenValue());
    }
  }

  @Override
  public void destroy() {
    synchronized (CACHE_NAME) {
      if (!internalCacheTimerCanceled && internalCacheTimer != null) {
        internalCacheTimerCanceled = true;
        internalCacheTimer.cancel();
      }
    }
  }

}
