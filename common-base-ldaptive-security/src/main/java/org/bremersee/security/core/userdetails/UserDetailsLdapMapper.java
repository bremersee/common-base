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

import static org.bremersee.data.ldaptive.LdaptiveEntryMapper.getAttributeValue;
import static org.bremersee.data.ldaptive.LdaptiveEntryMapper.getAttributeValuesAsSet;
import static org.bremersee.data.ldaptive.transcoder.UserAccountControlValueTranscoder.isUserAccountEnabled;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.bremersee.data.ldaptive.LdaptiveEntryMapper;
import org.bremersee.data.ldaptive.transcoder.UserAccountControlValueTranscoder;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;
import org.ldaptive.transcode.AbstractStringValueTranscoder;
import org.ldaptive.transcode.ValueTranscoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

/**
 * The user details mapper.
 *
 * @author Christian Bremer
 */
@ToString
public class UserDetailsLdapMapper implements LdaptiveEntryMapper<UserDetails> {

  @Getter(value = AccessLevel.PROTECTED)
  private final String userName;

  @Getter(value = AccessLevel.PROTECTED)
  private final String userAccountControlAttributeName;

  @Getter(value = AccessLevel.PROTECTED)
  private final List<String> authorities;

  @Getter(value = AccessLevel.PROTECTED)
  private final String authorityAttributeName;

  @Getter(value = AccessLevel.PROTECTED)
  private final String authorityPrefix;

  @Getter(value = AccessLevel.PROTECTED)
  private final ValueTranscoder<GrantedAuthority> authorityTranscoder;

  @Getter(value = AccessLevel.PROTECTED)
  private final UserAccountControlValueTranscoder userAccountControlValueTranscoder;

  /**
   * Instantiates a new user details mapper.
   *
   * @param userName the user name
   * @param userAccountControlAttributeName the user account control attribute name
   * @param authorities the authorities
   * @param authorityAttributeName the authority attribute name
   * @param authorityDn the authority dn
   * @param authorityMap the authority map
   * @param authorityPrefix the authority prefix
   */
  public UserDetailsLdapMapper(
      String userName,
      String userAccountControlAttributeName,
      List<String> authorities,
      String authorityAttributeName,
      boolean authorityDn,
      Map<String, String> authorityMap,
      String authorityPrefix) {

    this.userName = userName;
    this.userAccountControlAttributeName = userAccountControlAttributeName;
    this.authorities = authorities != null ? authorities : Collections.emptyList();
    this.authorityAttributeName = authorityAttributeName;
    this.authorityPrefix = authorityPrefix;
    this.authorityTranscoder = new GrantedAuthorityValueTranscoder(authorityDn, authorityMap, authorityPrefix);
    if (StringUtils.hasText(userAccountControlAttributeName)) {
      userAccountControlValueTranscoder = new UserAccountControlValueTranscoder();
    } else {
      userAccountControlValueTranscoder = null;
    }
  }

  @Override
  public String[] getObjectClasses() {
    return null;
  }

  @Override
  public String mapDn(UserDetails domainObject) {
    return null;
  }

  @Override
  public UserDetails map(LdapEntry ldapEntry) {
    return new User(
        userName,
        userName,
        isAccountEnabled(ldapEntry),
        isAccountNonExpired(ldapEntry),
        isCredentialsNonExpired(ldapEntry),
        isAccountNonLocked(ldapEntry),
        getGrantedAuthorities(ldapEntry));
  }

  @Override
  public void map(LdapEntry source, UserDetails destination) {
    throw new UnsupportedOperationException("User details are unmodifiable.");
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(UserDetails source, LdapEntry destination) {
    return new AttributeModification[0];
  }

  /**
   * Determines whether the account is enabled or not.
   *
   * @param ldapEntry the ldap entry
   * @return the boolean
   */
  protected boolean isAccountEnabled(LdapEntry ldapEntry) {
    return !StringUtils.hasText(getUserAccountControlAttributeName()) || isUserAccountEnabled(getAttributeValue(
        ldapEntry, getUserAccountControlAttributeName(), getUserAccountControlValueTranscoder(), null));
  }

  /**
   * Determines whether the account is not expired.
   *
   * @param ldapEntry the ldap entry
   * @return the boolean
   */
  protected boolean isAccountNonExpired(@SuppressWarnings("unused") LdapEntry ldapEntry) {
    return true;
  }

  /**
   * Determines whether the account credentials are not expired.
   *
   * @param ldapEntry the ldap entry
   * @return the boolean
   */
  protected boolean isCredentialsNonExpired(@SuppressWarnings("unused") LdapEntry ldapEntry) {
    return true;
  }

  /**
   * Determines whether the account is not locked.
   *
   * @param ldapEntry the ldap entry
   * @return the boolean
   */
  protected boolean isAccountNonLocked(@SuppressWarnings("unused") LdapEntry ldapEntry) {
    return true;
  }

  /**
   * Gets granted authorities.
   *
   * @param ldapEntry the ldap entry
   * @return the granted authorities
   */
  protected Collection<? extends GrantedAuthority> getGrantedAuthorities(LdapEntry ldapEntry) {
    Set<GrantedAuthority> grantedAuthorities = getAuthorities().stream()
        .map(value -> UserDetailsLdapMapper.prefixAuthority(getAuthorityPrefix(), value))
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());
    if (StringUtils.hasText(getAuthorityAttributeName())) {
      grantedAuthorities.addAll(
          getAttributeValuesAsSet(ldapEntry, getAuthorityAttributeName(), getAuthorityTranscoder()));
    }
    return grantedAuthorities;
  }

  /**
   * Prefix authority.
   *
   * @param prefix the prefix
   * @param value the value
   * @return the string
   */
  protected static String prefixAuthority(String prefix, String value) {
    return StringUtils.hasText(prefix) && !value.startsWith(prefix) ? prefix + value : value;
  }

  /**
   * The granted authority value transcoder.
   *
   * @author Christian Bremer
   */
  @ToString
  protected static class GrantedAuthorityValueTranscoder extends AbstractStringValueTranscoder<GrantedAuthority> {

    @Getter(value = AccessLevel.PROTECTED)
    private final boolean authorityDn;

    @Getter(value = AccessLevel.PROTECTED)
    private final Map<String, String> authorityMap;

    @Getter(value = AccessLevel.PROTECTED)
    private final String authorityPrefix;

    /**
     * Instantiates a new granted authority value transcoder.
     *
     * @param authorityDn the authority dn
     * @param authorityMap the authority map
     * @param authorityPrefix the authority prefix
     */
    public GrantedAuthorityValueTranscoder(
        boolean authorityDn,
        Map<String, String> authorityMap,
        String authorityPrefix) {
      this.authorityDn = authorityDn;
      this.authorityMap = authorityMap != null ? authorityMap : Collections.emptyMap();
      this.authorityPrefix = authorityPrefix;
    }

    @Override
    public GrantedAuthority decodeStringValue(String value) {
      String ldapValue = isAuthorityDn() ? LdaptiveEntryMapper.getRdn(value) : value;
      String mappedValue = getAuthorityMap().getOrDefault(ldapValue, ldapValue);
      String authorityValue = UserDetailsLdapMapper.prefixAuthority(getAuthorityPrefix(), mappedValue);
      return new SimpleGrantedAuthority(authorityValue);
    }

    @Override
    public String encodeStringValue(GrantedAuthority value) {
      throw new UnsupportedOperationException("Getting ldap attribute value from granted authority is not supported.");
    }

    @Override
    public Class<GrantedAuthority> getType() {
      return GrantedAuthority.class;
    }
  }

}
