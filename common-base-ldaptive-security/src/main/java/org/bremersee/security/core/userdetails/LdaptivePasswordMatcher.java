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

import java.util.Collections;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.data.ldaptive.LdaptiveOperations;
import org.ldaptive.CompareRequest;
import org.ldaptive.FilterTemplate;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

/**
 * The ldaptive password matcher.
 *
 * @author Christian Bremer
 */
@Slf4j
public class LdaptivePasswordMatcher implements PasswordEncoder {

  @Getter(value = AccessLevel.PROTECTED)
  private final LdaptiveOperations ldaptiveOperations;

  @Getter(value = AccessLevel.PROTECTED)
  private final String userBaseDn;

  @Getter(value = AccessLevel.PROTECTED)
  private final String userFindOneFilter;

  @Getter(value = AccessLevel.PROTECTED)
  private SearchScope userFindOneSearchScope = SearchScope.ONELEVEL;

  @Getter(value = AccessLevel.PROTECTED)
  private String userPasswordAttributeName = "userPassword";

  @Getter(value = AccessLevel.PROTECTED)
  private PasswordEncoder delegate = new LdaptivePasswordEncoder();

  /**
   * Instantiates a new ldaptive password matcher.
   *
   * @param ldaptiveOperations the ldaptive operations
   * @param userBaseDn the user base dn
   * @param userFindOneFilter the user find one filter
   */
  public LdaptivePasswordMatcher(
      LdaptiveOperations ldaptiveOperations,
      String userBaseDn,
      String userFindOneFilter) {

    this.ldaptiveOperations = ldaptiveOperations;
    this.userBaseDn = userBaseDn;
    this.userFindOneFilter = userFindOneFilter;
  }

  /**
   * Sets user find one search scope.
   *
   * @param userFindOneSearchScope the user find one search scope
   */
  public void setUserFindOneSearchScope(SearchScope userFindOneSearchScope) {
    if (userFindOneSearchScope != null) {
      this.userFindOneSearchScope = userFindOneSearchScope;
    }
  }

  /**
   * Sets user password attribute name.
   *
   * @param userPasswordAttributeName the user password attribute name
   */
  public void setUserPasswordAttributeName(String userPasswordAttributeName) {
    if (StringUtils.hasText(userPasswordAttributeName)) {
      this.userPasswordAttributeName = userPasswordAttributeName;
    }
  }

  /**
   * Sets delegate.
   *
   * @param delegate the delegate
   */
  public void setDelegate(PasswordEncoder delegate) {
    if (delegate != null) {
      this.delegate = delegate;
    }
  }

  @Override
  public String encode(CharSequence rawPassword) {
    return getDelegate().encode(rawPassword);
  }

  /**
   * Checks whether the given raw password matches the value in the ldap store. Since the password attribute usually
   * cannot be retrieved and cannot be stored in the user details, the comparison of the passwords is done by the ldap
   * server. For this reason this password encoder implementation expects here the user name as second parameter instead
   * of the encoded password from the user details.
   *
   * @param rawPassword the raw password
   * @param userName the user name of the user
   * @return {@code true} if the raw password matches the password in the ldap store, otherwise {@code false}
   */
  @Override
  public boolean matches(CharSequence rawPassword, String userName) {
    if (!StringUtils.hasText(userName)) {
      log.warn("Ldaptive password matcher: password does not match because there is no user name.");
      return false;
    }
    String raw = rawPassword != null ? rawPassword.toString() : "";
    boolean result = getLdaptiveOperations()
        .findOne(SearchRequest.builder()
            .dn(getUserBaseDn())
            .filter(FilterTemplate.builder()
                .filter(getUserFindOneFilter())
                .parameters(userName)
                .build())
            .scope(getUserFindOneSearchScope())
            .returnAttributes(Collections.emptyList())
            .sizeLimit(1)
            .build())
        .map(LdapEntry::getDn)
        .map(dn -> authenticate(dn, raw))
        .orElse(false);
    if (log.isDebugEnabled()) {
      log.debug("Ldaptive password matcher: password matches for user ({})? {}", userName, result);
    }
    return result;
  }

  private boolean authenticate(String dn, String rawPassword) {
    return getLdaptiveOperations().compare(CompareRequest.builder()
        .dn(dn)
        .name(getUserPasswordAttributeName())
        .value(encode(rawPassword))
        .build());
  }

}
