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

package org.bremersee.exception;

import lombok.Getter;
import org.bremersee.ErrorCodeAware;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

/**
 * @author Christian Bremer
 */
public class ServiceException extends RuntimeException implements ErrorCodeAware {

  public static final String ERROR_CODE_ALREADY_EXISTS = "COMMON:ALREADY_EXISTS";

  @Getter
  private final Integer httpStatusCode;

  @Getter
  private final String errorCode;

  public ServiceException() {
    super();
    this.httpStatusCode = null;
    this.errorCode = null;
  }

  public ServiceException(final HttpStatus httpStatus) {
    super(detectReason(httpStatus));
    this.httpStatusCode = detectHttpStatusCode(httpStatus);
    this.errorCode = null;
  }

  public ServiceException(final HttpStatus httpStatus, final String errorCode) {
    super(detectReason(httpStatus));
    this.httpStatusCode = detectHttpStatusCode(httpStatus);
    this.errorCode = errorCode;
  }

  public ServiceException(final HttpStatus httpStatus, Throwable cause) {
    super(detectReason(httpStatus), cause);
    this.httpStatusCode = detectHttpStatusCode(httpStatus);
    this.errorCode = null;
  }

  public ServiceException(final HttpStatus httpStatus, final String errorCode, Throwable cause) {
    super(detectReason(httpStatus), cause);
    this.httpStatusCode = detectHttpStatusCode(httpStatus);
    this.errorCode = errorCode;
  }


  public ServiceException(final int httpStatusCode, final String reason) {
    super(reason);
    this.httpStatusCode = resolveHttpStatusCode(httpStatusCode);
    this.errorCode = null;
  }

  public ServiceException(final int httpStatusCode, final String reason, final String errorCode) {
    super(reason);
    this.httpStatusCode = resolveHttpStatusCode(httpStatusCode);
    this.errorCode = errorCode;
  }

  public ServiceException(final int httpStatusCode, final String reason, final Throwable cause) {
    super(reason, cause);
    this.httpStatusCode = resolveHttpStatusCode(httpStatusCode);
    this.errorCode = null;
  }

  public ServiceException(final int httpStatusCode, final String reason, final String errorCode,
      final Throwable cause) {
    super(reason, cause);
    this.httpStatusCode = resolveHttpStatusCode(httpStatusCode);
    this.errorCode = errorCode;
  }

  public Integer status() {
    return httpStatusCode;
  }


  private static Integer detectHttpStatusCode(final HttpStatus httpStatus) {
    return httpStatus != null ? httpStatus.value() : null;
  }

  private static String detectReason(final HttpStatus httpStatus) {
    return httpStatus != null ? httpStatus.getReasonPhrase() : null;
  }

  private static Integer resolveHttpStatusCode(final int httpStatusCode) {
    final HttpStatus httpStatus = HttpStatus.resolve(httpStatusCode);
    return httpStatus != null ? httpStatus.value() : null;
  }


  public static ServiceException internalServerError(final String reason) {
    return internalServerError(reason, (String) null);
  }

  public static ServiceException internalServerError(final String reason, final Throwable cause) {
    return internalServerError(reason, null, cause);
  }

  public static ServiceException internalServerError(final String reason, final String errorCode) {
    return new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR.value(), reason, errorCode);
  }

  public static ServiceException internalServerError(final String reason, final String errorCode,
      final Throwable cause) {
    return new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR.value(), reason, errorCode, cause);
  }


  public static ServiceException badRequest(final String reason) {
    return badRequest(reason, (String) null);
  }

  public static ServiceException badRequest(final String reason, final Throwable cause) {
    return badRequest(reason, null, cause);
  }

  public static ServiceException badRequest(final String reason, final String errorCode) {
    return new ServiceException(HttpStatus.BAD_REQUEST.value(), reason, errorCode);
  }

  public static ServiceException badRequest(final String reason, final String errorCode,
      final Throwable cause) {
    return new ServiceException(HttpStatus.BAD_REQUEST.value(), reason, errorCode, cause);
  }

  public static ServiceException notFound() {
    return new ServiceException(HttpStatus.NOT_FOUND);
  }

  public static ServiceException notFound(Object entityName) {
    return notFound("Entity", entityName);
  }

  public static ServiceException notFound(String entityType, Object entityName) {
    return notFoundWithErrorCode(entityType, entityName, null);
  }

  public static ServiceException notFoundWithErrorCode(
      Object entityName,
      String errorCode) {
    return notFoundWithErrorCode("Entity", entityName, errorCode);
  }

  public static ServiceException notFoundWithErrorCode(
      String entityType,
      Object entityName,
      String errorCode) {
    return new ServiceException(HttpStatus.NOT_FOUND.value(),
        entityType + " with identifier [" + entityName + "] was not found.", errorCode);
  }

  public static ServiceException alreadyExists(
      Object entityName) {
    return alreadyExistsWithErrorCode("Entity", entityName, null);
  }

  public static ServiceException alreadyExists(
      String entityType,
      Object entityName) {
    return alreadyExistsWithErrorCode(entityType, entityName, null);
  }

  public static ServiceException alreadyExistsWithErrorCode(
      Object entityName,
      String errorCode) {
    return alreadyExistsWithErrorCode("Entity", entityName, errorCode);
  }

  public static ServiceException alreadyExistsWithErrorCode(
      String entityType,
      Object entityName,
      String errorCode) {
    return new ServiceException(HttpStatus.CONFLICT.value(),
        entityType + " with identifier [" + entityName + "] already exists.",
        StringUtils.hasText(errorCode) ? errorCode : ERROR_CODE_ALREADY_EXISTS);
  }

  public static ServiceException forbidden() {
    return new ServiceException(HttpStatus.FORBIDDEN);
  }

  public static ServiceException forbidden(
      Object entityName) {
    return forbiddenWithErrorCode(entityName, null);
  }

  public static ServiceException forbidden(
      String entityType,
      Object entityName) {
    return forbiddenWithErrorCode(entityType, entityName, null);
  }

  public static ServiceException forbiddenWithErrorCode(String errorCode) {
    return new ServiceException(HttpStatus.FORBIDDEN, errorCode);
  }

  public static ServiceException forbiddenWithErrorCode(
      Object entityName,
      String errorCode) {
    return new ServiceException(HttpStatus.FORBIDDEN.value(),
        "Access to entity with identifier [" + entityName + "] is forbidden.",
        errorCode);
  }

  public static ServiceException forbiddenWithErrorCode(
      String entityType,
      Object entityName,
      String errorCode) {
    return new ServiceException(HttpStatus.FORBIDDEN.value(),
        "Access to [" + entityType + "] with identifier [" + entityName + "] is forbidden.",
        errorCode);
  }

}
