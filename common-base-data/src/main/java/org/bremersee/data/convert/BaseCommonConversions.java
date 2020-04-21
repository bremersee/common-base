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

package org.bremersee.data.convert;

/**
 * The base common conversions.
 */
public class BaseCommonConversions {

  /**
   * The constant CONVERTERS.
   */
  public static final Object[] CONVERTERS = {
      new DateToOffsetDateTimeReadConverter(),
      new InstantToOffsetDateTimeReadConverter(),
      new JavaLocaleReadConverter(),
      new JavaLocaleWriteConverter(),
      new LocaleReadConverter(),
      new LocaleWriteConverter(),
      new OffsetDateTimeToDateWriteConverter(),
      new OffsetDateTimeToInstantWriteConverter(),
      new ThreeLetterCountryCodeReadConverter(),
      new ThreeLetterCountryCodeWriteConverter(),
      new ThreeLetterLanguageCodeReadConverter(),
      new ThreeLetterLanguageCodeWriteConverter(),
      new TimeZoneReadConverter(),
      new TimeZoneWriteConverter(),
      new TimeZoneIdReadConverter(),
      new TimeZoneIdWriteConverter(),
      new TwoLetterCountryCodeReadConverter(),
      new TwoLetterCountryCodeWriteConverter(),
      new TwoLetterLanguageCodeReadConverter(),
      new TwoLetterLanguageCodeWriteConverter()
  };

}
