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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.util.StringUtils;

/**
 * General service exception with http status code and error code.
 *
 * @author Christian Bremer
 */
@EqualsAndHashCode(callSuper = true)
public class ServiceException extends RuntimeException
    implements ErrorCodeAware, HttpStatusAware {

  private static final long serialVersionUID = 2L;

  /**
   * Default error code for an 'already exists exception'.
   */
  public static final String ERROR_CODE_ALREADY_EXISTS = "COMMON:ALREADY_EXISTS";

  private final int httpStatus;

  @Getter
  private final String errorCode;

  /**
   * Instantiates a new Service exception.
   */
  protected ServiceException() {
    super();
    this.httpStatus = 0;
    this.errorCode = null;
  }

  /**
   * Instantiates a new Service exception.
   *
   * @param httpStatus the http status
   * @param errorCode  the error code
   */
  protected ServiceException(final int httpStatus, final String errorCode) {
    super();
    this.httpStatus = httpStatus;
    this.errorCode = errorCode;
  }

  /**
   * Instantiates a new Service exception.
   *
   * @param httpStatus the http status
   * @param errorCode  the error code
   * @param reason     the reason
   */
  protected ServiceException(final int httpStatus, final String errorCode, final String reason) {
    super(reason);
    this.httpStatus = httpStatus;
    this.errorCode = errorCode;
  }

  /**
   * Instantiates a new Service exception.
   *
   * @param httpStatus the http status
   * @param errorCode  the error code
   * @param cause      the cause
   */
  protected ServiceException(final int httpStatus, final String errorCode, final Throwable cause) {
    super(cause);
    this.httpStatus = httpStatus;
    this.errorCode = errorCode;
  }

  /**
   * Instantiates a new Service exception.
   *
   * @param httpStatus the http status
   * @param errorCode  the error code
   * @param reason     the reason
   * @param cause      the cause
   */
  protected ServiceException(
      final int httpStatus,
      final String errorCode,
      final String reason,
      final Throwable cause) {
    super(reason, cause);
    this.httpStatus = httpStatus;
    this.errorCode = errorCode;
  }

  /**
   * Get the http status.
   *
   * @return the http status
   */
  @Override
  public int status() {
    return httpStatus;
  }

  /**
   * Creates new exception builder.
   *
   * @return the builder
   */
  public static ServiceExceptionBuilder<ServiceException> builder() {
    return new Builder();
  }

  /**
   * Internal server error service exception.
   *
   * @return the service exception
   */
  public static ServiceException internalServerError() {
    return internalServerError(null);
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
    return internalServerError(reason, errorCode, null);
  }

  /**
   * Internal server error service exception.
   *
   * @param reason    the reason
   * @param errorCode the error code
   * @param cause     the cause
   * @return the service exception
   */
  public static ServiceException internalServerError(
      final String reason,
      final String errorCode,
      final Throwable cause) {
    return ServiceException.builder()
        .httpStatus(500)
        .reason(reason)
        .errorCode(errorCode)
        .cause(cause)
        .build();
  }


  /**
   * Bad request service exception.
   *
   * @return the service exception
   */
  public static ServiceException badRequest() {
    return badRequest(null);
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
    return badRequest(reason, errorCode, null);
  }

  /**
   * Bad request service exception.
   *
   * @param reason    the reason
   * @param errorCode the error code
   * @param cause     the cause
   * @return the service exception
   */
  public static ServiceException badRequest(
      final String reason,
      final String errorCode,
      final Throwable cause) {
    return ServiceException.builder()
        .httpStatus(400)
        .reason(reason)
        .errorCode(errorCode)
        .cause(cause)
        .build();
  }

  /**
   * Not found service exception.
   *
   * @return the service exception
   */
  public static ServiceException notFound() {
    return new ServiceException(404, null);
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
    return new ServiceException(404, errorCode,
        entityType + " with identifier [" + entityName + "] was not found.");
  }

  /**
   * Already exists service exception.
   *
   * @return the service exception
   */
  public static ServiceException alreadyExists() {
    return new ServiceException(409, null);
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
    return new ServiceException(409,
        StringUtils.hasText(errorCode) ? errorCode : ERROR_CODE_ALREADY_EXISTS,
        entityType + " with identifier [" + entityName + "] already exists.");
  }

  /**
   * Forbidden service exception.
   *
   * @return the service exception
   */
  public static ServiceException forbidden() {
    return new ServiceException(403, null);
  }

  /**
   * Forbidden service exception.
   *
   * @param entityName the entity name
   * @return the service exception
   */
  public static ServiceException forbidden(Object entityName) {
    return forbiddenWithErrorCode(entityName, null);
  }

  /**
   * Forbidden service exception.
   *
   * @param entityType the entity type
   * @param entityName the entity name
   * @return the service exception
   */
  public static ServiceException forbidden(String entityType, Object entityName) {
    return forbiddenWithErrorCode(entityType, entityName, null);
  }

  /**
   * Forbidden with error code service exception.
   *
   * @param errorCode the error code
   * @return the service exception
   */
  public static ServiceException forbiddenWithErrorCode(String errorCode) {
    return new ServiceException(403, errorCode);
  }

  /**
   * Forbidden with error code service exception.
   *
   * @param entityName the entity name
   * @param errorCode  the error code
   * @return the service exception
   */
  public static ServiceException forbiddenWithErrorCode(Object entityName, String errorCode) {
    return new ServiceException(403, errorCode,
        "Access to entity with identifier [" + entityName + "] is forbidden.");
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
    return new ServiceException(403, errorCode,
        "Access to [" + entityType + "] with identifier [" + entityName + "] is forbidden.");
  }

  public static class Builder extends AbstractServiceExceptionBuilder<ServiceException> {

    private static final long serialVersionUID = 2L;

    @Override
    protected ServiceException buildWith(int httpStatus, String errorCode) {
      return new ServiceException(httpStatus, errorCode);
    }

    @Override
    protected ServiceException buildWith(int httpStatus, String errorCode, String reason) {
      return new ServiceException(httpStatus, errorCode, reason);
    }

    @Override
    protected ServiceException buildWith(int httpStatus, String errorCode, Throwable cause) {
      return new ServiceException(httpStatus, errorCode, cause);
    }

    @Override
    protected ServiceException buildWith(
        int httpStatus,
        String errorCode,
        String reason,
        Throwable cause) {
      return new ServiceException(httpStatus, errorCode, reason, cause);
    }
  }

}
