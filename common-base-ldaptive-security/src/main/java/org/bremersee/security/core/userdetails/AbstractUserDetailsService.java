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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.bremersee.data.ldaptive.LdaptiveEntryMapper;
import org.ldaptive.SearchScope;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

/**
 * The abstract user details service.
 *
 * @author Christian Bremer
 */
@ToString
public abstract class AbstractUserDetailsService {

  @Getter(value = AccessLevel.PROTECTED)
  private final String userBaseDn;

  @Getter(value = AccessLevel.PROTECTED)
  private final String userFindOneFilter;

  @Getter(value = AccessLevel.PROTECTED)
  private final SearchScope userFindOneSearchScope;

  @Getter(value = AccessLevel.PROTECTED)
  private final String userAccountControlAttributeName;

  @Getter(value = AccessLevel.PROTECTED)
  private final List<String> authorities;

  @Getter(value = AccessLevel.PROTECTED)
  private final String authorityAttributeName;

  @Getter(value = AccessLevel.PROTECTED)
  private final boolean authorityDn;

  @Getter(value = AccessLevel.PROTECTED)
  private final Map<String, String> authorityMap;

  @Getter(value = AccessLevel.PROTECTED)
  private final String authorityPrefix;

  /**
   * Instantiates a new Abstract user details service.
   *
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
  public AbstractUserDetailsService(
      String userBaseDn,
      String userFindOneFilter,
      SearchScope userFindOneSearchScope,
      String userAccountControlAttributeName,
      List<String> authorities,
      String authorityAttributeName,
      boolean authorityDn,
      Map<String, String> authorityMap,
      String authorityPrefix) {

    this.userBaseDn = userBaseDn;
    this.userFindOneFilter = userFindOneFilter;
    this.userFindOneSearchScope = userFindOneSearchScope != null ? userFindOneSearchScope : SearchScope.ONELEVEL;
    this.userAccountControlAttributeName = userAccountControlAttributeName;
    this.authorities = authorities;
    this.authorityAttributeName = authorityAttributeName;
    this.authorityDn = authorityDn;
    this.authorityMap = authorityMap;
    this.authorityPrefix = authorityPrefix;
  }

  /**
   * Return attributes.
   *
   * @return the attributes
   */
  protected Set<String> returnAttributes() {
    Set<String> attributes = new HashSet<>();
    if (StringUtils.hasText(userAccountControlAttributeName)) {
      attributes.add(userAccountControlAttributeName);
    }
    if (StringUtils.hasText(authorityAttributeName)) {
      attributes.add(authorityAttributeName);
    }
    return attributes;
  }

  /**
   * Gets user details mapper.
   *
   * @param userName the user name
   * @return the user details mapper
   */
  protected LdaptiveEntryMapper<UserDetails> getUserDetailsLdapMapper(String userName) {
    return new UserDetailsLdapMapper(
        userName,
        userAccountControlAttributeName,
        authorities,
        authorityAttributeName,
        authorityDn,
        authorityMap,
        authorityPrefix);
  }

}
