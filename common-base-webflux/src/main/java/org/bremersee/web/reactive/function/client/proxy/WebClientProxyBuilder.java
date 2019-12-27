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
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * The web client proxy builder.
 *
 * @author Christian Bremer
 */
@Validated
public interface WebClientProxyBuilder {

  /**
   * Sets web client.
   *
   * @param webClient the web client
   * @return the web client proxy builder
   */
  WebClientProxyBuilder webClient(WebClient webClient);

  /**
   * Sets common functions.
   *
   * @param functions the functions
   * @return the web client proxy builder
   */
  @SuppressWarnings("unused")
  WebClientProxyBuilder commonFunctions(InvocationFunctions functions);

  /**
   * Sets method functions.
   *
   * @param method    the method
   * @param functions the functions
   * @return the web client proxy builder
   */
  @SuppressWarnings("unused")
  WebClientProxyBuilder methodFunctions(Method method, InvocationFunctions functions);

  /**
   * Build the proxy.
   *
   * @param <T>    the type of the target
   * @param target the target
   * @return the proxy
   */
  <T> T build(@NotNull Class<T> target);

  /**
   * Default web client proxy builder.
   *
   * @return the web client proxy builder
   */
  static WebClientProxyBuilder defaultBuilder() {
    return new Default();
  }

  /**
   * The default web client proxy builder.
   */
  class Default implements WebClientProxyBuilder {

    private final Map<MethodDescription, InvocationFunctions> methodFunctions = new HashMap<>();

    private InvocationFunctions commonFunctions;

    private WebClient webClient;

    @Override
    public WebClientProxyBuilder webClient(final WebClient webClient) {
      this.webClient = webClient;
      return this;
    }

    @Override
    public WebClientProxyBuilder methodFunctions(
        final Method method,
        final InvocationFunctions functions) {

      if (method != null && functions != null) {
        methodFunctions.put(new MethodDescription(method), functions);
      }
      return this;
    }

    @Override
    public WebClientProxyBuilder commonFunctions(final InvocationFunctions functions) {
      this.commonFunctions = functions;
      return this;
    }

    @Override
    public <T> T build(final Class<T> target) {

      final InvocationHandler handler = new WebClientInvocationHandler(
          Collections.unmodifiableMap(methodFunctions),
          commonFunctions,
          webClient != null ? webClient : WebClient.builder().build(),
          target);
      //noinspection unchecked
      return (T) Proxy.newProxyInstance(target.getClassLoader(), new Class<?>[]{target}, handler);
    }

  }

}