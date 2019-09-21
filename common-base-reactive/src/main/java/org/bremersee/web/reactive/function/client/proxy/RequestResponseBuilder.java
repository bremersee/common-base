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

import static org.springframework.core.GenericTypeResolver.resolveReturnTypeArgument;

import java.lang.reflect.Method;
import org.bremersee.exception.ServiceException;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The request response builder.
 *
 * @author Christian Bremer
 */
public interface RequestResponseBuilder {

  /**
   * Build object.
   *
   * @param parameters   the parameters
   * @param responseSpec the response spec
   * @return the object
   */
  Object build(final InvocationParameters parameters, final ResponseSpec responseSpec);

  /**
   * Default request response builder.
   *
   * @return the request response builder
   */
  static RequestResponseBuilder defaultBuilder() {
    return new Default();
  }

  /**
   * The default request response builder.
   */
  class Default implements RequestResponseBuilder {

    @Override
    public Object build(InvocationParameters parameters, ResponseSpec responseSpec) {
      final Method method = parameters.getMethod();
      final Class<?> responseClass = method.getReturnType();
      if (Mono.class.isAssignableFrom(responseClass)) {
        final Class<?> typeClass = resolveReturnTypeArgument(method, Mono.class);
        return responseSpec.bodyToMono(typeClass);
      }
      if (Flux.class.isAssignableFrom(responseClass)) {
        final Class<?> typeClass = resolveReturnTypeArgument(method, Flux.class);
        return responseSpec.bodyToFlux(typeClass);
      }
      throw ServiceException.internalServerError("Response class must be Mono or Flux.");
    }
  }

}
