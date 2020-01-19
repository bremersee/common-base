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

import static org.bremersee.test.web.RestApiTester.assertSameApi;
import static org.bremersee.test.web.RestApiAssertionType.ANNOTATION_MUST_NOT_BE_NULL;
import static org.bremersee.test.web.RestApiAssertionType.METHOD_MUST_NOT_BE_NULL;
import static org.bremersee.test.web.RestApiAssertionType.SAME_ANNOTATION_ATTRIBUTE_VALUE;
import static org.bremersee.test.web.RestApiAssertionType.SAME_ANNOTATION_SIZE;
import static org.bremersee.test.web.RestApiAssertionType.SAME_METHOD_SIZE;
import static org.bremersee.test.web.RestApiTesterExclusion.exclusionBuilder;
import static org.bremersee.test.web.RestApiTesterPath.PathType.ANNOTATION;
import static org.bremersee.test.web.RestApiTesterPath.PathType.ATTRIBUTE;
import static org.bremersee.test.web.RestApiTesterPath.PathType.CLASS;
import static org.bremersee.test.web.RestApiTesterPath.PathType.METHOD;
import static org.bremersee.test.web.RestApiTesterPath.pathBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The rest api tester test.
 *
 * @author Christian Bremer
 */
class RestApiTesterTest {

  /**
   * Compare good apis.
   */
  @Test
  void compareGoodApis() {
    assertSameApi(GoodRestApiOne.class, GoodRestApiTwo.class);
  }

  /**
   * Compare bad apis and expect wrong class annotations.
   */
  @Test
  void compareBadApisAndExpectWrongClassAnnotations() {
    Assertions.assertThrows(
        AssertionError.class,
        () -> assertSameApi(BadApis.One.class, BadApis.Two.class));
  }

  /**
   * Compare bad apis and expect wrong size of methods.
   */
  @Test
  void compareBadApisAndExpectWrongSizeOfMethods() {
    Assertions.assertThrows(
        AssertionError.class,
        () -> assertSameApi(BadApis.Three.class, BadApis.Four.class));
  }

  /**
   * Compare bad apis and expect wrong methods.
   */
  @Test
  void compareBadApisAndExpectWrongMethods() {
    Assertions.assertThrows(
        AssertionError.class,
        () -> assertSameApi(BadApis.Five.class, BadApis.Six.class));
  }

  /**
   * Compare bad apis and expect wrong method parameters.
   */
  @Test
  void compareBadApisAndExpectWrongMethodParameters() {
    Assertions.assertThrows(
        AssertionError.class,
        () -> assertSameApi(BadApis.Seven.class, BadApis.Eight.class));
  }

  /**
   * Compare bad apis but exclude exclude different values.
   */
  @Test
  void compareBadApisButExcludeDifferentValues() {
    assertSameApi(
        BadApis.Three.class,
        BadApis.Four.class,
        exclusionBuilder()
            .path(pathBuilder()
                .add(CLASS, "Four")
                .build())
            .type(SAME_METHOD_SIZE)
            .build(),
        exclusionBuilder()
            .path(pathBuilder()
                .add(CLASS, "Four")
                .add(METHOD, "getGeometries")
                .build())
            .type(METHOD_MUST_NOT_BE_NULL)
            .build(),
        exclusionBuilder()
            .path(pathBuilder()
                .add(CLASS, "Four")
                .add(METHOD, "updateGeometry")
                .build())
            .type(METHOD_MUST_NOT_BE_NULL)
            .build(),
        exclusionBuilder()
            .path(pathBuilder()
                .add(CLASS, "Four")
                .add(METHOD, "addGeometry")
                .build())
            .type(SAME_ANNOTATION_SIZE)
            .build(),
        exclusionBuilder()
            .path(pathBuilder()
                .add(CLASS, "Four")
                .add(METHOD, "addGeometry")
                .add(ANNOTATION, "PostMapping")
                .build())
            .type(ANNOTATION_MUST_NOT_BE_NULL)
            .build(),
        exclusionBuilder()
            .path(pathBuilder()
                .add(CLASS, "Four")
                .add(METHOD, "getGeometry")
                .add(ANNOTATION, "ApiOperation")
                .add(ATTRIBUTE, "response")
                .build())
            .type(SAME_ANNOTATION_ATTRIBUTE_VALUE)
            .build(),
        exclusionBuilder()
            .path(pathBuilder()
                .add(CLASS, "Four")
                .add(METHOD, "getGeometry")
                .add(ANNOTATION, "ApiResponses")
                .add(ATTRIBUTE, "value")
                .add(ANNOTATION, "ApiResponse")
                .add(ATTRIBUTE, "response")
                .build())
            .type(SAME_ANNOTATION_ATTRIBUTE_VALUE)
            .build()
    );
  }
}