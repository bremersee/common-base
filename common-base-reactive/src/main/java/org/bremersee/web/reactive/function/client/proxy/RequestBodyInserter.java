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
import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;

/**
 * @author Christian Bremer
 */
public interface RequestBodyInserter {

  void insert(InvocationParameters parameters, RequestBodyUriSpec uriSpec);

  static RequestBodyInserter defaultInserter() {
    return new Default();
  }

  class Default implements RequestBodyInserter {

    @Override
    public void insert(final InvocationParameters parameters, final RequestBodyUriSpec uriSpec) {
      final Method method = parameters.getMethod();
      final Object[] args = parameters.getArgs();
      final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
      for (int i = 0; i < parameterAnnotations.length; i++) {
        for (final Annotation annotation : parameterAnnotations[i]) {
          if (annotation instanceof RequestBody) {
            final Object value = args[i];
            if (isMediaType(value, method, MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
              //noinspection unchecked
              uriSpec.body(BodyInserters.fromFormData((MultiValueMap) value));
            } else if (isMediaType(value, method, MediaType.MULTIPART_FORM_DATA_VALUE)) {
              //noinspection unchecked
              uriSpec.body(BodyInserters.fromMultipartData((MultiValueMap) value));
            } else if (value != null) {
              uriSpec.body(BodyInserters.fromObject(args[i]));
            }
          }
        }
      }
    }

    private boolean isMediaType(final Object value, final Method method, final String mediaType) {
      return Optional.ofNullable(value)
          .filter(v -> v instanceof MultiValueMap)
          .flatMap(v -> InvocationUtils.findRequestMappingValue(
              method,
              mapping -> isMediaType(mapping.consumes(), mediaType),
              mapping -> true))
          .orElse(false);
    }

    private boolean isMediaType(final String[] consumes, final String mediaType) {
      return Arrays.stream(consumes)
          .anyMatch(value -> value.toLowerCase().contains(mediaType.toLowerCase()));
    }

  }

}
