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

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.Date;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The offset date time converter test.
 *
 * @author Christian Bremer
 */
class OffsetDateTimeConverterTest {

  private static DateToOffsetDateTimeReadConverter dateToConverter;

  private static OffsetDateTimeToDateWriteConverter fromDateConverter;

  private static InstantToOffsetDateTimeReadConverter instantToConverter;

  private static OffsetDateTimeToInstantWriteConverter fromInstantConverter;

  /**
   * Sets up.
   */
  @BeforeAll
  static void setUp() {
    dateToConverter = new DateToOffsetDateTimeReadConverter();
    fromDateConverter = new OffsetDateTimeToDateWriteConverter();
    instantToConverter = new InstantToOffsetDateTimeReadConverter();
    fromInstantConverter = new OffsetDateTimeToInstantWriteConverter();
  }

  /**
   * Convert.
   */
  @Test
  void convert() {
    Date expected = new Date();

    OffsetDateTime actual = dateToConverter.convert(expected);
    assertNotNull(actual);
    assertEquals(expected, Date.from(actual.toInstant()));
    assertEquals(expected, fromDateConverter.convert(actual));

    actual = instantToConverter.convert(expected.toInstant());
    assertNotNull(actual);
    assertEquals(expected, Date.from(actual.toInstant()));
    assertEquals(expected.toInstant(), fromInstantConverter.convert(actual));
  }
}