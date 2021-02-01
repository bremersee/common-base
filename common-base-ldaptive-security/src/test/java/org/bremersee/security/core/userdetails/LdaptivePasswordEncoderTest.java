/*
 * Copyright 2021 the original author or authors.
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

package org.bremersee.security.core.userdetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bremersee.exception.ServiceException;
import org.junit.jupiter.api.Test;

/**
 * The ldaptive password encoder test.
 */
class LdaptivePasswordEncoderTest {

  /**
   * Plain.
   */
  @Test
  void plain() {
    LdaptivePasswordEncoder encoder = LdaptivePasswordEncoder.plain();
    assertEquals("plain", encoder.getLabel());
    assertNull(encoder.getAlgorithm());
    assertEquals("{plain}HelloWorld", encoder.encode("HelloWorld"));
    assertTrue(encoder.matches("HelloWorld", "{plain}HelloWorld"));
  }

  /**
   * Plain with no label.
   */
  @Test
  void plainWithNoLabel() {
    LdaptivePasswordEncoder encoder = LdaptivePasswordEncoder.plainWithNoLabel();
    assertNull(encoder.getLabel());
    assertNull(encoder.getAlgorithm());
    assertEquals("HelloWorld", encoder.encode("HelloWorld"));
    assertTrue(encoder.matches("HelloWorld", "HelloWorld"));
  }

  /**
   * Encode.
   */
  @Test
  void encode() {
    LdaptivePasswordEncoder encoder = new LdaptivePasswordEncoder();
    String enc = encoder.encode("HelloWorld");
    assertNotNull(enc);
    assertEquals("{SHA}24rBwlnridShMbJTus/KXzGdVPI=", enc);
  }

  /**
   * Encode and expect service exception.
   */
  @Test
  void encodeAndExpectServiceException() {
    LdaptivePasswordEncoder encoder = new LdaptivePasswordEncoder("FOO", "BAR");
    assertThrows(ServiceException.class, () -> encoder.encode("HelloWorld"));
  }

  /**
   * Matches.
   */
  @Test
  void matches() {
    LdaptivePasswordEncoder encoder = new LdaptivePasswordEncoder();
    assertTrue(encoder.matches("HelloWorld", "{SHA}24rBwlnridShMbJTus/KXzGdVPI="));
  }

  /**
   * Matches with other algorithm.
   */
  @Test
  void matchesWithOtherAlgorithm() {
    LdaptivePasswordEncoder encoder = new LdaptivePasswordEncoder("MD5", "MD5");
    assertTrue(encoder.matches("HelloWorld", "{MD5}aOEJ8PQMpyoV4FzCJ4b45g=="));
  }

  /**
   * Matches with no algorithm.
   */
  @Test
  void matchesWithNoAlgorithm() {
    LdaptivePasswordEncoder encoder = new LdaptivePasswordEncoder(null, "MD5");
    assertTrue(encoder.matches("HelloWorld", "aOEJ8PQMpyoV4FzCJ4b45g=="));
  }

  /**
   * Matches with delegate.
   */
  @Test
  void matchesWithDelegate() {
    LdaptivePasswordEncoder encoder = new LdaptivePasswordEncoder();
    assertTrue(encoder.matches(
        "HelloWorld",
        "{bcrypt}$2a$10$5clGJ2Fh3z4bNgMEPUBbHuBxlXZPJfgjeegr95EWdXZHMomjnzHhu"));
  }

  /**
   * Not matches.
   */
  @Test
  void notMatches() {
    LdaptivePasswordEncoder encoder = new LdaptivePasswordEncoder();
    assertFalse(encoder.matches(
        "HelloWorld",
        "$2a$10$5clGJ2Fh3z4bNgMEPUBbHuBxlXZPJfgjeegr95EWdXZHMomjnzHhu"));
  }

  /**
   * Test to string.
   */
  @Test
  void testToString() {
    LdaptivePasswordEncoder encoder = new LdaptivePasswordEncoder("ABC", "XYZ");
    assertTrue(encoder.toString().contains("ABC"));
    assertTrue(encoder.toString().contains("XYZ"));
  }

}