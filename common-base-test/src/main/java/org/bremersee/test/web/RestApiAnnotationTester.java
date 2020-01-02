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
 * The rest api annotation tester.
 *
 * @author Christian Bremer
 */
public class RestApiAnnotationTester {

  private static final Logger log = LoggerFactory.getLogger(RestApiAnnotationTester.class);

  private RestApiAnnotationTester() {
  }

  /**
   * Assert same api annotations.
   *
   * @param expected the expected
   * @param actual   the actual
   */
  public static void assertSameApiAnnotations(Class<?> expected, Class<?> actual) {
    log.info("Assert same api annotations: expected = {}, actual = {}", name(expected),
        name(actual));
    assertNotNull("Expected api class must not be null.", expected);
    assertNotNull("Actual api class must not be null.", actual);
    assertTrue("Expected api class must be an interface.", expected.isInterface());
    assertTrue("Actual api class must be an interface.", expected.isInterface());
    assertSameClassAnnotations(expected, actual);
    assertSameMethodAnnotations(expected, actual);
  }

  private static void assertSameClassAnnotations(Class<?> expected, Class<?> actual) {
    log.info("Assert same class annotations:"
            + "\n  - expected = {}"
            + "\n  - actual   = {}",
        name(expected), name(actual));
    Annotation[] expectedAnnotations = expected.getAnnotations();
    Annotation[] actualAnnotations = actual.getAnnotations();
    assertSameAnnotations(
        "class " + actual.getSimpleName(),
        expectedAnnotations,
        actualAnnotations,
        actual);
  }

  private static void assertSameMethodAnnotations(Class<?> expected, Class<?> actual) {
    Method[] expectedMethods = ReflectionUtils.getDeclaredMethods(expected);
    Method[] actualMethods = ReflectionUtils.getDeclaredMethods(actual);
    assertEquals(format("Methods must have the same size on %s and %s.",
        expected.getSimpleName(), actual.getSimpleName()),
        expectedMethods.length, actualMethods.length);
    for (Method expectedMethod : expectedMethods) {

      log.info("Assert same method annotations of method {}.", expectedMethod.getName());
      Method actualMethod = ReflectionUtils.findMethod(
          actual, expectedMethod.getName(), expectedMethod.getParameterTypes());
      assertNotNull(
          format("Method %s (%s) is missing on %s", expectedMethod.getName(),
              parameters(expectedMethod.getParameterTypes()), name(actual)),
          actualMethod);

      Annotation[] expectedAnnotations = expectedMethod.getAnnotations();
      Annotation[] actualAnnotations = actualMethod.getAnnotations();
      assertSameAnnotations(
          "class " + actual.getSimpleName() + " -> method " + expectedMethod.getName(),
          expectedAnnotations,
          actualAnnotations,
          actualMethod);

      log.info("Assert same method parameter annotations of method {}.", expectedMethod.getName());
      assertEquals(
          format("Parameter size of method %s must be the same on %s.",
              expectedMethod.getName(), actual.getSimpleName()),
          expectedMethod.getParameterCount(), actualMethod.getParameterCount());
      if (expectedMethod.getParameterCount() > 0) {
        Parameter[] expectedParameters = expectedMethod.getParameters();
        Parameter[] actualParameters = actualMethod.getParameters();
        for (int i = 0; i < expectedParameters.length; i++) {
          Parameter expectedParameter = expectedParameters[i];
          Parameter actualParameter = actualParameters[i];
          assertEquals(
              format("Parameter %s of method %s (%s) must be the same on %s",
                  i, expectedMethod.getName(), parameters(expectedMethod.getParameterTypes()),
                  actual.getSimpleName()),
              expectedParameter.getType(), actualParameter.getType());
          assertSameAnnotations(
              "class " + actual.getSimpleName() + " -> method " + expectedMethod.getName(),
              expectedParameter.getAnnotations(),
              actualParameter.getAnnotations(),
              actualParameter);
        }
      }
    }
  }

  private static void assertSameAnnotations(
      String elementPath,
      Annotation[] expectedAnnotations,
      Annotation[] actualAnnotations,
      AnnotatedElement actualAnnotatedElement) {

    if (expectedAnnotations == null) {
      assertNull(
          format("Annotations on %s must be null.", elementPath),
          actualAnnotations);
      return;
    }
    assertEquals(
        format("Annotations must have the same size on %s.", elementPath),
        expectedAnnotations.length, actualAnnotations.length);
    for (Annotation expectedAnnotation : expectedAnnotations) {
      Annotation actualAnnotation = AnnotationUtils.getAnnotation(
          actualAnnotatedElement, expectedAnnotation.annotationType());
      assertSameAnnotation(
          elementPath,
          expectedAnnotation,
          actualAnnotation);
    }
  }

  private static void assertSameAnnotation(
      String elementPath,
      Annotation expected,
      Annotation actual) {

    assertNotNull(
        format("Annotation %s is missing on %s.", name(expected), elementPath),
        actual);
    Map<String, Object> expectedAttributes = AnnotationUtils.getAnnotationAttributes(
        expected, true, false);
    Map<String, Object> actualAttributes = AnnotationUtils.getAnnotationAttributes(
        actual, true, false);
    assertSameAttributes(
        elementPath + " -> annotation " + expected.annotationType().getSimpleName(),
        expectedAttributes,
        actualAttributes);
  }

  private static void assertSameAttributes(
      String elementPath,
      Map<String, Object> expected,
      Map<String, Object> actual) {

    if (expected == null) {
      assertNull(
          format("Attributes of annotation (%s) must be null.", elementPath),
          actual);
      return;
    }
    assertEquals(
        format("Attributes of annotation (%s) must have the same size.", elementPath),
        expected.size(), actual.size());
    for (Map.Entry<String, Object> expectedAttribute : expected.entrySet()) {
      Object expectedAttributeValue = expectedAttribute.getValue();
      Object actualAttributeValue = actual.get(expectedAttribute.getKey());
      assertSameAttributeValues(
          elementPath + " -> attribute " + expectedAttribute.getKey(),
          expectedAttributeValue,
          actualAttributeValue);
    }
  }

  private static void assertSameAttributeValues(
      String elementPath,
      Object expected,
      Object actual) {

    log.info("Assert same attribute values:"
            + "\n  - path     = {}"
            + "\n  - expected = {}"
            + "\n  - actual   = {}",
        elementPath, expected, actual);
    if (expected == null) {
      assertNull(
          format("Attribute (%s) must be null.", elementPath),
          actual);
    } else if (expected instanceof Annotation[]) {
      assertTrue(actual instanceof Annotation[]);
      Annotation[] expectedAnnotations = (Annotation[]) expected;
      Annotation[] actualAnnotations = (Annotation[]) actual;
      assertEquals(expectedAnnotations.length, actualAnnotations.length);
      for (int i = 0; i < actualAnnotations.length; i++) {
        Map<String, Object> expectedAttributes = AnnotationUtils.getAnnotationAttributes(
            expectedAnnotations[i], true, false);
        Map<String, Object> actualAttributes = AnnotationUtils.getAnnotationAttributes(
            actualAnnotations[i], true, false);
        assertSameAttributes(elementPath, expectedAttributes, actualAttributes);
      }
    } else if (expected instanceof Annotation) {
      Annotation expectedAnnotation = (Annotation) expected;
      Annotation actualAnnotation = (Annotation) actual;
      Map<String, Object> expectedAttributes = AnnotationUtils.getAnnotationAttributes(
          expectedAnnotation, true, false);
      Map<String, Object> actualAttributes = AnnotationUtils.getAnnotationAttributes(
          actualAnnotation, true, false);
      assertSameAttributes(elementPath, expectedAttributes, actualAttributes);
    } else {
      if (expected.getClass().isArray()) {
        Object[] expectedArray = (Object[]) expected;
        Object[] actualArray = (Object[]) actual;
        assertArrayEquals(expectedArray, actualArray);
      } else {
        assertEquals(
            expected, actual);
      }
    }
  }

  private static String name(Annotation annotation) {
    return annotation != null ? name(annotation.annotationType()) : "null";
  }

  private static String name(Class<?> clazz) {
    return clazz != null ? clazz.getName() : "null";
  }

  private static String parameters(Class<?>[] parameters) {
    if (parameters == null || parameters.length == 0) {
      return "";
    }
    return StringUtils.collectionToCommaDelimitedString(Arrays.stream(parameters)
        .map(RestApiAnnotationTester::name).collect(Collectors.toList()));
  }

}
