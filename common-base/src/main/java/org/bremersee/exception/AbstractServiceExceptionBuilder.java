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

package org.bremersee.exception;

import org.springframework.util.StringUtils;

/**
 * The abstract service exception builder.
 *
 * @param <T> the exception type
 * @author Christian Bremer
 */
public abstract class AbstractServiceExceptionBuilder<T extends ServiceException>
    implements ServiceExceptionBuilder<T> {

  private static final long serialVersionUID = 2L;

  /**
   * The http status.
   */
  protected int httpStatus;

  /**
   * The reason.
   */
  protected String reason;

  /**
   * The error code.
   */
  protected String errorCode;

  /**
   * The cause.
   */
  protected Throwable cause;

  @Override
  public ServiceExceptionBuilder<T> httpStatus(int httpStatus) {
    this.httpStatus = httpStatus;
    return this;
  }

  @Override
  public ServiceExceptionBuilder<T> errorCode(String errorCode) {
    this.errorCode = errorCode;
    return this;
  }

  @Override
  public ServiceExceptionBuilder<T> reason(String reason) {
    this.reason = reason;
    return this;
  }

  @Override
  public ServiceExceptionBuilder<T> cause(Throwable cause) {
    this.cause = cause;
    return this;
  }

  @Override
  public T build() {
    if (StringUtils.hasText(reason) && cause != null) {
      return buildWith(httpStatus, errorCode, reason, cause);
    }
    if (!StringUtils.hasText(reason) && cause != null) {
      return buildWith(httpStatus, errorCode, cause);
    }
    if (StringUtils.hasText(reason) && cause == null) {
      return buildWith(httpStatus, errorCode, reason);
    }
    return buildWith(httpStatus, errorCode);
  }

  /**
   * Build the service exception with the given values.
   *
   * @param httpStatus the http status
   * @param errorCode  the error code
   * @return the service exception
   */
  protected abstract T buildWith(int httpStatus, String errorCode);

  /**
   * Build the service exception with the given values.
   *
   * @param httpStatus the http status
   * @param errorCode  the error code
   * @param reason     the reason
   * @return the service exception
   */
  protected abstract T buildWith(int httpStatus, String errorCode, String reason);

  /**
   * Build the service exception with the given values.
   *
   * @param httpStatus the http status
   * @param errorCode  the error code
   * @param cause      the cause
   * @return the t
   */
  protected abstract T buildWith(int httpStatus, String errorCode, Throwable cause);

  /**
   * Build the service exception with the given values.
   *
   * @param httpStatus the http status
   * @param errorCode  the error code
   * @param reason     the reason
   * @param cause      the cause
   * @return the t
   */
  protected abstract T buildWith(int httpStatus, String errorCode, String reason, Throwable cause);

}
