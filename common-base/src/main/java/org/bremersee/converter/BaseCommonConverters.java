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

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterRegistry;

/**
 * The base common converters.
 *
 * @author Christian Bremer
 */
@Slf4j
public class BaseCommonConverters {

  /**
   * The constant CONVERTERS.
   */
  public static final Converter<?, ?>[] CONVERTERS = new Converter<?, ?>[]{
      new StringToJavaLocaleConverter(),
      new StringToLocaleConverter(),
      new StringToMongoSearchLanguageConverter(),
      new StringToThreeLetterCountryCodeConverter(),
      new StringToThreeLetterLanguageCodeConverter(),
      new StringToTimeZoneConverter(),
      new StringToTimeZoneIdConverter(),
      new StringToTwoLetterCountryCodeConverter(),
      new StringToTwoLetterLanguageCodeConverter(),
      new StringToTimeZoneConverter()
  };

  /**
   * Register all converters.
   *
   * @param registry the registry
   */
  public static void registerAll(ConverterRegistry registry) {
    for (Converter<?, ?> converter : CONVERTERS) {
      log.info("Adding convert {} to registry.", converter);
      registry.addConverter(converter);
    }
  }

}
