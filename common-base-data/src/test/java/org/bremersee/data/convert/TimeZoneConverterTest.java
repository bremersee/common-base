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

import java.util.TimeZone;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The time zone converter test.
 *
 * @author Christian Bremer
 */
class TimeZoneConverterTest {

  private static TimeZoneReadConverter readConverter;

  private static TimeZoneWriteConverter writeConverter;

  /**
   * Sets up.
   */
  @BeforeAll
  static void setUp() {
    readConverter = new TimeZoneReadConverter();
    writeConverter = new TimeZoneWriteConverter();
  }

  /**
   * Convert.
   */
  @Test
  void convert() {
    TimeZone expected = TimeZone.getTimeZone("Europe/Berlin");
    String actual = writeConverter.convert(expected);
    assertNotNull(actual);
    assertEquals("Europe/Berlin", actual);
    assertEquals(expected, readConverter.convert(actual));
  }
}