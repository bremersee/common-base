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

import static org.bremersee.web.reactive.function.client.proxy.InvocationUtils.getPathVariables;
import static org.bremersee.web.reactive.function.client.proxy.InvocationUtils.getRequestPath;
import static org.bremersee.web.reactive.function.client.proxy.InvocationUtils.setRequestParams;

import java.lang.reflect.Method;
import java.net.URI;
import org.springframework.web.util.UriBuilder;

/**
 * The request uri builder.
 *
 * @author Christian Bremer
 */
public interface RequestUriBuilder {

  /**
   * Build uri.
   *
   * @param parameters the parameters
   * @param uriBuilder the uri builder
   * @return the uri
   */
  URI build(InvocationParameters parameters, UriBuilder uriBuilder);

  /**
   * Default request uri builder.
   *
   * @return the request uri builder
   */
  static RequestUriBuilder defaultBuilder() {
    return new Default();
  }

  /**
   * The default request response builder.
   */
  class Default implements RequestUriBuilder {

    @Override
    public URI build(final InvocationParameters parameters, final UriBuilder uriBuilder) {
      final Class<?> targetClass = parameters.getTargetClass();
      final Method method = parameters.getMethod();
      final Object[] args = parameters.getArgs();

      UriBuilder builder = uriBuilder;
      builder = builder.path(getRequestPath(targetClass, method));
      builder = setRequestParams(method, args, builder);
      return builder.build(getPathVariables(method, args));
    }
  }

}