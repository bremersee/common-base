package org.bremersee.security.authentication;

import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

/**
 * The interface Reactive access token cache.
 */
@Validated
public interface ReactiveAccessTokenCache {

  /**
   * The constant CACHE_NAME.
   */
  String CACHE_NAME = AccessTokenCache.CACHE_NAME;

  /**
   * Find not expired access token from cache.
   *
   * @param key the key
   * @return the access token
   */
  Mono<String> findAccessToken(@NotNull String key);

  /**
   * Put new access token into the cache.
   *
   * @param key the key
   * @param accessToken the access token
   * @return the access token
   */
  Mono<String> putAccessToken(@NotNull String key, @NotNull String accessToken);

  static ReactiveAccessTokenCache from(@NotNull AccessTokenCache accessTokenCache) {
    return new ReactiveAccessTokenCache() {
      @Override
      public Mono<String> findAccessToken(@NotNull String key) {
        return accessTokenCache.findAccessToken(key).map(Mono::just).orElse(Mono.empty());
      }

      @Override
      public Mono<String> putAccessToken(@NotNull String key, @NotNull String accessToken) {
        accessTokenCache.putAccessToken(key, accessToken);
        return Mono.just(accessToken);
      }

      @Override
      public String toString() {
        return "Reactive wrapper of " + accessTokenCache;
      }
    };
  }

}
