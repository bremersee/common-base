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

import static org.bremersee.web.reactive.function.client.proxy.InvocationUtils.findDeleteMappingValue;
import static org.bremersee.web.reactive.function.client.proxy.InvocationUtils.findGetMappingValue;
import static org.bremersee.web.reactive.function.client.proxy.InvocationUtils.findPatchMappingValue;
import static org.bremersee.web.reactive.function.client.proxy.InvocationUtils.findPostMappingValue;
import static org.bremersee.web.reactive.function.client.proxy.InvocationUtils.findPutMappingValue;
import static org.bremersee.web.reactive.function.client.proxy.InvocationUtils.findRequestMappingValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriBuilder;

/**
 * @author Christian Bremer
 */
public interface RequestUriBuilder {

  URI build(InvocationParameters parameters, UriBuilder uriBuilder);

  static RequestUriBuilder defaultBuilder() {
    return new Default();
  }

  class Default implements RequestUriBuilder {

    @Override
    public URI build(final InvocationParameters parameters, final UriBuilder uriBuilder) {
      final Class<?> targetClass = parameters.getTargetClass();
      final Method method = parameters.getMethod();
      final Object[] args = parameters.getArgs();
      final String uriTemplate = getCommonPath(targetClass) + getPath(method);
      return addQueries(uriBuilder.path(uriTemplate), method, args)
          .build(getPathVariables(method, args));
    }

    private String getCommonPath(final Class<?> targetClass) {
      return findRequestMappingValue(
          targetClass,
          mapping -> mapping.path().length > 0,
          mapping -> mapping.path()[0])
          .orElse("");
    }

    private String getPath(final Method method) {
      String value = findRequestMappingValue(
          method,
          mapping -> mapping.path().length > 0,
          mapping -> mapping.path()[0])
          .orElse(null);
      if (StringUtils.hasText(value)) {
        return value;
      }
      value = findGetMappingValue(
          method,
          mapping -> mapping.path().length > 0,
          mapping -> mapping.path()[0])
          .orElse(null);
      if (StringUtils.hasText(value)) {
        return value;
      }
      value = findPostMappingValue(
          method,
          mapping -> mapping.path().length > 0,
          mapping -> mapping.path()[0])
          .orElse(null);
      if (StringUtils.hasText(value)) {
        return value;
      }
      value = findPutMappingValue(
          method,
          mapping -> mapping.path().length > 0,
          mapping -> mapping.path()[0])
          .orElse(null);
      if (StringUtils.hasText(value)) {
        return value;
      }
      value = findDeleteMappingValue(
          method,
          mapping -> mapping.path().length > 0,
          mapping -> mapping.path()[0])
          .orElse(null);
      if (StringUtils.hasText(value)) {
        return value;
      }
      return findPatchMappingValue(
          method,
          mapping -> mapping.path().length > 0,
          mapping -> mapping.path()[0])
          .orElse("");
    }

    private UriBuilder addQueries(
        final UriBuilder uriBuilder,
        final Method method,
        final Object[] args) {
      final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
      for (int i = 0; i < parameterAnnotations.length; i++) {
        for (final Annotation annotation : parameterAnnotations[i]) {
          if (annotation instanceof RequestParam) {
            final RequestParam param = (RequestParam) annotation;
            final String name = param.name();
            final Object value = args[i];
            if (value instanceof Map) {
              final Map<?, ?> map = (Map) value;
              for (Map.Entry<?, ?> entry : map.entrySet()) {
                final String key = String.valueOf(entry.getKey());
                final Object mapValue = entry.getValue();
                if (mapValue instanceof Collection) {
                  uriBuilder.queryParam(key, ((Collection<?>) mapValue).toArray(new Object[0]));
                } else {
                  uriBuilder.queryParam(name, mapValue);
                }
              }
            } else if (value instanceof Collection) {
              uriBuilder.queryParam(name, ((Collection<?>) value).toArray(new Object[0]));
            } else {
              uriBuilder.queryParam(name, value);
            }
          }
        }
      }

      return uriBuilder;
    }

    private Map<String, ?> getPathVariables(final Method method, final Object[] args) {
      final Map<String, Object> values = new HashMap<>();
      final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
      for (int i = 0; i < parameterAnnotations.length; i++) {
        for (final Annotation annotation : parameterAnnotations[i]) {
          if (annotation instanceof PathVariable) {
            final PathVariable param = (PathVariable) annotation;
            final String name = param.name();
            final Object value = args[i];
            values.put(name, value);
          }
        }
      }
      return values;
    }

  }

}