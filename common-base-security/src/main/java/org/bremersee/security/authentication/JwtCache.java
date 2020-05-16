package org.bremersee.security.authentication;

import java.sql.Date;
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
import org.springframework.validation.annotation.Validated;

/**
 * The jwt cache.
 */
@Validated
@Slf4j
public class JwtCache implements DisposableBean {

  /**
   * The constant CACHE_NAME.
   */
  public static final String CACHE_NAME = "jwt";

  private static ConcurrentMapCache internalCache;

  private static Timer internalCacheTimer;

  private static boolean internalCacheTimerCanceled = false;

  private final Cache cache;

  @Setter
  @Positive
  private long expirationToleranceAmount = 30L;

  @Setter
  @NotNull
  private ChronoUnit expirationToleranceUnit = ChronoUnit.SECONDS;

  /**
   * Instantiates a new jwt cache.
   */
  public JwtCache() {
    this(null);
  }

  /**
   * Instantiates a new jwt cache.
   *
   * @param cache the cache
   */
  public JwtCache(Cache cache) {
    if (cache != null) {
      log.info("Creating a jwt cache with given cache.");
      this.cache = cache;
    } else {
      log.info("Creating a jwt cache with internal in memory cache.");
      this.cache = getInternalCache();
    }
  }

  private static ConcurrentMapCache getInternalCache() {
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
            keys.forEach(key -> {
              Jwt jwt = (Jwt) internalCache.getNativeCache().get(key);
              if (jwt != null && jwt.getExpiresAt() != null
                  && jwt.getExpiresAt().isBefore(Instant.now())) {
                log.trace("Removing a jwt that expires at {}", Date.from(jwt.getExpiresAt()));
                internalCache.evict(key);
              }
            });
          }
        }, period, period);
      }
      return internalCache;
    }
  }

  /**
   * Find not expired jwt from cache.
   *
   * @param key the key
   * @return the jwt
   */
  public Optional<Jwt> findJwt(@NotNull Object key) {
    return Optional.ofNullable(cache.get(key, Jwt.class))
        .filter(jwt -> jwt.getExpiresAt() != null && jwt.getExpiresAt()
            .isAfter(Instant.now().plus(expirationToleranceAmount, expirationToleranceUnit)));
  }

  /**
   * Put new jwt into the cache.
   *
   * @param key the key
   * @param jwt the jwt
   */
  public void put(@NotNull Object key, @NotNull Jwt jwt) {
    if (jwt.getExpiresAt() != null && jwt.getExpiresAt()
        .isAfter(Instant.now().plus(expirationToleranceAmount, expirationToleranceUnit))) {
      cache.put(key, jwt);
    }
  }

  @Override
  public void destroy() {
    synchronized (CACHE_NAME) {
      if (!internalCacheTimerCanceled) {
        internalCacheTimerCanceled = true;
        internalCacheTimer.cancel();
      }
    }
  }

}
