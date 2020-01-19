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
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;

/**
 * The request cookies builder.
 *
 * @author Christian Bremer
 */
public interface RequestCookiesBuilder {

  /**
   * Build.
   *
   * @param parameters the parameters
   * @param cookies the cookies
   */
  void build(InvocationParameters parameters, MultiValueMap<String, String> cookies);

  /**
   * Default request cookies builder.
   *
   * @return the request cookies builder
   */
  static RequestCookiesBuilder defaultBuilder() {
    return new Default();
  }

  /**
   * The default request cookies builder.
   */
  class Default implements RequestCookiesBuilder {

    @Override
    public void build(
        final InvocationParameters parameters,
        final MultiValueMap<String, String> cookies) {

      final Method method = parameters.getMethod();
      final Object[] args = parameters.getArgs();
      final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
      for (int i = 0; i < parameterAnnotations.length; i++) {
        for (final Annotation annotation : parameterAnnotations[i]) {
          if (annotation instanceof CookieValue) {
            final CookieValue param = (CookieValue) annotation;
            final String name = StringUtils.hasText(param.value()) ? param.value() : param.name();
            final Object value = args[i];
            putToMultiValueMap(name, value, cookies);
          }
        }
      }
    }
  }
}
