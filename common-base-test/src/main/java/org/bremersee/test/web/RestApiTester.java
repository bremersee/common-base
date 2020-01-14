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

import static java.lang.String.format;
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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
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

    log.info("Assert same api: expected = {}, actual = {}", name(expected),
        name(actual));
    assertNotNull("Expected api class must not be null.", expected);
    assertNotNull("Actual api class must not be null.", actual);

    RestApiTesterPath path = pathBuilder().add(CLASS, expected.getSimpleName()).build();
    if (!isExcluded(path, CLASS_MUST_BE_INTERFACE, exclusions)) {
      log.info("Assert given class is an interface:"
              + "\n  - path = {}"
              + "\n  - type = {}",
          path, CLASS_MUST_BE_INTERFACE);
      assertTrue("Expected api class must be an interface.", expected.isInterface());
    }

    path = pathBuilder().add(CLASS, actual.getSimpleName()).build();
    if (!isExcluded(path, CLASS_MUST_BE_INTERFACE, exclusions)) {
      log.info("Assert given class is an interface:"
              + "\n  - path = {}"
              + "\n  - type = {}",
          path, CLASS_MUST_BE_INTERFACE);
      assertTrue("Actual api class must be an interface.", actual.isInterface());
    }

    assertSameClassAnnotations(expected, actual, exclusions);
    assertSameMethodAnnotations(expected, actual, exclusions);
  }

  private static void assertSameClassAnnotations(
      final Class<?> expected,
      final Class<?> actual,
      final RestApiTesterExclusion... exclusions) {

    final Annotation[] expectedAnnotations = expected.getAnnotations();
    final Annotation[] actualAnnotations = actual.getAnnotations();
    assertSameAnnotations(
        pathBuilder().add(CLASS, actual.getSimpleName()).build(),
        expectedAnnotations,
        actualAnnotations,
        actual,
        exclusions);
  }

  private static void assertSameMethodAnnotations(
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
      assertEquals(format("Methods must have the same size on %s and %s.",
          expected.getSimpleName(), actual.getSimpleName()),
          expectedMethods.length, actualMethods.length);
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
        assertNotNull(
            format("Method %s (%s) is missing on %s", expectedMethod.getName(),
                parameters(expectedMethod.getParameterTypes()), name(actual)),
            actualMethod);
      } else if (actualMethod == null) {
        continue;
      }

      final Annotation[] expectedAnnotations = expectedMethod.getAnnotations();
      final Annotation[] actualAnnotations = actualMethod.getAnnotations();
      assertSameAnnotations(
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
      assertEquals(
          format("Annotations must have the same size on %s.", path),
          expectedAnnotations.length, actualAnnotations.length);
    }
    for (final Annotation expectedAnnotation : expectedAnnotations) {
      final Annotation actualAnnotation = AnnotationUtils.getAnnotation(
          actualAnnotatedElement, expectedAnnotation.annotationType());
      assertSameAnnotation(
          path,
          expectedAnnotation,
          actualAnnotation,
          exclusions);
    }
  }

  private static void assertSameAnnotation(
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
      assertNotNull(
          format("Annotation %s is missing on %s.", name(expected), path),
          actual);
    }
    if (actual == null) {
      return;
    }
    final Map<String, Object> expectedAttributes = AnnotationUtils.getAnnotationAttributes(
        expected, true, false);
    final Map<String, Object> actualAttributes = AnnotationUtils.getAnnotationAttributes(
        actual, true, false);
    assertSameAttributes(
        annotationPath,
        expectedAttributes,
        actualAttributes,
        exclusions);
  }

  private static void assertSameAttributes(
      final RestApiTesterPath annotationPath,
      final Map<String, Object> expected,
      final Map<String, Object> actual,
      final RestApiTesterExclusion... exclusions) {

    if (!isExcluded(annotationPath, SAME_ANNOTATION_ATTRIBUTES_SIZE, exclusions)) {
      log.info("Assert same annotation attributes size:"
              + "\n  - path = {}"
              + "\n  - type = {}",
          annotationPath, SAME_ANNOTATION_ATTRIBUTES_SIZE);
      assertEquals(
          format("Attributes of annotation (%s) must have the same size.", annotationPath),
          expected.size(), actual.size());
    }
    for (final Map.Entry<String, Object> expectedAttribute : expected.entrySet()) {
      final Object expectedAttributeValue = expectedAttribute.getValue();
      final Object actualAttributeValue = actual.get(expectedAttribute.getKey());
      assertSameAttributeValues(
          annotationPath.toPathBuilder().add(ATTRIBUTE, expectedAttribute.getKey()).build(),
          expectedAttributeValue,
          actualAttributeValue,
          exclusions);
    }
  }

  private static void assertSameAttributeValues(
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

      if (expected == null) {
        assertNull(
            format("Attribute (%s) must be null.", attributePath),
            actual);
      } else if (expected instanceof Annotation[]) {
        assertTrue(actual instanceof Annotation[]);
        final Annotation[] expectedAnnotations = (Annotation[]) expected;
        final Annotation[] actualAnnotations = (Annotation[]) actual;
        assertEquals(expectedAnnotations.length, actualAnnotations.length);
        for (int i = 0; i < actualAnnotations.length; i++) {
          final Annotation expectedAnnotation = expectedAnnotations[i];
          final Annotation actualAnnotation = actualAnnotations[i];
          assertSameAnnotation(
              attributePath,
              expectedAnnotation,
              actualAnnotation,
              exclusions);
        }
      } else if (expected instanceof Annotation) {
        final Annotation expectedAnnotation = (Annotation) expected;
        final Annotation actualAnnotation = (Annotation) actual;
        assertSameAnnotation(
            attributePath,
            expectedAnnotation,
            actualAnnotation,
            exclusions);
      } else {
        if (expected.getClass().isArray()) {
          final Object[] expectedArray = (Object[]) expected;
          final Object[] actualArray = (Object[]) actual;
          assertArrayEquals(expectedArray, actualArray);
        } else {
          assertEquals(
              expected, actual);
        }
      }
    }
  }

  private static String name(final Annotation annotation) {
    return annotation != null ? name(annotation.annotationType()) : "null";
  }

  private static String name(final Class<?> clazz) {
    return clazz != null ? clazz.getName() : "null";
  }

  private static String parameters(final Class<?>[] parameters) {
    if (parameters == null || parameters.length == 0) {
      return "";
    }
    return StringUtils.collectionToCommaDelimitedString(Arrays.stream(parameters)
        .map(RestApiTester::name).collect(Collectors.toList()));
  }

}
