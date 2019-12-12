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
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;

/**
 * The request body inserter.
 *
 * @author Christian Bremer
 */
public interface RequestBodyInserter {

  /**
   * Insert.
   *
   * @param parameters the parameters
   * @param uriSpec    the uri spec
   */
  void insert(InvocationParameters parameters, RequestBodyUriSpec uriSpec);

  /**
   * Default request body inserter.
   *
   * @return the request body inserter
   */
  static RequestBodyInserter defaultInserter() {
    return new Default();
  }

  /**
   * The default request body inserter.
   */
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
            if (matches(value, MultiValueMap.class, method,
                MediaType.APPLICATION_FORM_URLENCODED)) {
              //noinspection unchecked
              uriSpec.body(BodyInserters.fromFormData((MultiValueMap) value));
            } else if (matches(value, MultiValueMap.class, method, MediaType.MULTIPART_FORM_DATA)) {
              //noinspection unchecked
              uriSpec.body(BodyInserters.fromMultipartData((MultiValueMap) value));
            } else if (value != null) {
              uriSpec.body(BodyInserters.fromValue(args[i]));
            }
            return;
          }
        }
      }
    }

    private boolean matches(
        final Object value,
        @SuppressWarnings("SameParameterValue") final Class<?> valueClass,
        final Method method,
        final MediaType mediaType) {
      return value != null && valueClass.isAssignableFrom(value.getClass()) && matches(method,
          mediaType);
    }

    private boolean matches(final Method method, final MediaType mediaType) {
      return InvocationUtils.findContentTypeHeader(method).stream()
          .anyMatch(mediaType::isCompatibleWith);
    }

  }

}
