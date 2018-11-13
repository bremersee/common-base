/*
 * Copyright 2017 the original author or authors.
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

package org.bremersee.web.reactive;

import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.web.MediaTypeHelper;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * @author Christian Bremer
 */
@Validated
@Slf4j
public class ApiExceptionHandler extends AbstractErrorWebExceptionHandler {

  @Getter(AccessLevel.PROTECTED)
  @Setter
  private PathMatcher pathMatcher = new AntPathMatcher();

  @Getter(AccessLevel.PROTECTED)
  private final RestApiExceptionMapper restApiExceptionMapper;

  public ApiExceptionHandler(
      @NotNull ErrorAttributes errorAttributes,
      @NotNull ResourceProperties resourceProperties,
      @NotNull ApplicationContext applicationContext,
      @Nullable ServerCodecConfigurer serverCodecConfigurer,
      @NotNull RestApiExceptionMapper restApiExceptionMapper) {

    super(errorAttributes, resourceProperties, applicationContext);
    if (serverCodecConfigurer != null) {
      setMessageReaders(serverCodecConfigurer.getReaders());
      setMessageWriters(serverCodecConfigurer.getWriters());
    }
    this.restApiExceptionMapper = restApiExceptionMapper;
  }

  @Override
  protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
    return RouterFunctions.route(this::isResponsibleExceptionHandler, this::renderErrorResponse);
  }

  @SuppressWarnings("WeakerAccess")
  protected boolean isResponsibleExceptionHandler(ServerRequest request) {
    return restApiExceptionMapper.getApiAntPaths().stream().anyMatch(
        s -> pathMatcher.match(s, request.path()));
  }

  @SuppressWarnings("WeakerAccess")
  protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {

    final RestApiException response = restApiExceptionMapper
        .build(getError(request), request.path(), null);
    final String accepts = MediaTypeHelper.toString(request.headers().accept());
    if (MediaTypeHelper.canContentTypeBeJson(accepts)) {
      return ServerResponse
          .status(restApiExceptionMapper.detectHttpStatus(getError(request), null))
          .contentType(MediaType.APPLICATION_JSON)
          .body(BodyInserters.fromObject(response));
    } else if (MediaTypeHelper.canContentTypeBeXml(accepts)) {
      return ServerResponse
          .status(restApiExceptionMapper.detectHttpStatus(getError(request), null))
          .contentType(MediaType.APPLICATION_XML)
          .body(BodyInserters.fromObject(response));
    } else {
      final String msg = StringUtils.hasText(response.getMessage())
          ? response.getMessage()
          : "No message available";
      final String code = StringUtils.hasText(response.getErrorCode())
          ? response.getErrorCode()
          : "UNSPECIFIED";
      final String cls = StringUtils.hasText(response.getClassName())
          ? response.getClassName()
          : Exception.class.getName();
      return ServerResponse
          .status(restApiExceptionMapper.detectHttpStatus(getError(request), null))
          .header(RestApiExceptionMapper.MESSAGE_HEADER_NAME, msg)
          .header(RestApiExceptionMapper.CODE_HEADER_NAME, code)
          .header(RestApiExceptionMapper.MESSAGE_HEADER_NAME, cls)
          .body(BodyInserters.empty());
    }
  }

}
