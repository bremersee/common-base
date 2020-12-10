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

package org.bremersee.test.beans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;

/**
 * The simple object provider test.
 */
class SimpleObjectProviderTest {

  /**
   * Gets object.
   */
  @Test
  void getObject() {
    String provides = "A bean";
    assertEquals("A bean", new SimpleObjectProvider<>(provides).getObject());

    assertThrows(
        NoSuchBeanDefinitionException.class,
        () -> new SimpleObjectProvider<>(null).getObject());
  }

  /**
   * If available.
   */
  @Test
  void ifAvailable() {
    new SimpleObjectProvider<>("1234", s -> assertEquals(4, s.length()))
        .ifAvailable(s -> {
          throw new RuntimeException("Should not happen");
        });

    assertThrows(RuntimeException.class, () -> {
      ObjectProvider<String> provider = new SimpleObjectProvider<>("1234");
      provider.ifAvailable(s -> {
        throw new RuntimeException("Should happen");
      });
    });
  }

  /**
   * If unique.
   */
  @Test
  void ifUnique() {
    new SimpleObjectProvider<>("1234", s -> assertEquals(4, s.length()))
        .ifUnique(s -> {
          throw new RuntimeException("Should not happen");
        });
    new SimpleObjectProvider<>(
        null,
        s -> {
          throw new RuntimeException("Should not happen");
        })
        .ifUnique(s -> {
          throw new RuntimeException("Should not happen");
        });
  }

  /**
   * Gets if available.
   */
  @Test
  void getIfAvailable() {
    String provides = "A bean";
    assertEquals("A bean", new SimpleObjectProvider<>(provides).getIfAvailable());
    assertNull(new SimpleObjectProvider<>(null).getIfAvailable());
  }

  /**
   * Gets if unique.
   */
  @Test
  void getIfUnique() {
    String provides = "A bean";
    assertEquals("A bean", new SimpleObjectProvider<>(provides).getIfUnique());
    assertNull(new SimpleObjectProvider<>(null).getIfUnique());
  }

  /**
   * Test get object.
   */
  @Test
  void testGetObject() {
    String provides = "A bean";
    assertEquals("A bean", new SimpleObjectProvider<>(provides).getObject("a", "b"));

    assertThrows(
        NoSuchBeanDefinitionException.class,
        () -> new SimpleObjectProvider<>(null).getObject("a", "b"));
  }
}