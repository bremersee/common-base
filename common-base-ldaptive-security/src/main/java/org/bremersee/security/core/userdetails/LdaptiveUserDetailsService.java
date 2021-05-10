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
import org.bremersee.data.ldaptive.LdaptiveOperations;
import org.ldaptive.FilterTemplate;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * The ldaptive user details service.
 *
 * @author Christian Bremer
 */
@ToString(callSuper = true, exclude = {"ldaptiveOperations"})
public class LdaptiveUserDetailsService extends AbstractUserDetailsService implements UserDetailsService {

  @Getter(value = AccessLevel.PROTECTED)
  private final LdaptiveOperations ldaptiveOperations;

  /**
   * Instantiates a new ldaptive user details service.
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
  public LdaptiveUserDetailsService(
      LdaptiveOperations ldaptiveOperations,
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
  public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
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
            getUserDetailsLdapMapper(userName))
        .orElseThrow(() -> new UsernameNotFoundException("User '" + userName + "' was not found."));
  }

}
