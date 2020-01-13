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

package org.bremersee.base.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.base.web.app.TestConfiguration;
import org.bremersee.common.model.JavaLocale;
import org.bremersee.common.model.MongoSearchLanguage;
import org.bremersee.common.model.ThreeLetterCountryCode;
import org.bremersee.common.model.ThreeLetterLanguageCode;
import org.bremersee.common.model.TimeZoneId;
import org.bremersee.common.model.TwoLetterCountryCode;
import org.bremersee.common.model.TwoLetterLanguageCode;
import org.bremersee.converter.StringToTimeZoneIdConverter;
import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.ServiceException;
import org.bremersee.exception.model.RestApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

/**
 * The auto configure test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = TestConfiguration.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "bremersee.messages.fallback-to-system-locale=false",
        "bremersee.messages.default-locale=en",
        "spring.application.name=common-base-web-test",
        "bremersee.exception-mapping.api-paths=/api/**"
    })
@Slf4j
public class AutoConfigureTest {

  /**
   * The Rest template.
   */
  @Autowired
  TestRestTemplate restTemplate;

  /**
   * The message source.
   */
  @Autowired
  MessageSource messageSource;

  /**
   * The Rest api exception mapper.
   */
  @Autowired
  RestApiExceptionMapper restApiExceptionMapper;

  /**
   * Test message source.
   */
  @Test
  void testMessageSource() {
    String actual = messageSource.getMessage("test0", null, Locale.ENGLISH);
    assertEquals("Hello JUnit", actual);

    actual = messageSource.getMessage("test1", new Object[]{"Anna"}, Locale.ENGLISH);
    assertEquals("Hello Anna", actual);

    actual = messageSource.getMessage("test0", null, Locale.GERMAN);
    assertEquals("Hallo JUnit", actual);

    actual = messageSource.getMessage("test1", new Object[]{"Anna"}, Locale.GERMAN);
    assertEquals("Hallo Anna", actual);

    actual = messageSource.getMessage("test0", null, Locale.FRANCE);
    assertEquals("Bon jour JUnit", actual);

    actual = messageSource.getMessage("test1", new Object[]{"Anna"}, Locale.FRANCE);
    assertEquals("Bon jour Anna", actual);
  }

  /**
   * Test get api paths.
   */
  @Test
  void testGetApiPaths() {
    assertTrue(restApiExceptionMapper.getApiPaths().contains("/api/**"));
  }

  /**
   * Test build 409.
   */
  @Test
  void testBuild409() {
    final ServiceException exception = ServiceException.builder()
        .httpStatus(409)
        .reason("Either a or b")
        .errorCode("TEST:4711")
        .build();
    final RestApiException model = restApiExceptionMapper.build(exception, "/api/something", null);
    assertNotNull(model);
    assertEquals(exception.getErrorCode(), model.getErrorCode());
    assertFalse(model.getErrorCodeInherited());
    assertEquals(exception.getMessage(), model.getMessage());
    assertEquals("/api/something", model.getPath());
    assertNull(model.getId());
  }

  /**
   * Test build 500.
   */
  @Test
  void testBuild500() {
    final ServiceException exception = ServiceException.builder()
        .httpStatus(500)
        .reason("Something failed.")
        .errorCode("TEST:4711")
        .build();
    final RestApiException model = restApiExceptionMapper.build(exception, "/api/something", null);
    assertNotNull(model);
    assertEquals(exception.getErrorCode(), model.getErrorCode());
    assertFalse(model.getErrorCodeInherited());
    assertEquals(exception.getMessage(), model.getMessage());
    assertEquals("/api/something", model.getPath());
    assertNotNull(model.getId());
  }

  /**
   * Test build with default exception mapping.
   */
  @Test
  void testBuildWithDefaultExceptionMapping() {
    final RuntimeException exception = new RuntimeException("Something went wrong");
    final RestApiException model = restApiExceptionMapper.build(
        exception, "/api/something", null);
    assertNotNull(model);
    assertNull(model.getErrorCode());
    assertFalse(model.getErrorCodeInherited());
    assertEquals(exception.getMessage(), model.getMessage());
    assertEquals("/api/something", model.getPath());
    assertNotNull(model.getId());
  }

  /**
   * Test build with default exception mapping and illegal argument exception.
   */
  @Test
  void testBuildWithDefaultExceptionMappingAndIllegalArgumentException() {
    final IllegalArgumentException exception = new IllegalArgumentException();
    final RestApiException model = restApiExceptionMapper.build(exception, "/api/illegal", null);
    assertNotNull(model);
    assertNull(model.getErrorCode());
    assertFalse(model.getErrorCodeInherited());
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), model.getMessage());
    assertEquals("/api/illegal", model.getPath());
    assertNull(model.getId());
    assertEquals(IllegalArgumentException.class.getName(), model.getClassName());
  }

  /**
   * Test java locale.
   */
  @Test
  void testJavaLocale() {
    JavaLocale expected = JavaLocale.fromLocale(Locale.CANADA_FRENCH);
    String actual = restTemplate.getForEntity("/java-locale/{locale}", String.class, expected)
        .getBody();
    assertNotNull(actual);
    assertEquals(expected.toString(), actual);
  }

  /**
   * Test locale.
   */
  @Test
  void testLocale() {
    Locale expected = Locale.CANADA_FRENCH;
    String actual = restTemplate.getForEntity("/locale/{locale}", String.class, expected)
        .getBody();
    assertNotNull(actual);
    assertEquals(expected.toString(), actual);
  }

  /**
   * Test mongo.
   */
  @Test
  void testMongo() {
    MongoSearchLanguage expected = MongoSearchLanguage.DE;
    String actual = restTemplate.getForEntity("/mongo/{value}", String.class, expected)
        .getBody();
    assertNotNull(actual);
    assertEquals(expected.toString(), actual);
  }

  /**
   * Test three letter country.
   */
  @Test
  void testThreeLetterCountry() {
    ThreeLetterCountryCode expected = ThreeLetterCountryCode.BRA;
    String actual = restTemplate.getForEntity("/3country/{value}", String.class, expected)
        .getBody();
    assertNotNull(actual);
    assertEquals(expected.toString(), actual);
  }

  /**
   * Test three letter language.
   */
  @Test
  void testThreeLetterLanguage() {
    ThreeLetterLanguageCode expected = ThreeLetterLanguageCode.BEL;
    String actual = restTemplate.getForEntity("/3language/{value}", String.class, expected)
        .getBody();
    assertNotNull(actual);
    assertEquals(expected.toString(), actual);
  }

  /**
   * Test two letter country.
   */
  @Test
  void testTwoLetterCountry() {
    TwoLetterCountryCode expected = TwoLetterCountryCode.BR;
    String actual = restTemplate.getForEntity("/2country/{value}", String.class, expected)
        .getBody();
    assertNotNull(actual);
    assertEquals(expected.toString(), actual);
  }

  /**
   * Test two letter language.
   */
  @Test
  void testTwoLetterLanguage() {
    TwoLetterLanguageCode expected = TwoLetterLanguageCode.BE;
    String actual = restTemplate.getForEntity("/2language/{value}", String.class, expected)
        .getBody();
    assertNotNull(actual);
    assertEquals(expected.toString(), actual);
  }

  /**
   * Test time zone.
   *
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  @Test
  void testTimeZone() throws UnsupportedEncodingException {
    TimeZoneId expected = TimeZoneId.EUROPE_BERLIN;
    assertEquals(expected, new StringToTimeZoneIdConverter().convert(expected.toString()));
    String value = URLEncoder.encode(expected.toString(), StandardCharsets.UTF_8.name());
    String actual = restTemplate.getForEntity("/timezone/{value}", String.class, value)
        .getBody();
    assertNotNull(actual);
    assertEquals(expected.toString(), actual);
  }

}
