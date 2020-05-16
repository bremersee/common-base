package org.bremersee.security.authentication;

import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;

/**
 * The interface Access token cache.
 */
@Validated
public interface AccessTokenCache {

  /**
   * The constant CACHE_NAME.
   */
  String CACHE_NAME = "jwt";

  /**
   * Find not expired access token from cache.
   *
   * @param key the key
   * @return the access token
   */
  Optional<String> findAccessToken(@NotNull String key);

  /**
   * Find not expired jwt from cache.
   *
   * @param key the key
   * @return the optional
   */
  Optional<Jwt> findJwt(String key);

  /**
   * Put new access token into the cache.
   *
   * @param key the key
   * @param accessToken the access token
   */
  void putAccessToken(@NotNull String key, @NotNull String accessToken);

  /**
   * Put new jwt into the cache.
   *
   * @param key the key
   * @param jwt the jwt
   */
  void putJwt(@NotNull String key, @NotNull Jwt jwt);

}
