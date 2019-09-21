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

import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bremersee.web.ErrorDetectors;
import org.bremersee.web.reactive.function.client.DefaultWebClientErrorDecoder;
import org.bremersee.web.reactive.function.client.WebClientErrorDecoder;
import org.springframework.http.HttpStatus;

/**
 * @author Christian Bremer
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@SuppressWarnings("WeakerAccess")
public class InvocationFunctions {

  private RequestUriSpecBuilder uriSpecBuilder;

  private RequestUriBuilder uriBuilder;

  private RequestHeadersBuilder headersBuilder;

  private RequestCookiesBuilder cookiesBuilder;

  private RequestBodyInserter bodyInserter;

  private Predicate<HttpStatus> errorDetector;

  private WebClientErrorDecoder<? extends Throwable> errorDecoder;

  private RequestResponseBuilder responseBuilder;

  private InvocationFunctions() {
    this(RequestUriSpecBuilder.defaultBuilder(),
        RequestUriBuilder.defaultBuilder(),
        RequestHeadersBuilder.defaultBuilder(),
        RequestCookiesBuilder.defaultBuilder(),
        RequestBodyInserter.defaultInserter(),
        ErrorDetectors.DEFAULT,
        new DefaultWebClientErrorDecoder(),
        RequestResponseBuilder.defaultBuilder());
  }

  @Builder
  public InvocationFunctions(
      final RequestUriSpecBuilder uriSpecBuilder,
      final RequestUriBuilder uriBuilder,
      final RequestHeadersBuilder headersBuilder,
      final RequestCookiesBuilder cookiesBuilder,
      final RequestBodyInserter bodyInserter,
      final Predicate<HttpStatus> errorDetector,
      final WebClientErrorDecoder<? extends Throwable> errorDecoder,
      final RequestResponseBuilder responseBuilder) {
    this.uriSpecBuilder = uriSpecBuilder;
    this.uriBuilder = uriBuilder;
    this.headersBuilder = headersBuilder;
    this.cookiesBuilder = cookiesBuilder;
    this.bodyInserter = bodyInserter;
    this.errorDetector = errorDetector;
    this.errorDecoder = errorDecoder;
    this.responseBuilder = responseBuilder;
  }

  static InvocationFunctions merge(
      final InvocationFunctions commonFunctions,
      final InvocationFunctions methodFunctions) {

    final InvocationFunctions functions = new InvocationFunctions();
    mergeInto(commonFunctions, functions);
    mergeInto(methodFunctions, functions);
    return functions;
  }

  private static void mergeInto(
      final InvocationFunctions source,
      final InvocationFunctions destination) {

    if (source != null) {
      if (source.getUriSpecBuilder() != null) {
        destination.setUriSpecBuilder(source.getUriSpecBuilder());
      }
      if (source.getUriBuilder() != null) {
        destination.setUriBuilder(source.getUriBuilder());
      }
      if (source.getHeadersBuilder() != null) {
        destination.setHeadersBuilder(source.getHeadersBuilder());
      }
      if (source.getCookiesBuilder() != null) {
        destination.setCookiesBuilder(source.getCookiesBuilder());
      }
      if (source.getBodyInserter() != null) {
        destination.setBodyInserter(source.getBodyInserter());
      }
      if (source.getErrorDecoder() != null) {
        destination.setErrorDecoder(source.getErrorDecoder());
      }
      if (source.getErrorDetector() != null) {
        destination.setErrorDetector(source.getErrorDetector());
      }
      if (source.getResponseBuilder() != null) {
        destination.setResponseBuilder(source.getResponseBuilder());
      }
    }
  }

}
