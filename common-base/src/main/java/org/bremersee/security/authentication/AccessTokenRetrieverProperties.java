package org.bremersee.security.authentication;

import org.springframework.util.MultiValueMap;

public interface AccessTokenRetrieverProperties {

  /**
   * Gets token endpoint.
   *
   * @return the token endpoint
   */
  String getTokenEndpoint();

  /**
   * Create body multi value map.
   *
   * @return the multi value map
   */
  MultiValueMap<String, String> createBody();

}
