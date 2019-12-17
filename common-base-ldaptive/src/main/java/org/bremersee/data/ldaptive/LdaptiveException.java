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
import org.bremersee.exception.ServiceException;
import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;
import org.springframework.http.HttpStatus;

/**
 * The ldaptive exception.
 *
 * @author Christian Bremer
 */
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings({"WeakerAccess", "unused"})
public class LdaptiveException extends ServiceException {

  /**
   * Instantiates a new ldaptive exception.
   */
  public LdaptiveException() {
    super(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Instantiates a new ldaptive exception.
   *
   * @param cause the cause
   */
  public LdaptiveException(Throwable cause) {
    this(HttpStatus.INTERNAL_SERVER_ERROR, cause);
  }

  /**
   * Instantiates a new ldaptive exception.
   *
   * @param httpStatus the http status
   * @param cause      the cause
   */
  public LdaptiveException(HttpStatus httpStatus, Throwable cause) {
    super(httpStatus != null ? httpStatus : HttpStatus.INTERNAL_SERVER_ERROR, cause);
  }

  /**
   * Instantiates a new ldaptive exception.
   *
   * @param httpStatus the http status
   * @param errorCode  the error code
   * @param cause      the cause
   */
  public LdaptiveException(HttpStatus httpStatus, String errorCode, Throwable cause) {
    super(httpStatus != null ? httpStatus : HttpStatus.INTERNAL_SERVER_ERROR, errorCode, cause);
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

}
