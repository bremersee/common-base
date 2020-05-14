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

package org.bremersee.converter.integration.servlet.app;

import java.util.Locale;
import org.bremersee.common.model.JavaLocale;
import org.bremersee.common.model.MongoSearchLanguage;
import org.bremersee.common.model.ThreeLetterCountryCode;
import org.bremersee.common.model.ThreeLetterLanguageCode;
import org.bremersee.common.model.TimeZoneId;
import org.bremersee.common.model.TwoLetterCountryCode;
import org.bremersee.common.model.TwoLetterLanguageCode;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The test configuration.
 *
 * @author Christian Bremer
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = {TestConfiguration.class})
public class TestConfiguration {

  /**
   * The converter controller.
   *
   * @author Christian Bremer
   */
  @RestController
  public static class ConverterController {

    /**
     * Convert java locale response entity.
     *
     * @param javaLocale the java locale
     * @return the response entity
     */
    @GetMapping(path = "/java-locale/{locale}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> convertJavaLocale(@PathVariable("locale") JavaLocale javaLocale) {
      return ResponseEntity.ok(javaLocale.toString());
    }

    /**
     * Convert locale response entity.
     *
     * @param locale the locale
     * @return the response entity
     */
    @GetMapping(path = "/locale/{locale}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> convertLocale(@PathVariable("locale") Locale locale) {
      return ResponseEntity.ok(locale.toString());
    }

    /**
     * Convert mongo search language response entity.
     *
     * @param value the value
     * @return the response entity
     */
    @GetMapping(path = "/mongo/{value}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> convertMongoSearchLanguage(
        @PathVariable("value") MongoSearchLanguage value) {
      return ResponseEntity.ok(value.toString());
    }

    /**
     * Convert three letter country response entity.
     *
     * @param value the value
     * @return the response entity
     */
    @GetMapping(path = "/3country/{value}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> convertThreeLetterCountry(
        @PathVariable("value") ThreeLetterCountryCode value) {
      return ResponseEntity.ok(value.toString());
    }

    /**
     * Convert three letter language response entity.
     *
     * @param value the value
     * @return the response entity
     */
    @GetMapping(path = "/3language/{value}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> convertThreeLetterLanguage(
        @PathVariable("value") ThreeLetterLanguageCode value) {
      return ResponseEntity.ok(value.toString());
    }

    /**
     * Convert two letter country response entity.
     *
     * @param value the value
     * @return the response entity
     */
    @GetMapping(path = "/2country/{value}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> convertTwoLetterCountry(
        @PathVariable("value") TwoLetterCountryCode value) {
      return ResponseEntity.ok(value.toString());
    }

    /**
     * Convert two letter language response entity.
     *
     * @param value the value
     * @return the response entity
     */
    @GetMapping(path = "/2language/{value}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> convertTwoLetterLanguage(
        @PathVariable("value") TwoLetterLanguageCode value) {
      return ResponseEntity.ok(value.toString());
    }

    /**
     * Convert time zone response entity.
     *
     * @param value the value
     * @return the response entity
     */
    @GetMapping(path = "/timezone", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> convertTimeZone(
        @RequestParam("value") TimeZoneId value) {
      return ResponseEntity.ok(value.toString());
    }

  }
}
