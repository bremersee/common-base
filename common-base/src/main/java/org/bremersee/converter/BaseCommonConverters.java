/*
 * Copyright 2019 the original author or authors.
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

package org.bremersee.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;

/**
 * The base common converters.
 *
 * @author Christian Bremer
 */
public class BaseCommonConverters {

  /**
   * The constant CONVERTERS.
   */
  @SuppressWarnings("WeakerAccess")
  public static final Converter<?, ?>[] CONVERTERS = new Converter<?, ?>[]{
      new StringToHttpLanguageTagConverter(),
      new StringToJavaLocaleConverter(),
      new StringToMongoSearchLanguageConverter(),
      new StringToThreeLetterCountryCodeConverter(),
      new StringToThreeLetterLanguageCodeConverter(),
      new StringToTimeZoneIdConverter(),
      new StringToTwoLetterCountryCodeConverter(),
      new StringToTwoLetterLanguageCodeConverter()
  };

  /**
   * Register all converters.
   *
   * @param registry the registry
   */
  @SuppressWarnings("unused")
  public static void registerAll(FormatterRegistry registry) {
    for (Converter<?, ?> converter : CONVERTERS) {
      registry.addConverter(converter);
    }
  }

}
