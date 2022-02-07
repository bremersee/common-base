/*
 * Copyright 2020-2022 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;

/**
 * The mock object provider test.
 *
 * @author Christian Bremer
 */
class MockObjectProviderTest {

  /**
   * Test get object.
   */
  @Test
  void testGetObject() {
    String expected = "A bean";
    String actual = new MockObjectProvider<>(expected).getObject();
    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Test get object and expect no such bean definition exception.
   */
  @Test
  void testGetObjectAndExpectNoSuchBeanDefinitionException() {
    assertThatExceptionOfType(NoSuchBeanDefinitionException.class)
        .isThrownBy(() -> new MockObjectProvider<>(null).getObject());
  }


  /**
   * Test get object with parameters.
   */
  @Test
  void testGetObjectWithParameters() {
    String expected = "A bean";
    String actual = new MockObjectProvider<>(expected).getObject("a", "b");
    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Test get object with parameters and expect no such bean definition exception.
   */
  @Test
  void testGetObjectWithParametersAndExpectNoSuchBeanDefinitionException() {
    ObjectProvider<?> target = new MockObjectProvider<>(null);
    assertThatExceptionOfType(NoSuchBeanDefinitionException.class)
        .isThrownBy(() -> target.getObject("a", "b"));
  }

  /**
   * Test if available.
   */
  @Test
  void testIfAvailable() {
    ObjectProvider<String> target = new MockObjectProvider<>(
        "1234",
        s -> assertThat(s).hasSize(4));

    // The given consumer will not be executed,
    // because the one of the target will be executed instead.
    assertThatNoException()
        .isThrownBy(() -> target.ifAvailable(s -> {
          throw new RuntimeException("Should not happen.");
        }));
  }

  /**
   * Test if available with null.
   */
  @Test
  void testIfAvailableWithNull() {
    ObjectProvider<String> target = new MockObjectProvider<>(null);
    assertThatNoException()
        .isThrownBy(() -> target.ifAvailable(s -> {
          throw new RuntimeException("Should not happen.");
        }));
  }

  /**
   * Test if available and expect runtime exception.
   */
  @Test
  void testIfAvailableAndExpectRuntimeException() {
    ObjectProvider<String> target = new MockObjectProvider<>("1234");
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> target.ifAvailable(s -> {
          throw new RuntimeException("Should happen");
        }));
  }

  /**
   * Test if unique.
   */
  @Test
  void testIfUnique() {
    ObjectProvider<String> target = new MockObjectProvider<>(
        "1234",
        s -> assertThat(s).hasSize(4));

    // The given consumer will not be executed,
    // because the one of the target will be executed instead.
    assertThatNoException()
        .isThrownBy(() -> target.ifUnique(s -> {
          throw new RuntimeException("Should not happen.");
        }));
  }

  /**
   * Test if unique with null.
   */
  @Test
  void testIfUniqueWithNull() {
    ObjectProvider<String> target = new MockObjectProvider<>(null);
    assertThatNoException()
        .isThrownBy(() -> target.ifUnique(s -> {
          throw new RuntimeException("Should not happen.");
        }));
  }

  /**
   * Test if unique and expect runtime exception.
   */
  @Test
  void testIfUniqueAndExpectRuntimeException() {
    ObjectProvider<String> target = new MockObjectProvider<>("1234");
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> target.ifUnique(s -> {
          throw new RuntimeException("Should happen");
        }));
  }

  /**
   * Gets if available.
   */
  @Test
  void getIfAvailable() {
    String expected = "A bean";
    String actual = new MockObjectProvider<>(expected).getIfAvailable();
    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Gets if available with null.
   */
  @Test
  void getIfAvailableWithNull() {
    assertThat(new MockObjectProvider<>(null).getIfAvailable())
        .isNull();
  }

  /**
   * Gets if unique.
   */
  @Test
  void getIfUnique() {
    String expected = "A bean";
    String actual = new MockObjectProvider<>(expected).getIfUnique();
    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Gets if unique with null.
   */
  @Test
  void getIfUniqueWithNull() {
    assertThat(new MockObjectProvider<>(null).getIfUnique())
        .isNull();
  }
}