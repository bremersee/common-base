/*
 * Copyright 2018 the original author or authors.
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

package org.bremersee.web.servlet;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.RestApiExceptionUtils;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.http.MediaTypeHelper;
import org.bremersee.http.converter.ObjectMapperHelper;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.servlet.view.xml.MappingJackson2XmlView;
import org.springframework.web.util.WebUtils;

/**
 * The api exception resolver.
 *
 * @author Christian Bremer
 */
@Validated
@Slf4j
public class ApiExceptionResolver implements HandlerExceptionResolver {

  /**
   * The constant MODEL_KEY.
   */
  @SuppressWarnings("WeakerAccess")
  protected static final String MODEL_KEY = "error";

  @Getter(AccessLevel.PROTECTED)
  @Setter
  private PathMatcher pathMatcher = new AntPathMatcher();

  @Getter(AccessLevel.PROTECTED)
  private final RestApiExceptionMapper exceptionMapper;

  @Getter(AccessLevel.PROTECTED)
  @Setter
  private Jackson2ObjectMapperBuilder objectMapperBuilder;

  /**
   * Instantiates a new Api exception resolver.
   *
   * @param exceptionMapper the exception mapper
   */
  @SuppressWarnings("WeakerAccess")
  public ApiExceptionResolver(
      final RestApiExceptionMapper exceptionMapper) {
    this.exceptionMapper = exceptionMapper;
  }

  @Override
  public ModelAndView resolveException(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final @Nullable Object handler,
      final Exception ex) {

    if (!isExceptionHandlerResponsible(request, handler)) {
      return null;
    }

    final RestApiException payload = exceptionMapper.build(ex, request.getRequestURI(), handler);

    ModelAndView modelAndView;
    final ResponseFormatAndContentType chooser = new ResponseFormatAndContentType(request);
    switch (chooser.getResponseFormat()) {
      case JSON:
        final MappingJackson2JsonView mjv = objectMapperBuilder == null
            ? new MappingJackson2JsonView(ObjectMapperHelper.getJsonMapper())
            : new MappingJackson2JsonView(objectMapperBuilder.build());
        mjv.setContentType(chooser.getContentType());
        mjv.setPrettyPrint(true);
        mjv.setModelKey(MODEL_KEY);
        mjv.setExtractValueFromSingleKeyModel(true); // removes the MODEL_KEY from the output
        modelAndView = new ModelAndView(mjv, MODEL_KEY, payload);
        break;

      case XML:
        final MappingJackson2XmlView mxv = objectMapperBuilder == null
            ? new MappingJackson2XmlView(ObjectMapperHelper.getXmlMapper())
            : new MappingJackson2XmlView(objectMapperBuilder.createXmlMapper(true).build());
        mxv.setContentType(chooser.getContentType());
        mxv.setPrettyPrint(true);
        mxv.setModelKey(MODEL_KEY);
        modelAndView = new ModelAndView(mxv, MODEL_KEY, payload);
        break;

      default:
        modelAndView = new ModelAndView(new EmptyView(payload, chooser.getContentType()));
    }

    response.setContentType(chooser.getContentType());
    final int statusCode = exceptionMapper.detectHttpStatus(ex, handler).value();
    modelAndView.setStatus(HttpStatus.resolve(statusCode));
    applyStatusCodeIfPossible(request, response, statusCode);
    return modelAndView;
  }

  /**
   * Is this exception handler responsible.
   *
   * @param request the request
   * @param handler the handler
   * @return {@code true} if it is responsible, otherwise {@code false}
   */
  @SuppressWarnings("WeakerAccess")
  protected boolean isExceptionHandlerResponsible(
      final HttpServletRequest request,
      final @Nullable Object handler) {

    if (!exceptionMapper.getApiPaths().isEmpty()) {
      return exceptionMapper.getApiPaths().stream().anyMatch(
          s -> pathMatcher.match(s, request.getServletPath()));
    }

    if (handler == null) {
      return false;
    }
    final Class<?> cls = handler instanceof HandlerMethod
        ? ((HandlerMethod) handler).getBean().getClass()
        : handler.getClass();
    final boolean result = AnnotationUtils.findAnnotation(cls, RestController.class) != null;
    if (log.isDebugEnabled()) {
      log.debug("Is handler [" + handler + "] a rest controller? " + result);
    }
    return result;
  }

  /**
   * Apply status code if possible.
   *
   * @param request    the request
   * @param response   the response
   * @param statusCode the status code
   */
  @SuppressWarnings("WeakerAccess")
  protected final void applyStatusCodeIfPossible(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final int statusCode) {

    if (!WebUtils.isIncludeRequest(request)) {
      if (log.isDebugEnabled()) {
        log.debug("Applying HTTP status code " + statusCode);
      }
      response.setStatus(statusCode);
      request.setAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE, statusCode);
    }
  }

  /**
   * The response format.
   */
  enum ResponseFormat {

    /**
     * Json response format.
     */
    JSON,

    /**
     * Xml response format.
     */
    XML,

    /**
     * Empty response format.
     */
    EMPTY
  }

  /**
   * The response format and content type.
   */
  static class ResponseFormatAndContentType {

    @Getter(AccessLevel.PROTECTED)
    private ResponseFormat responseFormat;

    @Getter(AccessLevel.PROTECTED)
    private String contentType;

    /**
     * Instantiates a new response format and content type.
     *
     * @param request the request
     */
    ResponseFormatAndContentType(final @NotNull HttpServletRequest request) {
      final String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
      if (MediaTypeHelper.canContentTypeBeJson(acceptHeader)) {
        responseFormat = ResponseFormat.JSON;
        contentType = MediaType.APPLICATION_JSON_VALUE;
      } else if (MediaTypeHelper.canContentTypeBeXml(acceptHeader)) {
        responseFormat = ResponseFormat.XML;
        contentType = MediaType.APPLICATION_XML_VALUE;
      } else {
        responseFormat = ResponseFormat.EMPTY;
        if (StringUtils.hasText(acceptHeader)) {
          final List<MediaType> accepts = MediaType.parseMediaTypes(acceptHeader);
          contentType = String
              .valueOf(MediaTypeHelper.findContentType(accepts, MediaType.TEXT_PLAIN));
        } else {
          contentType = MediaType.TEXT_PLAIN_VALUE;
        }
      }
    }
  }

  /**
   * The empty view.
   */
  static class EmptyView extends AbstractView {

    /**
     * The rest api exception.
     */
    final RestApiException restApiException;

    /**
     * Instantiates a new empty view.
     *
     * @param payload     the payload
     * @param contentType the content type
     */
    EmptyView(final @NotNull RestApiException payload, final String contentType) {
      this.restApiException = payload;
      setContentType(contentType);
    }

    @Override
    protected void renderMergedOutputModel(
        @Nullable final Map<String, Object> map,
        final HttpServletRequest httpServletRequest,
        final HttpServletResponse httpServletResponse) {

      httpServletResponse.addHeader(RestApiExceptionUtils.ID_HEADER_NAME,
          StringUtils.hasText(restApiException.getId())
              ? restApiException.getId()
              : RestApiExceptionUtils.NO_ID_VALUE);

      httpServletResponse.addHeader(RestApiExceptionUtils.TIMESTAMP_HEADER_NAME,
          restApiException.getTimestamp() != null
              ? restApiException.getTimestamp().format(RestApiExceptionUtils.TIMESTAMP_FORMATTER)
              : OffsetDateTime.now(ZoneId.of("UTC")).format(
                  RestApiExceptionUtils.TIMESTAMP_FORMATTER));

      httpServletResponse.addHeader(RestApiExceptionUtils.MESSAGE_HEADER_NAME,
          StringUtils.hasText(restApiException.getMessage())
              ? restApiException.getMessage()
              : RestApiExceptionUtils.NO_MESSAGE_VALUE);

      httpServletResponse.addHeader(RestApiExceptionUtils.CODE_HEADER_NAME,
          StringUtils.hasText(restApiException.getErrorCode())
              ? restApiException.getErrorCode()
              : RestApiExceptionUtils.NO_ERROR_CODE_VALUE);

      httpServletResponse.addHeader(RestApiExceptionUtils.CLASS_HEADER_NAME,
          StringUtils.hasText(restApiException.getClassName())
              ? restApiException.getClassName()
              : RestApiExceptionUtils.NO_CLASS_VALUE);
    }

  }

}
