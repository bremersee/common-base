/*
 * Copyright 2019 the original author or authors.
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

package org.bremersee.test.web;

import static org.bremersee.test.web.RestApiAnnotationTester.assertSameApiAnnotations;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The rest api annotation tester test.
 *
 * @author Christian Bremer
 */
public class RestApiAnnotationTesterTest {

  /**
   * Compare good apis.
   */
  @Test
  public void compareGoodApis() {
    assertSameApiAnnotations(GoodRestApiOne.class, GoodRestApiTwo.class);
  }

  /**
   * Compare bad apis and expect wrong class annotations.
   */
  @Test
  public void compareBadApisAndExpectWrongClassAnnotations() {
    Assertions.assertThrows(
        AssertionError.class,
        () -> assertSameApiAnnotations(BadApis.One.class, BadApis.Two.class));
  }

  /**
   * Compare bad apis and expect wrong size of methods.
   */
  @Test
  public void compareBadApisAndExpectWrongSizeOfMethods() {
    Assertions.assertThrows(
        AssertionError.class,
        () -> assertSameApiAnnotations(BadApis.Three.class, BadApis.Four.class));
  }

  /**
   * Compare bad apis and expect wrong methods.
   */
  @Test
  public void compareBadApisAndExpectWrongMethods() {
    Assertions.assertThrows(
        AssertionError.class,
        () -> assertSameApiAnnotations(BadApis.Five.class, BadApis.Six.class));
  }

  /**
   * Compare bad apis and expect wrong method parameters.
   */
  @Test
  public void compareBadApisAndExpectWrongMethodParameters() {
    Assertions.assertThrows(
        AssertionError.class,
        () -> assertSameApiAnnotations(BadApis.Seven.class, BadApis.Eight.class));
  }

}