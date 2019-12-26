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

package org.bremersee.data.ldaptive;

import lombok.EqualsAndHashCode;
import org.bremersee.exception.AbstractServiceExceptionBuilder;
import org.bremersee.exception.ErrorCodeAware;
import org.bremersee.exception.HttpStatusAware;
import org.bremersee.exception.ServiceException;
import org.bremersee.exception.ServiceExceptionBuilder;
import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;

/**
 * The ldaptive exception.
 *
 * @author Christian Bremer
 */
@EqualsAndHashCode(callSuper = true)
public class LdaptiveException extends ServiceException implements HttpStatusAware, ErrorCodeAware {

  /**
   * Instantiates a new ldaptive exception.
   */
  protected LdaptiveException() {
    super();
  }

  /**
   * Instantiates a new ldaptive exception.
   *
   * @param httpStatus the http status
   * @param errorCode  the error code
   */
  protected LdaptiveException(final int httpStatus, final String errorCode) {
    super(httpStatus, errorCode);
  }

  /**
   * Instantiates a new ldaptive exception.
   *
   * @param httpStatus the http status
   * @param errorCode  the error code
   * @param reason     the reason
   */
  protected LdaptiveException(final int httpStatus, final String errorCode, final String reason) {
    super(httpStatus, errorCode, reason);
  }

  /**
   * Instantiates a new ldaptive exception.
   *
   * @param httpStatus the http status
   * @param errorCode  the error code
   * @param cause      the cause
   */
  protected LdaptiveException(final int httpStatus, final String errorCode, final Throwable cause) {
    super(httpStatus, errorCode, cause);
  }

  /**
   * Instantiates a new ldaptive exception.
   *
   * @param httpStatus the http status
   * @param errorCode  the error code
   * @param reason     the reason
   * @param cause      the cause
   */
  protected LdaptiveException(
      final int httpStatus,
      final String errorCode,
      final String reason,
      final Throwable cause) {
    super(httpStatus, errorCode, reason, cause);
  }


  /**
   * Gets ldap exception (can be {@code null}).
   *
   * @return the ldap exception
   */
  public LdapException getLdapException() {
    return getCause() instanceof LdapException ? (LdapException) getCause() : null;
  }

  /**
   * Gets result code (can be {@code null}).
   *
   * @return the result code
   */
  public ResultCode getResultCode() {
    final LdapException ldapException = getLdapException();
    return ldapException != null ? ldapException.getResultCode() : null;
  }

  public static ServiceExceptionBuilder<LdaptiveException> builder() {
    return new AbstractServiceExceptionBuilder<>() {

      private static final long serialVersionUID = 2L;

      @Override
      protected LdaptiveException buildWith(int httpStatus, String errorCode) {
        return new LdaptiveException(httpStatus, errorCode);
      }

      @Override
      protected LdaptiveException buildWith(int httpStatus, String errorCode, String reason) {
        return new LdaptiveException(httpStatus, errorCode, reason);
      }

      @Override
      protected LdaptiveException buildWith(int httpStatus, String errorCode, Throwable cause) {
        return new LdaptiveException(httpStatus, errorCode, cause);
      }

      @Override
      protected LdaptiveException buildWith(int httpStatus, String errorCode, String reason,
          Throwable cause) {
        return new LdaptiveException(httpStatus, errorCode, reason, cause);
      }
    };
  }

}
