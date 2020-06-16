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

package org.bremersee.data.minio.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * The multipart names test.
 *
 * @author Christian Bremer
 */
class MultipartNamesTest {

  /**
   * Gets content part.
   */
  @Test
  void getContentPart() {
    String value = UUID.randomUUID().toString();
    MultipartNames expected = MultipartNames.builder()
        .contentPart(value)
        .build();
    MultipartNames actual = new MultipartNames(value, null, null);
    assertEquals(value, actual.getContentPart());
    assertEquals(value, expected.getContentPart());
    assertEquals(expected.hashCode(), actual.hashCode());
    assertEquals(expected, actual);
    assertNotEquals(expected, null);
    assertNotEquals(expected, new Object());
    assertTrue(actual.toString().contains(value));
  }

  /**
   * Gets content type part.
   */
  @Test
  void getContentTypePart() {
    String value = UUID.randomUUID().toString();
    MultipartNames expected = MultipartNames.builder()
        .contentTypePart(value)
        .build();
    MultipartNames actual = new MultipartNames(null, value, null);
    assertEquals(value, actual.getContentTypePart());
    assertEquals(value, expected.toBuilder().build().getContentTypePart());
    assertEquals(expected.hashCode(), actual.hashCode());
    assertEquals(expected, actual);
    assertNotEquals(expected, null);
    assertNotEquals(expected, new Object());
    assertTrue(actual.toString().contains(value));
  }

  /**
   * Gets filename part.
   */
  @Test
  void getFilenamePart() {
    String value = UUID.randomUUID().toString();
    MultipartNames expected = MultipartNames.builder()
        .filenamePart(value)
        .required(false)
        .build();
    MultipartNames actual = new MultipartNames(null, null, value, false);
    assertEquals(value, actual.getFilenamePart());
    assertEquals(value, expected.toBuilder().build().getFilenamePart());
    assertEquals(expected.hashCode(), actual.hashCode());
    assertEquals(expected, actual);
    assertNotEquals(expected, null);
    assertNotEquals(expected, new Object());
    assertTrue(actual.toString().contains(value));
  }

  /**
   * Is required.
   */
  @Test
  void isRequired() {
    String value = UUID.randomUUID().toString();
    MultipartNames expected = MultipartNames.builder()
        .contentPart(value)
        .required(true)
        .build();
    MultipartNames actual = new MultipartNames(value, null, null, true);
    assertTrue(actual.isRequired());
    assertTrue(expected.isRequired());
    assertEquals(expected.hashCode(), actual.hashCode());
    assertEquals(expected, actual);
  }
}