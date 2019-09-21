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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.util.UriBuilder;

/**
 * @author Christian Bremer
 */
class WebClientInvocationHandler implements InvocationHandler {

  private final Map<MethodDescription, InvocationFunctions> methodFunctions;

  private InvocationFunctions commonFunctions;

  private final WebClient webClient;

  private final UriBuilder uriBuilder;

  WebClientInvocationHandler(
      final Map<MethodDescription, InvocationFunctions> methodFunctions,
      final InvocationFunctions commonFunctions,
      final WebClient webClient,
      final UriBuilder uriBuilder) {
    this.methodFunctions = methodFunctions;
    this.commonFunctions = commonFunctions;
    this.webClient = webClient;
    this.uriBuilder = uriBuilder;
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) {
    final InvocationParameters parameters = new InvocationParameters(proxy, method, args);
    final InvocationFunctions functions = InvocationFunctions.merge(
        commonFunctions,
        methodFunctions.get(new MethodDescription(method)));
    final RequestHeadersUriSpec<?> uriSpec = functions.getUriSpecBuilder()
        .build(parameters, webClient);
    uriSpec
        .uri(functions.getUriBuilder().build(parameters, uriBuilder))
        .headers(
            httpHeaders -> functions.getHeadersBuilder().build(parameters, httpHeaders))
        .cookies(cookies -> functions.getCookiesBuilder().build(parameters, cookies));
    if (uriSpec instanceof RequestBodyUriSpec) {
      functions.getBodyInserter().insert(parameters, (RequestBodyUriSpec) uriSpec);
    }
    final ResponseSpec responseSpec = uriSpec.retrieve();
    responseSpec.onStatus(functions.getErrorDetector(), functions.getErrorDecoder());
    return functions.getResponseBuilder();
  }
}
