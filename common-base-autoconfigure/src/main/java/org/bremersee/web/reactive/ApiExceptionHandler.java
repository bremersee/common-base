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

package org.bremersee.web.reactive;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.RestApiExceptionUtils;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.http.MediaTypeHelper;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.lang.NonNull;
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
 * The reactive api exception handler.
 *
 * @author Christian Bremer
 */
@Validated
@Slf4j
public class ApiExceptionHandler extends AbstractErrorWebExceptionHandler {

  @Getter(AccessLevel.PROTECTED)
  @Setter
  @NotNull
  private PathMatcher pathMatcher = new AntPathMatcher();

  @Getter(AccessLevel.PROTECTED)
  @NotNull
  private final RestApiExceptionMapper restApiExceptionMapper;

  /**
   * Instantiates a new api exception handler.
   *
   * @param errorAttributes the error attributes
   * @param resources the resources
   * @param applicationContext the application context
   * @param serverCodecConfigurer the server codec configurer
   * @param restApiExceptionMapper the rest api exception mapper
   */
  public ApiExceptionHandler(
      @NotNull final ErrorAttributes errorAttributes,
      @NotNull final WebProperties.Resources resources,
      @NotNull final ApplicationContext applicationContext,
      @Nullable final ServerCodecConfigurer serverCodecConfigurer,
      @NotNull final RestApiExceptionMapper restApiExceptionMapper) {

    super(errorAttributes, resources, applicationContext);
    if (serverCodecConfigurer != null) {
      setMessageReaders(serverCodecConfigurer.getReaders());
      setMessageWriters(serverCodecConfigurer.getWriters());
    }
    this.restApiExceptionMapper = restApiExceptionMapper;
  }

  @Override
  protected RouterFunction<ServerResponse> getRoutingFunction(
      final ErrorAttributes errorAttributes) {

    return RouterFunctions.route(this::isResponsibleExceptionHandler, this::renderErrorResponse);
  }

  /**
   * Is this exception handler responsible.
   *
   * @param request the request
   * @return {@code true} if it is responsible, otherwise {@code false}
   */
  protected boolean isResponsibleExceptionHandler(final ServerRequest request) {
    return getRestApiExceptionMapper().getApiPaths().stream().anyMatch(
        path -> getPathMatcher().match(path, request.path()));
  }

  /**
   * Render error response.
   *
   * @param request the request
   * @return the server response
   */
  @NonNull
  protected Mono<ServerResponse> renderErrorResponse(final ServerRequest request) {

    final RestApiException response = getRestApiExceptionMapper()
        .build(getError(request), request.path(), null);
    final String accepts = MediaTypeHelper.toString(request.headers().accept());
    if (MediaTypeHelper.canContentTypeBeJson(accepts)) {
      return ServerResponse
          .status(getRestApiExceptionMapper().detectHttpStatus(getError(request), null))
          .contentType(MediaType.APPLICATION_JSON)
          .body(BodyInserters.fromValue(response));
    } else if (MediaTypeHelper.canContentTypeBeXml(accepts)) {
      return ServerResponse
          .status(getRestApiExceptionMapper().detectHttpStatus(getError(request), null))
          .contentType(MediaType.APPLICATION_XML)
          .body(BodyInserters.fromValue(response));
    } else {
      final String id = StringUtils.hasText(response.getId())
          ? response.getId()
          : RestApiExceptionUtils.NO_ID_VALUE;
      final String timestamp = response.getTimestamp() != null
          ? response.getTimestamp().format(RestApiExceptionUtils.TIMESTAMP_FORMATTER)
          : OffsetDateTime.now(ZoneOffset.UTC).format(RestApiExceptionUtils.TIMESTAMP_FORMATTER);
      final String msg = StringUtils.hasText(response.getMessage())
          ? response.getMessage()
          : RestApiExceptionUtils.NO_MESSAGE_VALUE;
      final String code = StringUtils.hasText(response.getErrorCode())
          ? response.getErrorCode()
          : RestApiExceptionUtils.NO_ERROR_CODE_VALUE;
      final String cls = StringUtils.hasText(response.getClassName())
          ? response.getClassName()
          : RestApiExceptionUtils.NO_CLASS_VALUE;
      return ServerResponse
          .status(getRestApiExceptionMapper().detectHttpStatus(getError(request), null))
          .header(RestApiExceptionUtils.ID_HEADER_NAME, id)
          .header(RestApiExceptionUtils.TIMESTAMP_HEADER_NAME, timestamp)
          .header(RestApiExceptionUtils.MESSAGE_HEADER_NAME, msg)
          .header(RestApiExceptionUtils.CODE_HEADER_NAME, code)
          .header(RestApiExceptionUtils.CLASS_HEADER_NAME, cls)
          .contentType(Objects.requireNonNull(MediaTypeHelper.findContentType(
              request.headers().accept(), MediaType.TEXT_PLAIN)))
          .body(BodyInserters.empty());
    }
  }

}
