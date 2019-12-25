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
import lombok.Getter;
import org.bremersee.exception.ErrorCodeAware;
import org.bremersee.exception.HttpStatusAware;
import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;
// import org.springframework.http.HttpStatus;

/**
 * The ldaptive exception.
 *
 * @author Christian Bremer
 */
@EqualsAndHashCode(callSuper = true)
public class LdaptiveException extends RuntimeException implements HttpStatusAware, ErrorCodeAware {

  @Getter
  private final Integer httpStatusCode;

  @Getter
  private final String errorCode;

  /**
   * Instantiates a new ldaptive exception.
   */
  public LdaptiveException() {
    super();
    this.httpStatusCode = 500;
    this.errorCode = null;
  }

  /**
   * Instantiates a new ldaptive exception.
   *
   * @param cause the cause
   */
  public LdaptiveException(Throwable cause) {
    super(cause);
    this.httpStatusCode = 500;
    this.errorCode = null;
  }

  public LdaptiveException(String errorCode, Throwable cause) {
    super(cause);
    this.httpStatusCode = 500;
    this.errorCode = errorCode;
  }

  public LdaptiveException(Integer httpStatus) {
    super();
    this.httpStatusCode = httpStatus;
    this.errorCode = null;
  }

  public LdaptiveException(String errorCode) {
    super();
    this.httpStatusCode = 500;
    this.errorCode = errorCode;
  }

  public LdaptiveException(Integer httpStatus, String errorCode) {
    super();
    this.httpStatusCode = httpStatus;
    this.errorCode = errorCode;
  }

  public LdaptiveException(Integer httpStatus, Throwable cause) {
    super(cause);
    this.httpStatusCode = httpStatus;
    this.errorCode = null;
  }

  public LdaptiveException(Integer httpStatus, String errorCode, Throwable cause) {
    super(cause);
    this.httpStatusCode = httpStatus;
    this.errorCode = errorCode;
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

  @Override
  public int status() {
    return httpStatusCode != null ? httpStatusCode : 500;
  }
}
