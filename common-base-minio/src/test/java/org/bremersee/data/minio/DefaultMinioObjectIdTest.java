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

package org.bremersee.data.minio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The default minio object id test.
 *
 * @author Christian Bremer
 */
class DefaultMinioObjectIdTest {

  /**
   * Gets name.
   */
  @Test
  void getName() {
    assertEquals("foobar", new DefaultMinioObjectId("foobar").getName());
  }

  /**
   * Gets version id.
   */
  @Test
  void getVersionId() {
    assertEquals("foobar", new DefaultMinioObjectId("name", "foobar").getVersionId());
  }

  /**
   * Equals and hash code.
   */
  @SuppressWarnings({"SimplifiableJUnitAssertion", "EqualsWithItself", "ConstantConditions"})
  @Test
  void equalsAndHashCode() {
    MinioObjectId id0 = new DefaultMinioObjectId("a-name", "a-version");
    MinioObjectId id1 = new DefaultMinioObjectId(id0);
    assertTrue(id0.equals(id0));
    assertFalse(id0.equals(null));
    assertFalse(id0.equals(new Object()));
    assertTrue(id0.equals(id1));
    assertEquals(id0.hashCode(), id1.hashCode());
    assertEquals(id0.toString(), id1.toString());
  }
}