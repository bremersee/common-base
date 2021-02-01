/*
 * Copyright 2021 the original author or authors.
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

package org.bremersee.security.core.userdetails;

import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.data.ldaptive.reactive.ReactiveLdaptiveOperations;
import org.ldaptive.FilterTemplate;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

/**
 * The reactive ldaptive user details service.
 *
 * @author Christian Bremer
 */
@ToString(callSuper = true, exclude = {"ldaptiveOperations"})
@Slf4j
public class ReactiveLdaptiveUserDetailsService extends AbstractUserDetailsService implements
    ReactiveUserDetailsService {

  @Getter(value = AccessLevel.PROTECTED)
  private final ReactiveLdaptiveOperations ldaptiveOperations;

  /**
   * Instantiates a new reactive ldaptive user details service.
   *
   * @param ldaptiveOperations the ldaptive operations
   * @param userBaseDn the user base dn
   * @param userFindOneFilter the user find one filter
   * @param userFindOneSearchScope the user find one search scope
   * @param userAccountControlAttributeName the user account control attribute name
   * @param authorities the authorities
   * @param authorityAttributeName the authority attribute name
   * @param authorityDn the authority dn
   * @param authorityMap the authority map
   * @param authorityPrefix the authority prefix
   */
  public ReactiveLdaptiveUserDetailsService(
      ReactiveLdaptiveOperations ldaptiveOperations,
      String userBaseDn,
      String userFindOneFilter,
      SearchScope userFindOneSearchScope,
      String userAccountControlAttributeName,
      List<String> authorities,
      String authorityAttributeName,
      boolean authorityDn,
      Map<String, String> authorityMap,
      String authorityPrefix) {

    super(
        userBaseDn, userFindOneFilter, userFindOneSearchScope, userAccountControlAttributeName, authorities,
        authorityAttributeName, authorityDn, authorityMap, authorityPrefix);
    this.ldaptiveOperations = ldaptiveOperations;
  }

  @Override
  public Mono<UserDetails> findByUsername(String userName) {
    return getLdaptiveOperations()
        .findOne(
            SearchRequest.builder()
                .dn(getUserBaseDn())
                .filter(FilterTemplate.builder()
                    .filter(getUserFindOneFilter())
                    .parameters(userName)
                    .build())
                .scope(getUserFindOneSearchScope())
                .returnAttributes(returnAttributes())
                .sizeLimit(1)
                .build(),
            getUserDetailsLdapMapper(userName));
  }

}
