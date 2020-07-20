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
import javax.validation.constraints.NotNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;

/**
 * The authentication details interface.
 *
 * @author Christian Bremer
 */
@Validated
public interface AuthenticationDetails {

  /**
   * Gets default locale.
   *
   * @return the default locale
   */
  @NotNull
  default Locale getDefaultLocale() {
    return Locale.getDefault();
  }

  /**
   * Gets default time zone.
   *
   * @return the default time zone
   */
  @NotNull
  default TimeZone getDefaultTimeZone() {
    return TimeZone.getDefault();
  }

  /**
   * Gets preferred language.
   *
   * @param authentication the authentication
   * @return the preferred language
   */
  Optional<Locale> getPreferredLanguage(@Nullable Authentication authentication);

  /**
   * Gets preferred language null safe.
   *
   * @param authentication the authentication
   * @return the preferred language null safe
   */
  @NotNull
  default Locale getPreferredLanguageNullSafe(@Nullable Authentication authentication) {
    return getPreferredLanguage(authentication)
        .orElseGet(this::getDefaultLocale);
  }

  /**
   * Gets preferred time zone.
   *
   * @param authentication the authentication
   * @return the preferred time zone
   */
  Optional<TimeZone> getPreferredTimeZone(@Nullable Authentication authentication);

  /**
   * Gets preferred time zone null safe.
   *
   * @param authentication the authentication
   * @return the preferred time zone null safe
   */
  @NotNull
  default TimeZone getPreferredTimeZoneNullSafe(@Nullable Authentication authentication) {
    return getPreferredTimeZone(authentication)
        .orElseGet(this::getDefaultTimeZone);
  }

}
