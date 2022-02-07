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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.bremersee.test.web.RestApiAssertionType.ANNOTATION_MUST_NOT_BE_NULL;
import static org.bremersee.test.web.RestApiAssertionType.CLASS_MUST_BE_INTERFACE;
import static org.bremersee.test.web.RestApiAssertionType.METHOD_MUST_NOT_BE_NULL;
import static org.bremersee.test.web.RestApiAssertionType.SAME_ANNOTATION_ATTRIBUTES_SIZE;
import static org.bremersee.test.web.RestApiAssertionType.SAME_ANNOTATION_ATTRIBUTE_VALUE;
import static org.bremersee.test.web.RestApiAssertionType.SAME_ANNOTATION_SIZE;
import static org.bremersee.test.web.RestApiAssertionType.SAME_METHOD_SIZE;
import static org.bremersee.test.web.RestApiTesterExclusion.isExcluded;
import static org.bremersee.test.web.RestApiTesterPath.PathType.ANNOTATION;
import static org.bremersee.test.web.RestApiTesterPath.PathType.ATTRIBUTE;
import static org.bremersee.test.web.RestApiTesterPath.PathType.CLASS;
import static org.bremersee.test.web.RestApiTesterPath.PathType.METHOD;
import static org.bremersee.test.web.RestApiTesterPath.PathType.METHOD_PARAMETER;
import static org.bremersee.test.web.RestApiTesterPath.pathBuilder;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.assertj.core.api.SoftAssertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * The rest api tester.
 *
 * @author Christian Bremer
 */
public class RestApiTester {

  private static final Logger log = LoggerFactory.getLogger(RestApiTester.class);

  private RestApiTester() {
  }

  /**
   * Assert same api.
   *
   * @param expected the expected
   * @param actual the actual
   * @param exclusions the exclusions
   */
  public static void assertSameApi(
      final Class<?> expected,
      final Class<?> actual,
      final RestApiTesterExclusion... exclusions) {

    assertSameApi(null, true, expected, actual, exclusions);
  }

  /**
   * Assert same api.
   *
   * @param softAssertions the soft assertions
   * @param assertAll the assert all
   * @param expected the expected
   * @param actual the actual
   * @param exclusions the exclusions
   */
  public static void assertSameApi(
      final SoftAssertions softAssertions,
      final boolean assertAll,
      final Class<?> expected,
      final Class<?> actual,
      final RestApiTesterExclusion... exclusions) {

    log.info("Assert same api: expected = {}, actual = {}", name(expected),
        name(actual));
    Assert.notNull(expected, "Expected api class must not be null.");
    Assert.notNull(actual, "Actual api class must not be null.");

    SoftAssertions softly;
    boolean internalAssertAll;
    if (Objects.nonNull(softAssertions)) {
      softly = softAssertions;
      internalAssertAll = assertAll;
    } else {
      softly = new SoftAssertions();
      internalAssertAll = true;
    }

    RestApiTesterPath path = pathBuilder().add(CLASS, expected.getSimpleName()).build();
    if (!isExcluded(path, CLASS_MUST_BE_INTERFACE, exclusions)) {
      log.info("Assert given class is an interface:"
              + "\n  - path = {}"
              + "\n  - type = {}",
          path, CLASS_MUST_BE_INTERFACE);
      softly.assertThat(expected.isInterface())
          .as("Expected api class must be an interface.")
          .isTrue();
    }

    path = pathBuilder().add(CLASS, actual.getSimpleName()).build();
    if (!isExcluded(path, CLASS_MUST_BE_INTERFACE, exclusions)) {
      log.info("Assert given class is an interface:"
              + "\n  - path = {}"
              + "\n  - type = {}",
          path, CLASS_MUST_BE_INTERFACE);
      softly.assertThat(actual.isInterface())
          .as("Actual api class must be an interface.")
          .isTrue();
    }

    assertSameClassAnnotations(softly, expected, actual, exclusions);
    assertSameMethodAnnotations(softly, expected, actual, exclusions);

    if (internalAssertAll) {
      softly.assertAll();
    }
  }

  private static void assertSameClassAnnotations(
      SoftAssertions softly,
      final Class<?> expected,
      final Class<?> actual,
      final RestApiTesterExclusion... exclusions) {

    final Annotation[] expectedAnnotations = expected.getAnnotations();
    final Annotation[] actualAnnotations = actual.getAnnotations();
    assertSameAnnotations(
        softly,
        pathBuilder().add(CLASS, actual.getSimpleName()).build(),
        expectedAnnotations,
        actualAnnotations,
        actual,
        exclusions);
  }

  private static void assertSameMethodAnnotations(
      SoftAssertions softly,
      final Class<?> expected,
      final Class<?> actual,
      final RestApiTesterExclusion... exclusions) {

    final RestApiTesterPath classPath = pathBuilder().add(CLASS, actual.getSimpleName()).build();
    final Method[] expectedMethods = ReflectionUtils.getDeclaredMethods(expected);
    final Method[] actualMethods = ReflectionUtils.getDeclaredMethods(actual);

    if (!isExcluded(classPath, SAME_METHOD_SIZE, exclusions)) {
      log.info("Assert same method size:"
              + "\n  - path = {}"
              + "\n  - type = {}",
          classPath, SAME_METHOD_SIZE);
      softly.assertThat(actualMethods.length)
          .as("Methods must have the same size on %s and %s.",
              expected.getSimpleName(), actual.getSimpleName())
          .isEqualTo(expectedMethods.length);
    }

    for (final Method expectedMethod : expectedMethods) {
      final RestApiTesterPath methodPath = classPath.toPathBuilder()
          .add(METHOD, expectedMethod.getName())
          .build();
      final Method actualMethod = ReflectionUtils.findMethod(
          actual, expectedMethod.getName(), expectedMethod.getParameterTypes());
      if (!isExcluded(methodPath, METHOD_MUST_NOT_BE_NULL, exclusions)) {
        log.info("Assert method exists:"
                + "\n  - path = {}"
                + "\n  - type = {}",
            methodPath, METHOD_MUST_NOT_BE_NULL);
        softly.assertThat(actualMethod)
            .as("Method %s (%s) must be present on %s", expectedMethod.getName(),
                parameters(expectedMethod.getParameterTypes()), name(actual))
            .isNotNull();
      }
      if (isNull(actualMethod)) {
        continue;
      }

      final Annotation[] expectedAnnotations = expectedMethod.getAnnotations();
      final Annotation[] actualAnnotations = actualMethod.getAnnotations();
      assertSameAnnotations(
          softly,
          methodPath,
          expectedAnnotations,
          actualAnnotations,
          actualMethod,
          exclusions);

      if (expectedMethod.getParameterCount() > 0) {
        final Parameter[] expectedParameters = expectedMethod.getParameters();
        final Parameter[] actualParameters = actualMethod.getParameters();
        for (int i = 0; i < expectedParameters.length; i++) {
          final Parameter expectedParameter = expectedParameters[i];
          final Parameter actualParameter = actualParameters[i];
          final RestApiTesterPath methodParameterPath = methodPath.toPathBuilder()
              .add(METHOD_PARAMETER, String.valueOf(i))
              .build();
          assertSameAnnotations(
              softly,
              methodParameterPath,
              expectedParameter.getAnnotations(),
              actualParameter.getAnnotations(),
              actualParameter,
              exclusions);
        }
      }
    }
  }

  private static void assertSameAnnotations(
      final SoftAssertions softly,
      final RestApiTesterPath path,
      final Annotation[] expectedAnnotations,
      final Annotation[] actualAnnotations,
      final AnnotatedElement actualAnnotatedElement,
      final RestApiTesterExclusion... exclusions) {

    if (!isExcluded(path, SAME_ANNOTATION_SIZE, exclusions)) {
      log.info("Assert same annotation size:"
              + "\n  - path = {}"
              + "\n  - type = {}",
          path, SAME_ANNOTATION_SIZE);
      softly.assertThat(actualAnnotations.length)
          .as("Annotations must have the same size on %s.", path)
          .isEqualTo(expectedAnnotations.length);
    }
    for (final Annotation expectedAnnotation : expectedAnnotations) {
      final Annotation actualAnnotation = AnnotationUtils.getAnnotation(
          actualAnnotatedElement, expectedAnnotation.annotationType());
      assertSameAnnotation(
          softly,
          path,
          expectedAnnotation,
          actualAnnotation,
          exclusions);
    }
  }

  private static void assertSameAnnotation(
      final SoftAssertions softly,
      final RestApiTesterPath path,
      final Annotation expected,
      final Annotation actual,
      final RestApiTesterExclusion... exclusions) {

    final RestApiTesterPath annotationPath = path.toPathBuilder()
        .add(ANNOTATION, expected.annotationType().getSimpleName())
        .build();
    if (!isExcluded(annotationPath, ANNOTATION_MUST_NOT_BE_NULL, exclusions)) {
      log.info("Assert annotation is not null:"
              + "\n  - path = {}"
              + "\n  - type = {}",
          annotationPath, ANNOTATION_MUST_NOT_BE_NULL);
      softly.assertThat(actual)
          .as("Annotation %s is missing on %s.", name(expected), path)
          .isNotNull();
    }
    if (isNull(actual)) {
      return;
    }
    final Map<String, Object> expectedAttributes = AnnotationUtils.getAnnotationAttributes(
        expected, true, false);
    final Map<String, Object> actualAttributes = AnnotationUtils.getAnnotationAttributes(
        actual, true, false);
    assertSameAttributes(
        softly,
        annotationPath,
        expectedAttributes,
        actualAttributes,
        exclusions);
  }

  private static void assertSameAttributes(
      final SoftAssertions softly,
      final RestApiTesterPath annotationPath,
      final Map<String, Object> expected,
      final Map<String, Object> actual,
      final RestApiTesterExclusion... exclusions) {

    if (!isExcluded(annotationPath, SAME_ANNOTATION_ATTRIBUTES_SIZE, exclusions)) {
      log.info("Assert same annotation attributes size:"
              + "\n  - path = {}"
              + "\n  - type = {}",
          annotationPath, SAME_ANNOTATION_ATTRIBUTES_SIZE);
      softly.assertThat(actual.size())
          .as("Attributes of annotation (%s) must have the same size.", annotationPath)
          .isEqualTo(expected.size());
    }
    for (final Map.Entry<String, Object> expectedAttribute : expected.entrySet()) {
      final Object expectedAttributeValue = expectedAttribute.getValue();
      final Object actualAttributeValue = actual.get(expectedAttribute.getKey());
      assertSameAttributeValues(
          softly,
          annotationPath.toPathBuilder().add(ATTRIBUTE, expectedAttribute.getKey()).build(),
          expectedAttributeValue,
          actualAttributeValue,
          exclusions);
    }
  }

  private static void assertSameAttributeValues(
      final SoftAssertions softly,
      final RestApiTesterPath attributePath,
      final Object expected,
      final Object actual,
      final RestApiTesterExclusion... exclusions) {

    if (!isExcluded(attributePath, SAME_ANNOTATION_ATTRIBUTE_VALUE, exclusions)) {
      log.info("Assert same attribute values:"
              + "\n  - path     = {}"
              + "\n  - expected = {}"
              + "\n  - actual   = {}"
              + "\n  - type     = {}",
          attributePath, expected, actual, SAME_ANNOTATION_ATTRIBUTE_VALUE);

      if (isNull(exclusions)) {
        softly.assertThat(actual)
            .as("Attribute (%s) must be null.", attributePath)
            .isNull();
      } else if (expected instanceof Annotation[]) {
        softly.assertThat(actual)
            .as("Attributes must be instance of 'Annotation[]' on %s", attributePath)
            .isInstanceOf(Annotation[].class);
        final Annotation[] expectedAnnotations = (Annotation[]) expected;
        final Annotation[] actualAnnotations = (Annotation[]) actual;
        softly.assertThat(actualAnnotations.length)
            .as("Annotations must have same size on %s", attributePath)
            .isEqualTo(expectedAnnotations.length);
        for (int i = 0; i < actualAnnotations.length; i++) {
          final Annotation expectedAnnotation = expectedAnnotations[i];
          final Annotation actualAnnotation = actualAnnotations[i];
          assertSameAnnotation(
              softly,
              attributePath,
              expectedAnnotation,
              actualAnnotation,
              exclusions);
        }
      } else if (expected instanceof Annotation) {
        final Annotation expectedAnnotation = (Annotation) expected;
        final Annotation actualAnnotation = (Annotation) actual;
        assertSameAnnotation(
            softly,
            attributePath,
            expectedAnnotation,
            actualAnnotation,
            exclusions);
      } else {
        if (expected.getClass().isArray()) {
          final Object[] expectedArray = (Object[]) expected;
          final Object[] actualArray = (Object[]) actual;
          softly.assertThat(actualArray)
              .as("Arrays must be equal on %s", attributePath)
              .isEqualTo(expectedArray);
        } else {
          softly.assertThat(actual)
              .as("Objects must be equal on %s", attributePath)
              .isEqualTo(expected);
        }
      }
    }
  }

  private static String name(final Annotation annotation) {
    return nonNull(annotation) ? name(annotation.annotationType()) : "null";
  }

  private static String name(final Class<?> clazz) {
    return nonNull(clazz) ? clazz.getName() : "null";
  }

  private static String parameters(final Class<?>[] parameters) {
    if (isEmpty(parameters)) {
      return "";
    }
    return StringUtils.collectionToCommaDelimitedString(Arrays.stream(parameters)
        .map(RestApiTester::name).collect(Collectors.toList()));
  }

}
