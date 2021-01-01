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

import java.util.Optional;
import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;

/**
 * The default ldaptive error handler.
 *
 * @author Christian Bremer
 */
public class DefaultLdaptiveErrorHandler extends AbstractLdaptiveErrorHandler {

  @Override
  public LdaptiveException map(LdapException ldapException) {
    return LdaptiveException.builder()
        .errorCode(errorCode(ldapException))
        .httpStatus(httpStatus(ldapException))
        .cause(ldapException)
        .build();
  }

  private int httpStatus(LdapException ldapException) {
    return Optional.ofNullable(ldapException)
        .map(LdapException::getResultCode)
        .map(this::httpStatus)
        .orElse(500);
  }

  private int httpStatus(ResultCode resultCode) {
    ResultCode code = Optional.ofNullable(resultCode).orElse(ResultCode.OTHER);
    switch (code) {
      case SUCCESS:
        return 200;
      case INVALID_CREDENTIALS:
        return 400;
      case NO_SUCH_OBJECT:
        return 404;
      default:
        return 500;
    }
  }

  private String errorCode(LdapException ldapException) {
    return Optional.ofNullable(ldapException)
        .map(LdapException::getResultCode)
        .map(ResultCode::name)
        .orElse(null);
  }

}
