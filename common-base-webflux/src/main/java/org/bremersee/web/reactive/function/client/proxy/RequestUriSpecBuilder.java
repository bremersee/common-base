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

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.lang.reflect.Method;
import org.bremersee.exception.ServiceException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;

/**
 * The request uri spec builder.
 *
 * @author Christian Bremer
 */
public interface RequestUriSpecBuilder {

  /**
   * Build request headers uri spec.
   *
   * @param parameters the parameters
   * @param webClient  the web client
   * @return the request headers uri spec
   */
  RequestHeadersUriSpec<?> build(InvocationParameters parameters, WebClient webClient);

  /**
   * Default request uri spec builder.
   *
   * @return the request uri spec builder
   */
  static RequestUriSpecBuilder defaultBuilder() {
    return new Default();
  }

  /**
   * The default request uri spec builder.
   */
  class Default implements RequestUriSpecBuilder {

    @Override
    public RequestHeadersUriSpec<?> build(
        final InvocationParameters parameters,
        final WebClient webClient) {

      final Method method = parameters.getMethod();
      final RequestMapping requestMapping = findAnnotation(method, RequestMapping.class);
      if (requestMapping != null && requestMapping.method().length > 0) {
        switch (requestMapping.method()[0]) {
          case GET:
            return webClient.get();
          case HEAD:
            return webClient.head();
          case POST:
            return webClient.post();
          case PUT:
            return webClient.put();
          case PATCH:
            return webClient.patch();
          case DELETE:
            return webClient.delete();
          case OPTIONS:
            return webClient.options();
          default:
        }
      }
      if (findAnnotation(method, GetMapping.class) != null) {
        return webClient.get();
      }
      if (findAnnotation(method, PostMapping.class) != null) {
        return webClient.post();
      }
      if (findAnnotation(method, PutMapping.class) != null) {
        return webClient.put();
      }
      if (findAnnotation(method, PatchMapping.class) != null) {
        return webClient.patch();
      }
      if (findAnnotation(method, DeleteMapping.class) != null) {
        return webClient.delete();
      }
      throw ServiceException.internalServerError("Cannot find request method on method '"
              + method.getName() + "'.",
          "org.bremersee:common-base-webflux:5222f2b4-1810-41bf-acfc-37988571304b");
    }
  }

}
