/*
 * Copyright 2020 the original author or authors.
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

package org.bremersee.security.authentication;

import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import org.bremersee.common.model.JavaLocale;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;

/**
 * The json path jwt authentication details.
 *
 * @author Christian Bremer
 */
public class JsonPathJwtAuthenticationDetails implements AuthenticationDetails {

  private final Locale defaultLocale;

  private final TimeZone defaultTimeZone;

  private final String preferredLanguageJsonPath;

  private final String preferredTimeZoneJsonPath;

  /**
   * Instantiates a new json path jwt authentication details.
   *
   * @param defaultLocale the default locale
   * @param defaultTimeZone the default time zone
   * @param preferredLanguageJsonPath the preferred language json path
   * @param preferredTimeZoneJsonPath the preferred time zone json path
   */
  public JsonPathJwtAuthenticationDetails(
      Locale defaultLocale,
      TimeZone defaultTimeZone,
      String preferredLanguageJsonPath,
      String preferredTimeZoneJsonPath) {
    this.defaultLocale = defaultLocale != null ? defaultLocale : Locale.getDefault();
    this.defaultTimeZone = defaultTimeZone != null ? defaultTimeZone : TimeZone.getDefault();
    this.preferredLanguageJsonPath = preferredLanguageJsonPath;
    this.preferredTimeZoneJsonPath = preferredTimeZoneJsonPath;
  }

  @Override
  public Locale getDefaultLocale() {
    return defaultLocale;
  }

  @Override
  public TimeZone getDefaultTimeZone() {
    return defaultTimeZone;
  }

  @Override
  public Optional<Locale> getPreferredLanguage(Authentication authentication) {
    return Optional.ofNullable(authentication)
        .filter(auth -> StringUtils.hasText(preferredLanguageJsonPath)
            && auth instanceof JwtAuthenticationToken)
        .map(auth -> ((JwtAuthenticationToken) auth).getToken())
        .map(JsonPathJwtParser::new)
        .map(parser -> parser.read(preferredLanguageJsonPath, String.class))
        .map(language -> JavaLocale.fromValue(language).toLocale());
  }

  @Override
  public Optional<TimeZone> getPreferredTimeZone(Authentication authentication) {
    if (!StringUtils.hasText(preferredTimeZoneJsonPath)) {
      return Optional.empty();
    }
    return Optional.ofNullable(authentication)
        .filter(auth -> StringUtils.hasText(preferredTimeZoneJsonPath)
            && auth instanceof JwtAuthenticationToken)
        .map(auth -> ((JwtAuthenticationToken) auth).getToken())
        .map(JsonPathJwtParser::new)
        .map(parser -> parser.read(preferredTimeZoneJsonPath, String.class))
        .map(TimeZone::getTimeZone);
  }

}
