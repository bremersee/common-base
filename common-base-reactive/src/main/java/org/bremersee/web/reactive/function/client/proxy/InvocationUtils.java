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

package org.bremersee.web.reactive.function.client.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriBuilder;

/**
 * The invocation utils.
 *
 * @author Christian Bremer
 */
abstract class InvocationUtils {

  private InvocationUtils() {
  }

  /**
   * Gets request path.
   *
   * @param cls    the cls
   * @param method the method
   * @return the request path
   */
  static String getRequestPath(final Class<?> cls, final Method method) {

    // Request mapping on class
    String clsPath = findRequestMappingValue(cls, a -> a.value().length > 0, a -> a.value()[0])
        .orElseGet(() -> findRequestMappingValue(cls, a -> a.path().length > 0, a -> a.path()[0])
            .orElse(""));

    // Request mapping on method
    String mPath = findRequestMappingValue(method, a -> a.value().length > 0, a -> a.value()[0])
        .orElseGet(() -> findRequestMappingValue(method, a -> a.path().length > 0, a -> a.path()[0])
            .orElse(""));
    if (StringUtils.hasText(mPath)) {
      return clsPath + mPath;
    }

    // Get mapping on method
    mPath = findGetMappingValue(method, a -> a.value().length > 0, a -> a.value()[0])
        .orElseGet(() -> findGetMappingValue(method, a -> a.path().length > 0, a -> a.path()[0])
            .orElse(""));
    if (StringUtils.hasText(mPath)) {
      return clsPath + mPath;
    }

    // Post mapping on method
    mPath = findPostMappingValue(method, a -> a.value().length > 0, a -> a.value()[0])
        .orElseGet(() -> findPostMappingValue(method, a -> a.path().length > 0, a -> a.path()[0])
            .orElse(""));
    if (StringUtils.hasText(mPath)) {
      return clsPath + mPath;
    }

    // Put mapping on method
    mPath = findPutMappingValue(method, a -> a.value().length > 0, a -> a.value()[0])
        .orElseGet(() -> findPutMappingValue(method, a -> a.path().length > 0, a -> a.path()[0])
            .orElse(""));
    if (StringUtils.hasText(mPath)) {
      return clsPath + mPath;
    }

    // Patch mapping on method
    mPath = findPatchMappingValue(method, a -> a.value().length > 0, a -> a.value()[0])
        .orElseGet(() -> findPatchMappingValue(method, a -> a.path().length > 0, a -> a.path()[0])
            .orElse(""));
    if (StringUtils.hasText(mPath)) {
      return clsPath + mPath;
    }

    // Delete mapping on method
    mPath = findDeleteMappingValue(method, a -> a.value().length > 0, a -> a.value()[0])
        .orElseGet(() -> findDeleteMappingValue(method, a -> a.path().length > 0, a -> a.path()[0])
            .orElse(""));

    return clsPath + mPath;
  }

  /**
   * Gets path variables.
   *
   * @param method the method
   * @param args   the args
   * @return the path variables
   */
  static Map<String, Object> getPathVariables(final Method method, final Object[] args) {
    final Map<String, Object> values = new HashMap<>();
    final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    for (int i = 0; i < parameterAnnotations.length; i++) {
      for (final Annotation annotation : parameterAnnotations[i]) {
        if (annotation instanceof PathVariable) {
          final PathVariable param = (PathVariable) annotation;
          final String name = StringUtils.hasText(param.value()) ? param.value() : param.name();
          final Object value = args[i];
          values.put(name, value);
        }
      }
    }
    return values;
  }

  /**
   * Gets request params.
   *
   * @param method the method
   * @param args   the args
   * @return the request params
   */
  @SuppressWarnings("WeakerAccess")
  static Map<String, Object[]> getRequestParams(final Method method, final Object[] args) {
    final Map<String, Object[]> values = new HashMap<>();
    final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    for (int i = 0; i < parameterAnnotations.length; i++) {
      for (final Annotation annotation : parameterAnnotations[i]) {
        if (annotation instanceof RequestParam) {
          final RequestParam param = (RequestParam) annotation;
          final String name = StringUtils.hasText(param.value()) ? param.value() : param.name();
          final Object value = args[i];
          if (value instanceof Map) {
            final Map<?, ?> map = (Map) value;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
              final String key = String.valueOf(entry.getKey());
              final Object mapValue = entry.getValue();
              if (mapValue instanceof Collection) {
                values.put(key, ((Collection<?>) mapValue).toArray(new Object[0]));
              } else {
                values.put(key, new Object[]{mapValue});
              }
            }
          } else if (value instanceof Collection) {
            values.put(name, ((Collection<?>) value).toArray(new Object[0]));
          } else {
            values.put(name, new Object[]{value});
          }
        }
      }
    }
    return values;
  }

  /**
   * Sets request params.
   *
   * @param method     the method
   * @param args       the args
   * @param uriBuilder the uri builder
   * @return the request params
   */
  static UriBuilder setRequestParams(final Method method, final Object[] args,
      final UriBuilder uriBuilder) {
    UriBuilder builder = uriBuilder;
    for (Map.Entry<String, Object[]> param : getRequestParams(method, args).entrySet()) {
      builder = builder.queryParam(param.getKey(), param.getValue());
    }
    return builder;
  }

  /**
   * Find accept header string.
   *
   * @param method the method
   * @return the string
   */
  static String findAcceptHeader(final Method method) {
    return findRequestMappingValue(
        method, a -> a.produces().length > 0, a -> a.produces()[0])
        .orElseGet(() -> findGetMappingValue(
            method, a -> a.produces().length > 0, a -> a.produces()[0])
            .orElseGet(() -> findPostMappingValue(
                method, a -> a.produces().length > 0, a -> a.produces()[0])
                .orElseGet(() -> findPutMappingValue(
                    method, a -> a.produces().length > 0, a -> a.produces()[0])
                    .orElseGet(() -> findPatchMappingValue(
                        method, a -> a.produces().length > 0, a -> a.produces()[0])
                        .orElseGet(() -> findDeleteMappingValue(
                            method, a -> a.produces().length > 0, a -> a.produces()[0])
                            .orElse(null))))));
  }

  /**
   * Find content type header string.
   *
   * @param method the method
   * @return the string
   */
  static String findContentTypeHeader(final Method method) {
    return findRequestMappingValue(
        method, a -> a.consumes().length > 0, a -> a.consumes()[0])
        .orElseGet(() -> findGetMappingValue(
            method, a -> a.consumes().length > 0, a -> a.consumes()[0])
            .orElseGet(() -> findPostMappingValue(
                method, a -> a.consumes().length > 0, a -> a.consumes()[0])
                .orElseGet(() -> findPutMappingValue(
                    method, a -> a.consumes().length > 0, a -> a.consumes()[0])
                    .orElseGet(() -> findPatchMappingValue(
                        method, a -> a.consumes().length > 0, a -> a.consumes()[0])
                        .orElseGet(() -> findDeleteMappingValue(
                            method, a -> a.consumes().length > 0, a -> a.consumes()[0])
                            .orElse(null))))));
  }

  /**
   * Find request mapping value optional.
   *
   * @param <T>       the type parameter
   * @param obj       the obj
   * @param condition the condition
   * @param selector  the selector
   * @return the optional
   */
  static <T> Optional<T> findRequestMappingValue(
      Object obj, // can be method or target class
      Predicate<RequestMapping> condition,
      Function<RequestMapping, T> selector) {

    if (obj instanceof Method) {
      final Method method = (Method) obj;
      return Optional.of(method)
          .map(m -> AnnotationUtils.findAnnotation(m, RequestMapping.class))
          .filter(condition)
          .map(selector);
    } else {
      final Class<?> cls = obj instanceof Class ? (Class<?>) obj : obj.getClass();
      return Optional.of(cls)
          .map(m -> AnnotationUtils.findAnnotation(m, RequestMapping.class))
          .filter(condition)
          .map(selector);
    }
  }

  private static <T> Optional<T> findGetMappingValue(Method method, Predicate<GetMapping> condition,
      Function<GetMapping, T> selector) {
    return Optional.of(method)
        .map(m -> AnnotationUtils.findAnnotation(m, GetMapping.class))
        .filter(condition)
        .map(selector);
  }

  private static <T> Optional<T> findPostMappingValue(Method method,
      Predicate<PostMapping> condition,
      Function<PostMapping, T> selector) {
    return Optional.of(method)
        .map(m -> AnnotationUtils.findAnnotation(m, PostMapping.class))
        .filter(condition)
        .map(selector);
  }

  private static <T> Optional<T> findPutMappingValue(Method method, Predicate<PutMapping> condition,
      Function<PutMapping, T> selector) {
    return Optional.of(method)
        .map(m -> AnnotationUtils.findAnnotation(m, PutMapping.class))
        .filter(condition)
        .map(selector);
  }

  private static <T> Optional<T> findPatchMappingValue(Method method,
      Predicate<PatchMapping> condition,
      Function<PatchMapping, T> selector) {
    return Optional.of(method)
        .map(m -> AnnotationUtils.findAnnotation(m, PatchMapping.class))
        .filter(condition)
        .map(selector);
  }

  private static <T> Optional<T> findDeleteMappingValue(Method method,
      Predicate<DeleteMapping> condition,
      Function<DeleteMapping, T> selector) {
    return Optional.of(method)
        .map(m -> AnnotationUtils.findAnnotation(m, DeleteMapping.class))
        .filter(condition)
        .map(selector);
  }

  /**
   * Put to multi value map.
   *
   * @param name          the name
   * @param value         the value
   * @param multiValueMap the multi value map
   */
  static void putToMultiValueMap(
      final String name,
      final Object value,
      final MultiValueMap<String, String> multiValueMap) {

    if (value instanceof Map) {
      final Map<?, ?> map = (Map) value;
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        final String key = String.valueOf(entry.getKey());
        final Object mapValue = entry.getValue();
        if (mapValue instanceof Collection) {
          putCollectionToValueMap(key, (Collection) mapValue, multiValueMap);
        } else if (mapValue != null) {
          multiValueMap.set(key, String.valueOf(mapValue));
        }
      }
    } else if (value instanceof Collection) {
      putCollectionToValueMap(name, (Collection) value, multiValueMap);
    } else if (value != null) {
      multiValueMap.set(name, String.valueOf(value));
    }
  }

  private static void putCollectionToValueMap(
      final String key,
      final Collection<?> collection,
      final MultiValueMap<String, String> multiValueMap) {
    final List<String> list = collection.stream()
        .filter(Objects::nonNull)
        .map(Object::toString)
        .collect(Collectors.toList());
    multiValueMap.put(key, list);
  }

}