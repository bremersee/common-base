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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Christian Bremer
 */
abstract class InvocationUtils {

  private InvocationUtils() {
  }

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

  static <T> Optional<T> findGetMappingValue(Method method, Predicate<GetMapping> condition,
      Function<GetMapping, T> selector) {
    return Optional.of(method)
        .map(m -> AnnotationUtils.findAnnotation(m, GetMapping.class))
        .filter(condition)
        .map(selector);
  }

  static <T> Optional<T> findPostMappingValue(Method method, Predicate<PostMapping> condition,
      Function<PostMapping, T> selector) {
    return Optional.of(method)
        .map(m -> AnnotationUtils.findAnnotation(m, PostMapping.class))
        .filter(condition)
        .map(selector);
  }

  static <T> Optional<T> findPutMappingValue(Method method, Predicate<PutMapping> condition,
      Function<PutMapping, T> selector) {
    return Optional.of(method)
        .map(m -> AnnotationUtils.findAnnotation(m, PutMapping.class))
        .filter(condition)
        .map(selector);
  }

  static <T> Optional<T> findPatchMappingValue(Method method, Predicate<PatchMapping> condition,
      Function<PatchMapping, T> selector) {
    return Optional.of(method)
        .map(m -> AnnotationUtils.findAnnotation(m, PatchMapping.class))
        .filter(condition)
        .map(selector);
  }

  static <T> Optional<T> findDeleteMappingValue(Method method, Predicate<DeleteMapping> condition,
      Function<DeleteMapping, T> selector) {
    return Optional.of(method)
        .map(m -> AnnotationUtils.findAnnotation(m, DeleteMapping.class))
        .filter(condition)
        .map(selector);
  }

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