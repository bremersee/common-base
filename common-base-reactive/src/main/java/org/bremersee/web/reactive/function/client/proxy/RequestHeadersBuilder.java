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

import static org.bremersee.web.reactive.function.client.proxy.InvocationUtils.putToMultiValueMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * The request headers builder.
 *
 * @author Christian Bremer
 */
public interface RequestHeadersBuilder {

  /**
   * Sets headers.
   *
   * @param parameters the parameters
   * @param headers    the headers
   */
  void setHeaders(InvocationParameters parameters, HttpHeaders headers);

  /**
   * Build.
   *
   * @param parameters the parameters
   * @param headers    the headers
   */
  default void build(InvocationParameters parameters, HttpHeaders headers) {
    setAcceptHeader(parameters, headers);
    setContentTypeHeader(parameters, headers);
    setHeaders(parameters, headers);
  }

  /**
   * Sets accept header.
   *
   * @param parameters the parameters
   * @param headers    the headers
   */
  default void setAcceptHeader(
      final InvocationParameters parameters,
      final HttpHeaders headers) {

    final Method method = parameters.getMethod();
    final String value = InvocationUtils.findAcceptHeader(method);
    if (StringUtils.hasText(value)) {
      headers.set(HttpHeaders.ACCEPT, value);
    }
  }

  /**
   * Sets content type header.
   *
   * @param parameters the parameters
   * @param headers    the headers
   */
  default void setContentTypeHeader(
      final InvocationParameters parameters,
      final HttpHeaders headers) {

    final Method method = parameters.getMethod();
    final String value = InvocationUtils.findContentTypeHeader(method);
    if (StringUtils.hasText(value)) {
      headers.set(HttpHeaders.CONTENT_TYPE, value);
    }
  }

  /**
   * Default request headers builder.
   *
   * @return the request headers builder
   */
  static RequestHeadersBuilder defaultBuilder() {
    return new Default();
  }

  /**
   * The default request headers builder.
   */
  class Default implements RequestHeadersBuilder {

    @Override
    public void setHeaders(final InvocationParameters parameters, final HttpHeaders headers) {
      final Method method = parameters.getMethod();
      final Object[] args = parameters.getArgs();
      final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
      for (int i = 0; i < parameterAnnotations.length; i++) {
        for (final Annotation annotation : parameterAnnotations[i]) {
          if (annotation instanceof RequestHeader) {
            final RequestHeader param = (RequestHeader) annotation;
            final String name = StringUtils.hasText(param.value()) ? param.value() : param.name();
            final Object value = args[i];
            putToMultiValueMap(name, value, headers);
          }
        }
      }
    }
  }

}
