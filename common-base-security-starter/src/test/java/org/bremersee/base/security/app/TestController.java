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

package org.bremersee.base.security.app;

import java.util.Locale;
import org.bremersee.common.model.JavaLocale;
import org.bremersee.common.model.MongoSearchLanguage;
import org.bremersee.common.model.ThreeLetterCountryCode;
import org.bremersee.common.model.ThreeLetterLanguageCode;
import org.bremersee.common.model.TimeZoneId;
import org.bremersee.common.model.TwoLetterCountryCode;
import org.bremersee.common.model.TwoLetterLanguageCode;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * The test controller.
 *
 * @author Christian Bremer
 */
@RestController
public class TestController {

  /**
   * Convert java locale response entity.
   *
   * @param javaLocale the java locale
   * @return the response entity
   */
  @GetMapping(path = "/public/java-locale/{locale}", produces = MediaType.TEXT_PLAIN_VALUE)
  public Mono<String> convertJavaLocale(@PathVariable("locale") JavaLocale javaLocale) {
    return Mono.just(javaLocale.toString());
  }

  /**
   * Convert locale response entity.
   *
   * @param locale the locale
   * @return the response entity
   */
  @GetMapping(path = "/public/locale/{locale}", produces = MediaType.TEXT_PLAIN_VALUE)
  public Mono<String> convertLocale(@PathVariable("locale") Locale locale) {
    return Mono.just(locale.toString());
  }

  /**
   * Convert mongo search language response entity.
   *
   * @param value the value
   * @return the response entity
   */
  @GetMapping(path = "/public/mongo/{value}", produces = MediaType.TEXT_PLAIN_VALUE)
  public Mono<String> convertMongoSearchLanguage(
      @PathVariable("value") MongoSearchLanguage value) {
    return Mono.just(value.toString());
  }

  /**
   * Convert three letter country response entity.
   *
   * @param value the value
   * @return the response entity
   */
  @GetMapping(path = "/public/3country/{value}", produces = MediaType.TEXT_PLAIN_VALUE)
  public Mono<String> convertThreeLetterCountry(
      @PathVariable("value") ThreeLetterCountryCode value) {
    return Mono.just(value.toString());
  }

  /**
   * Convert three letter language response entity.
   *
   * @param value the value
   * @return the response entity
   */
  @GetMapping(path = "/public/3language/{value}", produces = MediaType.TEXT_PLAIN_VALUE)
  public Mono<String> convertThreeLetterLanguage(
      @PathVariable("value") ThreeLetterLanguageCode value) {
    return Mono.just(value.toString());
  }

  /**
   * Convert two letter country response entity.
   *
   * @param value the value
   * @return the response entity
   */
  @GetMapping(path = "/public/2country/{value}", produces = MediaType.TEXT_PLAIN_VALUE)
  public Mono<String> convertTwoLetterCountry(
      @PathVariable("value") TwoLetterCountryCode value) {
    return Mono.just(value.toString());
  }

  /**
   * Convert two letter language response entity.
   *
   * @param value the value
   * @return the response entity
   */
  @GetMapping(path = "/public/2language/{value}", produces = MediaType.TEXT_PLAIN_VALUE)
  public Mono<String> convertTwoLetterLanguage(
      @PathVariable("value") TwoLetterLanguageCode value) {
    return Mono.just(value.toString());
  }

  /**
   * Convert time zone response entity.
   *
   * @param value the value
   * @return the response entity
   */
  @GetMapping(path = "/public/timezone/{value}", produces = MediaType.TEXT_PLAIN_VALUE)
  public Mono<String> convertTimeZone(
      @PathVariable("value") TimeZoneId value) {
    return Mono.just(value.toString());
  }

}
