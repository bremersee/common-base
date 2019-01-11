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

package org.bremersee.exception;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionMapperProperties.ExceptionMappingConfig;
import org.bremersee.exception.annotation.ErrorCode;
import org.bremersee.exception.model.Handler;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.exception.model.StackTraceItem;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;

/**
 * The default implementation of a rest api exception mapper.
 *
 * @author Christian Bremer
 */
@Validated
@Slf4j
public class RestApiExceptionMapperImpl implements RestApiExceptionMapper {

  @Getter(AccessLevel.PROTECTED)
  private RestApiExceptionMapperProperties properties;

  @Getter(AccessLevel.PROTECTED)
  private String applicationName;

  /**
   * Instantiates a new rest api exception mapper.
   *
   * @param properties      the properties
   * @param applicationName the application name
   */
  public RestApiExceptionMapperImpl(
      RestApiExceptionMapperProperties properties,
      String applicationName) {
    this.properties = properties;
    this.applicationName = applicationName;
  }

  @Override
  public List<String> getApiPaths() {
    return properties.getApiPaths();
  }

  @Override
  public HttpStatus detectHttpStatus(@NotNull Throwable exception, @Nullable Object handler) {

    HttpStatus httpStatus = null;
    if (exception instanceof HttpStatusAware) {
      httpStatus = fromStatus(((HttpStatusAware) exception).status());
    }
    if (httpStatus == null && (exception instanceof ResponseStatusException)) {
      httpStatus = ((ResponseStatusException) exception).getStatus();
    }
    if (httpStatus == null) {
      final ResponseStatus ann = AnnotatedElementUtils
          .findMergedAnnotation(exception.getClass(), ResponseStatus.class);
      if (ann != null) {
        httpStatus = ann.code();
      }
    }
    if (httpStatus == null) {
      final Method method = findHandlerMethod(handler);
      if (method != null) {
        final ResponseStatus ann = AnnotatedElementUtils
            .findMergedAnnotation(method, ResponseStatus.class);
        if (ann != null) {
          httpStatus = ann.code();
        }
      }
    }
    if (httpStatus == null) {
      final Object result = getMethodValue(exception, "status");
      if (result instanceof Integer) {
        httpStatus = fromStatus((Integer) result);
      }
    }
    if (httpStatus == null) {
      httpStatus = fromStatus(properties.findExceptionMapping(exception).getStatus());
    }
    if (httpStatus == null) {
      httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    return httpStatus;
  }

  @SuppressWarnings("SameParameterValue")
  private <T> T getMethodValue(
      @NotNull final Throwable throwable,
      @NotNull final String methodName) {

    try {
      final Method method = ReflectionUtils.findMethod(
          throwable.getClass(), methodName);
      if (method != null) {
        //noinspection unchecked
        return (T) ReflectionUtils.invokeMethod(method, throwable);
      } else {
        log.debug("Method " + methodName + " not found in " + throwable.getClass().getName());
        return null;
      }
    } catch (Exception e) {
      log.warn("Calling " + methodName + " from " + throwable.getClass().getName() + " failed. "
          + "Returning null.", e);
      return null;
    }
  }

  @Nullable
  private HttpStatus fromStatus(@Nullable final Integer status) {
    if (status == null) {
      return null;
    }
    return HttpStatus.resolve(status);
  }

  @Override
  public RestApiException build(
      final Throwable exception,
      final String requestPath,
      final Object handler) {

    final ExceptionMappingConfig config = getProperties().findExceptionMappingConfig(exception);
    final HttpStatus httpStatus = detectHttpStatus(exception, handler);

    final RestApiException restApiException = new RestApiException();
    if (httpStatus.series() == HttpStatus.Series.SERVER_ERROR) {
      restApiException.setId(UUID.randomUUID().toString());
    }
    restApiException.setTimestamp(OffsetDateTime.now(ZoneId.of("UTC")));
    restApiException.setMessage(detectMessage(exception, handler, config));
    if (config.isIncludeExceptionClassName()) {
      restApiException.setClassName(exception.getClass().getName());
    }
    if (config.isIncludeApplicationName()) {
      restApiException.setApplication(getApplicationName());
    }
    if (config.isIncludePath()) {
      restApiException.setPath(requestPath);
    }
    if (config.isIncludeHandler()) {
      restApiException.setHandler(buildHandler(handler));
    }
    if (config.isIncludeStackTrace()) {
      addStackTraceItems(restApiException, exception.getStackTrace());
    }

    final RestApiException cause;
    if (exception instanceof RestApiExceptionAware
        && ((RestApiExceptionAware) exception).getRestApiException() != null) {
      final RestApiException source = ((RestApiExceptionAware) exception).getRestApiException();
      cause = cloneRestApiException(source, config);
    } else {
      cause = buildRestApiExceptionCause(exception.getCause(), config);
    }
    if (cause != null && StringUtils.hasText(cause.getErrorCode())
        && !RestApiExceptionUtils.NO_ERROR_CODE_VALUE.equals(cause.getErrorCode())) {
      restApiException.setErrorCode(cause.getErrorCode());
      restApiException.setErrorCodeInherited(true);
    } else {
      restApiException.setErrorCode(detectErrorCode(exception, handler, config));
      restApiException.setErrorCodeInherited(false);
    }

    if (config.isIncludeCause()) {
      restApiException.setCause(cause);
    }

    return restApiException;
  }

  /**
   * Find the handler class.
   *
   * @param handler the handler
   * @return the class
   */
  @SuppressWarnings("WeakerAccess")
  @Nullable
  protected Class<?> findHandlerClass(Object handler) {
    if (handler == null) {
      return null;
    } else if (handler instanceof HandlerMethod) {
      return ((HandlerMethod) handler).getBean().getClass();
    } else {
      return handler.getClass();
    }
  }

  /**
   * Find the handler method.
   *
   * @param handler the handler
   * @return the method
   */
  @SuppressWarnings("WeakerAccess")
  @Nullable
  protected Method findHandlerMethod(Object handler) {
    if (handler instanceof HandlerMethod) {
      return ((HandlerMethod) handler).getMethod();
    } else {
      return null;
    }
  }

  /**
   * Detect message exception message.
   *
   * @param exception the exception
   * @param handler   the handler
   * @param config    the config
   * @return the exception message
   */
  @SuppressWarnings("WeakerAccess")
  @NotNull
  protected String detectMessage(
      final @NotNull Throwable exception,
      final @Nullable Object handler,
      final @NotNull ExceptionMappingConfig config) {

    String message = exception.getMessage();
    if (StringUtils.hasText(message) && !config.isEvaluateAnnotationFirst()) {
      return message;
    }

    ResponseStatus responseStatus = AnnotatedElementUtils.findMergedAnnotation(
        exception.getClass(), ResponseStatus.class);
    if (responseStatus == null) {
      Method method = findHandlerMethod(handler);
      if (method != null) {
        responseStatus = AnnotatedElementUtils.findMergedAnnotation(method, ResponseStatus.class);
      }
    }
    if (responseStatus != null && StringUtils.hasText(responseStatus.reason())) {
      message = responseStatus.reason();
    }
    return StringUtils.hasText(message)
        ? message
        : getProperties().findExceptionMapping(exception).getMessage();
  }

  /**
   * Detect the error code.
   *
   * @param exception the exception
   * @param handler   the handler
   * @param config    the config
   * @return the string
   */
  @SuppressWarnings("WeakerAccess")
  @Nullable
  protected String detectErrorCode(
      final @NotNull Throwable exception,
      final @Nullable Object handler,
      final @NotNull ExceptionMappingConfig config) {

    String code = (exception instanceof ErrorCodeAware)
        ? ((ErrorCodeAware) exception).getErrorCode()
        : null;
    if (StringUtils.hasText(code) && !config.isEvaluateAnnotationFirst()) {
      return code;
    }

    ErrorCode errorCode = AnnotationUtils.findAnnotation(exception.getClass(), ErrorCode.class);
    if (errorCode == null) {
      Method method = findHandlerMethod(handler);
      if (method != null) {
        errorCode = AnnotationUtils.findAnnotation(method, ErrorCode.class);
      }
    }
    if (errorCode != null && StringUtils.hasText(errorCode.value())) {
      code = errorCode.value();
    }
    return StringUtils.hasText(code)
        ? code
        : getProperties().findExceptionMapping(exception).getCode();
  }

  /**
   * Build the handler model of the rest ape exception.
   *
   * @param handler the handler
   * @return the handler model
   */
  @SuppressWarnings("WeakerAccess")
  @Nullable
  protected Handler buildHandler(@Nullable Object handler) {
    final Method method = findHandlerMethod(handler);
    if (method == null) {
      return null;
    }
    final Handler model = new Handler();
    model.setMethodName(method.getName());
    final Class<?> handlerClass = findHandlerClass(handler);
    model.setClassName(handlerClass != null ? handlerClass.getName() : null);
    final Class<?>[] types = method.getParameterTypes();
    for (Class<?> type : types) {
      model.addMethodParameterTypesItem(type.getName());
    }
    return model;
  }

  /**
   * Add stack trace items.
   *
   * @param restApiException the rest api exception
   * @param stackTrace       the stack trace
   */
  @SuppressWarnings("WeakerAccess")
  protected void addStackTraceItems(
      final @NotNull RestApiException restApiException,
      final @Nullable StackTraceElement[] stackTrace) {

    if (stackTrace != null) {
      for (StackTraceElement elem : stackTrace) {
        restApiException.addStackTraceItem(
            new StackTraceItem().declaringClass(elem.getClassName()).fileName(elem.getFileName())
                .lineNumber(elem.getLineNumber()).methodName(elem.getMethodName()));
      }
    }
  }

  /**
   * Build the cause of a rest api exception.
   *
   * @param cause  the cause
   * @param config the config
   * @return the rest api exception
   */
  @SuppressWarnings("WeakerAccess")
  @Nullable
  protected RestApiException buildRestApiExceptionCause(
      final @Nullable Throwable cause,
      final @NotNull ExceptionMappingConfig config) {

    if (cause == null) {
      return null;
    }

    if (cause instanceof RestApiExceptionAware
        && ((RestApiExceptionAware) config).getRestApiException() != null) {
      final RestApiException source = ((RestApiExceptionAware) cause).getRestApiException();
      return cloneRestApiException(source, config);
    }

    final RestApiException restApiException = new RestApiException();
    restApiException.setMessage(detectMessage(cause, null, config));
    restApiException.setErrorCode(detectErrorCode(cause, null, config));
    if (config.isIncludeExceptionClassName()) {
      restApiException.setClassName(cause.getClass().getName());
    }
    if (config.isIncludeStackTrace()) {
      addStackTraceItems(restApiException, cause.getStackTrace());
    }
    restApiException.setCause(buildRestApiExceptionCause(cause.getCause(), config));
    return restApiException;
  }

  @Nullable
  private RestApiException cloneRestApiException(
      final @Nullable RestApiException source,
      final @NotNull ExceptionMappingConfig config) {

    if (source == null) {
      return null;
    }

    final RestApiException destination = new RestApiException();
    destination.setId(source.getId());
    destination.setTimestamp(source.getTimestamp());
    destination.setMessage(source.getMessage());
    destination.setErrorCode(source.getErrorCode());
    destination.setErrorCodeInherited(source.isErrorCodeInherited());
    if (config.isIncludeExceptionClassName()) {
      destination.setClassName(source.getClassName());
    }
    if (config.isIncludeApplicationName()) {
      destination.setApplication(source.getApplication());
    }
    if (config.isIncludePath()) {
      destination.setPath(source.getPath());
    }
    if (config.isIncludeHandler()) {
      destination.setHandler(cloneHandler(source.getHandler()));
    }
    if (source.getStackTrace() != null) {
      for (final StackTraceItem item : source.getStackTrace()) {
        destination.addStackTraceItem(item);
      }
    }
    if (config.isIncludeCause()) {
      destination.setCause(cloneRestApiException(source.getCause(), config));
    }
    return destination;
  }

  @Nullable
  private Handler cloneHandler(final @Nullable Handler source) {
    if (source == null) {
      return null;
    }
    final Handler destination = new Handler();
    destination.setClassName(destination.getClassName());
    destination.setMethodName(source.getMethodName());
    if (source.getMethodParameterTypes() != null) {
      for (final String item : source.getMethodParameterTypes()) {
        destination.addMethodParameterTypesItem(item);
      }
    }
    return destination;
  }

}
