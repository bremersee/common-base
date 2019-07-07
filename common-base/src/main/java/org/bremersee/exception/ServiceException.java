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

import java.util.function.Supplier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

/**
 * General service exception with http status code and error code.
 *
 * @author Christian Bremer
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@EqualsAndHashCode(callSuper = true)
public class ServiceException
    extends RuntimeException
    implements ErrorCodeAware, Supplier<ServiceException> {

  /**
   * Default error code for an 'already exists exception'.
   */
  public static final String ERROR_CODE_ALREADY_EXISTS = "COMMON:ALREADY_EXISTS";

  @Getter
  private final Integer httpStatusCode;

  @Getter
  private final String errorCode;

  /**
   * Instantiates a new Service exception.
   */
  public ServiceException() {
    super();
    this.httpStatusCode = null;
    this.errorCode = null;
  }

  /**
   * Instantiates a new Service exception.
   *
   * @param httpStatus the http status
   */
  public ServiceException(final HttpStatus httpStatus) {
    super(detectReason(httpStatus));
    this.httpStatusCode = detectHttpStatusCode(httpStatus);
    this.errorCode = null;
  }

  /**
   * Instantiates a new Service exception.
   *
   * @param httpStatus the http status
   * @param errorCode  the error code
   */
  public ServiceException(final HttpStatus httpStatus, final String errorCode) {
    super(detectReason(httpStatus));
    this.httpStatusCode = detectHttpStatusCode(httpStatus);
    this.errorCode = errorCode;
  }

  /**
   * Instantiates a new Service exception.
   *
   * @param httpStatus the http status
   * @param cause      the cause
   */
  public ServiceException(final HttpStatus httpStatus, Throwable cause) {
    super(detectReason(httpStatus), cause);
    this.httpStatusCode = detectHttpStatusCode(httpStatus);
    this.errorCode = null;
  }

  /**
   * Instantiates a new Service exception.
   *
   * @param httpStatus the http status
   * @param errorCode  the error code
   * @param cause      the cause
   */
  public ServiceException(final HttpStatus httpStatus, final String errorCode, Throwable cause) {
    super(detectReason(httpStatus), cause);
    this.httpStatusCode = detectHttpStatusCode(httpStatus);
    this.errorCode = errorCode;
  }


  /**
   * Instantiates a new Service exception.
   *
   * @param httpStatusCode the http status code
   * @param reason         the reason
   */
  public ServiceException(final int httpStatusCode, final String reason) {
    super(reason);
    this.httpStatusCode = resolveHttpStatusCode(httpStatusCode);
    this.errorCode = null;
  }

  /**
   * Instantiates a new Service exception.
   *
   * @param httpStatusCode the http status code
   * @param reason         the reason
   * @param errorCode      the error code
   */
  public ServiceException(final int httpStatusCode, final String reason, final String errorCode) {
    super(reason);
    this.httpStatusCode = resolveHttpStatusCode(httpStatusCode);
    this.errorCode = errorCode;
  }

  /**
   * Instantiates a new Service exception.
   *
   * @param httpStatusCode the http status code
   * @param reason         the reason
   * @param cause          the cause
   */
  public ServiceException(final int httpStatusCode, final String reason, final Throwable cause) {
    super(reason, cause);
    this.httpStatusCode = resolveHttpStatusCode(httpStatusCode);
    this.errorCode = null;
  }

  /**
   * Instantiates a new Service exception.
   *
   * @param httpStatusCode the http status code
   * @param reason         the reason
   * @param errorCode      the error code
   * @param cause          the cause
   */
  public ServiceException(final int httpStatusCode, final String reason, final String errorCode,
      final Throwable cause) {
    super(reason, cause);
    this.httpStatusCode = resolveHttpStatusCode(httpStatusCode);
    this.errorCode = errorCode;
  }

  @Override
  public ServiceException get() {
    return this;
  }

  /**
   * Get the http status.
   *
   * @return the http status
   */
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


  /**
   * Internal server error service exception.
   *
   * @return the service exception
   */
  public static ServiceException internalServerError() {
    return internalServerError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), (String) null);
  }

  /**
   * Internal server error service exception.
   *
   * @param reason the reason
   * @return the service exception
   */
  public static ServiceException internalServerError(final String reason) {
    return internalServerError(reason, (String) null);
  }

  /**
   * Internal server error service exception.
   *
   * @param reason the reason
   * @param cause  the cause
   * @return the service exception
   */
  public static ServiceException internalServerError(final String reason, final Throwable cause) {
    return internalServerError(reason, null, cause);
  }

  /**
   * Internal server error service exception.
   *
   * @param reason    the reason
   * @param errorCode the error code
   * @return the service exception
   */
  public static ServiceException internalServerError(final String reason, final String errorCode) {
    return new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR.value(), reason, errorCode);
  }

  /**
   * Internal server error service exception.
   *
   * @param reason    the reason
   * @param errorCode the error code
   * @param cause     the cause
   * @return the service exception
   */
  public static ServiceException internalServerError(final String reason, final String errorCode,
      final Throwable cause) {
    return new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR.value(), reason, errorCode, cause);
  }


  /**
   * Bad request service exception.
   *
   * @return the service exception
   */
  public static ServiceException badRequest() {
    return badRequest(HttpStatus.BAD_REQUEST.getReasonPhrase(), (String) null);
  }

  /**
   * Bad request service exception.
   *
   * @param reason the reason
   * @return the service exception
   */
  public static ServiceException badRequest(final String reason) {
    return badRequest(reason, (String) null);
  }

  /**
   * Bad request service exception.
   *
   * @param reason the reason
   * @param cause  the cause
   * @return the service exception
   */
  public static ServiceException badRequest(final String reason, final Throwable cause) {
    return badRequest(reason, null, cause);
  }

  /**
   * Bad request service exception.
   *
   * @param reason    the reason
   * @param errorCode the error code
   * @return the service exception
   */
  public static ServiceException badRequest(final String reason, final String errorCode) {
    return new ServiceException(HttpStatus.BAD_REQUEST.value(), reason, errorCode);
  }

  /**
   * Bad request service exception.
   *
   * @param reason    the reason
   * @param errorCode the error code
   * @param cause     the cause
   * @return the service exception
   */
  public static ServiceException badRequest(final String reason, final String errorCode,
      final Throwable cause) {
    return new ServiceException(HttpStatus.BAD_REQUEST.value(), reason, errorCode, cause);
  }

  /**
   * Not found service exception.
   *
   * @return the service exception
   */
  public static ServiceException notFound() {
    return new ServiceException(HttpStatus.NOT_FOUND);
  }

  /**
   * Not found service exception.
   *
   * @param entityName the entity name
   * @return the service exception
   */
  public static ServiceException notFound(Object entityName) {
    return notFound("Entity", entityName);
  }

  /**
   * Not found service exception.
   *
   * @param entityType the entity type
   * @param entityName the entity name
   * @return the service exception
   */
  public static ServiceException notFound(String entityType, Object entityName) {
    return notFoundWithErrorCode(entityType, entityName, null);
  }

  /**
   * Not found with error code service exception.
   *
   * @param entityName the entity name
   * @param errorCode  the error code
   * @return the service exception
   */
  public static ServiceException notFoundWithErrorCode(
      Object entityName,
      String errorCode) {
    return notFoundWithErrorCode("Entity", entityName, errorCode);
  }

  /**
   * Not found with error code service exception.
   *
   * @param entityType the entity type
   * @param entityName the entity name
   * @param errorCode  the error code
   * @return the service exception
   */
  public static ServiceException notFoundWithErrorCode(
      String entityType,
      Object entityName,
      String errorCode) {
    return new ServiceException(HttpStatus.NOT_FOUND.value(),
        entityType + " with identifier [" + entityName + "] was not found.", errorCode);
  }

  /**
   * Already exists service exception.
   *
   * @return the service exception
   */
  public static ServiceException alreadyExists() {
    return new ServiceException(HttpStatus.CONFLICT);
  }

  /**
   * Already exists service exception.
   *
   * @param entityName the entity name
   * @return the service exception
   */
  public static ServiceException alreadyExists(
      Object entityName) {
    return alreadyExistsWithErrorCode("Entity", entityName, null);
  }

  /**
   * Already exists service exception.
   *
   * @param entityType the entity type
   * @param entityName the entity name
   * @return the service exception
   */
  public static ServiceException alreadyExists(
      String entityType,
      Object entityName) {
    return alreadyExistsWithErrorCode(entityType, entityName, null);
  }

  /**
   * Already exists with error code service exception.
   *
   * @param entityName the entity name
   * @param errorCode  the error code
   * @return the service exception
   */
  public static ServiceException alreadyExistsWithErrorCode(
      Object entityName,
      String errorCode) {
    return alreadyExistsWithErrorCode("Entity", entityName, errorCode);
  }

  /**
   * Already exists with error code service exception.
   *
   * @param entityType the entity type
   * @param entityName the entity name
   * @param errorCode  the error code
   * @return the service exception
   */
  public static ServiceException alreadyExistsWithErrorCode(
      String entityType,
      Object entityName,
      String errorCode) {
    return new ServiceException(HttpStatus.CONFLICT.value(),
        entityType + " with identifier [" + entityName + "] already exists.",
        StringUtils.hasText(errorCode) ? errorCode : ERROR_CODE_ALREADY_EXISTS);
  }

  /**
   * Forbidden service exception.
   *
   * @return the service exception
   */
  public static ServiceException forbidden() {
    return new ServiceException(HttpStatus.FORBIDDEN);
  }

  /**
   * Forbidden service exception.
   *
   * @param entityName the entity name
   * @return the service exception
   */
  public static ServiceException forbidden(
      Object entityName) {
    return forbiddenWithErrorCode(entityName, null);
  }

  /**
   * Forbidden service exception.
   *
   * @param entityType the entity type
   * @param entityName the entity name
   * @return the service exception
   */
  public static ServiceException forbidden(
      String entityType,
      Object entityName) {
    return forbiddenWithErrorCode(entityType, entityName, null);
  }

  /**
   * Forbidden with error code service exception.
   *
   * @param errorCode the error code
   * @return the service exception
   */
  public static ServiceException forbiddenWithErrorCode(String errorCode) {
    return new ServiceException(HttpStatus.FORBIDDEN, errorCode);
  }

  /**
   * Forbidden with error code service exception.
   *
   * @param entityName the entity name
   * @param errorCode  the error code
   * @return the service exception
   */
  public static ServiceException forbiddenWithErrorCode(
      Object entityName,
      String errorCode) {
    return new ServiceException(HttpStatus.FORBIDDEN.value(),
        "Access to entity with identifier [" + entityName + "] is forbidden.",
        errorCode);
  }

  /**
   * Forbidden with error code service exception.
   *
   * @param entityType the entity type
   * @param entityName the entity name
   * @param errorCode  the error code
   * @return the service exception
   */
  public static ServiceException forbiddenWithErrorCode(
      String entityType,
      Object entityName,
      String errorCode) {
    return new ServiceException(HttpStatus.FORBIDDEN.value(),
        "Access to [" + entityType + "] with identifier [" + entityName + "] is forbidden.",
        errorCode);
  }

}
