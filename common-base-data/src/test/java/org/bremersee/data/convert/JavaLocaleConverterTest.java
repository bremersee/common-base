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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Locale;
import org.bremersee.common.model.JavaLocale;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The java locale converter test.
 *
 * @author Christian Bremer
 */
class JavaLocaleConverterTest {

  private static JavaLocaleReadConverter readConverter;

  private static JavaLocaleWriteConverter writeConverter;

  /**
   * Sets up.
   */
  @BeforeAll
  static void setUp() {
    readConverter = new JavaLocaleReadConverter();
    writeConverter = new JavaLocaleWriteConverter();
  }

  /**
   * Convert.
   */
  @Test
  void convert() {
    JavaLocale expected = JavaLocale.fromLocale(Locale.CANADA_FRENCH);
    String actual = writeConverter.convert(expected);
    assertNotNull(actual);
    assertEquals("fr-CA", actual);
    assertEquals(expected, readConverter.convert(actual));

    expected = JavaLocale.fromLocale(Locale.GERMAN);
    actual = writeConverter.convert(expected);
    assertNotNull(actual);
    assertEquals("de", actual);
    assertEquals(expected, readConverter.convert(actual));

    actual = writeConverter.convert(new JavaLocale());
    assertNotNull(actual);
    assertEquals("", actual);
    assertEquals(new JavaLocale(), readConverter.convert(actual));
  }
}