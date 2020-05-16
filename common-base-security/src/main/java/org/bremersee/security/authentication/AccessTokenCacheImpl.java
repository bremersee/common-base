package org.bremersee.security.authentication;

import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import java.sql.Date;
import java.text.ParseException;
import java.time.Duration;
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
import org.bremersee.exception.ServiceException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * The access token cache implementation.
 */
@Slf4j
public class AccessTokenCacheImpl implements AccessTokenCache, DisposableBean {

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
              String cacheValue = (String) internalCache.getNativeCache().get(key);
              Instant expires = instantFromCacheValue(cacheValue);
              if (expires.isBefore(Instant.now())) {
                log.trace("Removing a jwt that expires at {}", Date.from(expires));
                internalCache.evict(key);
              }
            });
          }
        }, period, period);
      }
      return internalCache;
    }
  }

  @Override
  public Optional<String> findAccessToken(String key) {
    return Optional.ofNullable(cache.get(key, String.class))
        .filter(value -> instantFromCacheValue(value)
            .isAfter(Instant.now().plus(expirationToleranceAmount, expirationToleranceUnit)))
        .map(AccessTokenCacheImpl::accessTokenFromCacheValue);
  }

  @Override
  public void put(String key, String accessToken) {
    toCacheValue(accessToken).ifPresent(cacheValue -> cache.put(key, cacheValue));
  }

  @Override
  public void put(String key, Jwt jwt) {
    toCacheValue(jwt).ifPresent(cacheValue -> cache.put(key, cacheValue));
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

  private static Instant instantFromCacheValue(@NotNull String cacheValue) {
    int index = cacheValue.indexOf(':');
    if (index > -1) {
      long millis = Long.parseLong(cacheValue.substring(0, index));
      return new Date(millis).toInstant();
    }
    return Instant.now().minus(1L, ChronoUnit.SECONDS);
  }

  private static String accessTokenFromCacheValue(@NotNull String cacheValue) {
    int index = cacheValue.indexOf(':');
    return index > -1 ? cacheValue.substring(index + 1) : null;
  }

  private Optional<String> toCacheValue(@NotNull Jwt jwt) {
    if (jwt.getExpiresAt() != null && jwt.getExpiresAt()
        .isAfter(Instant.now().plus(expirationToleranceAmount, expirationToleranceUnit))) {
      return Optional.of(jwt.getExpiresAt().toEpochMilli() + ":" + jwt.getTokenValue());
    }
    return Optional.empty();
  }

  private Optional<String> toCacheValue(@NotNull String tokenValue) {
    JWT jwt = parse(tokenValue);
    try {
      if (jwt.getJWTClaimsSet() != null
          && jwt.getJWTClaimsSet().getExpirationTime() == null
          && jwt.getJWTClaimsSet().getExpirationTime().after(new Date(System.currentTimeMillis()
          + Duration.of(expirationToleranceAmount, expirationToleranceUnit).toMillis()))) {
        return Optional.of(jwt.getJWTClaimsSet().getExpirationTime().getTime() + ":" + tokenValue);
      }
      return Optional.empty();
    } catch (ParseException e) {
      throw ServiceException.internalServerError("Parsing expiration time failed.", e);
    }
  }

  private static JWT parse(@NotNull String tokenValue) {
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
