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

import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;
import org.springframework.http.HttpStatus;

/**
 * The default ldaptive error handler.
 *
 * @author Christian Bremer
 */
public class DefaultLdaptiveErrorHandler extends AbstractLdaptiveErrorHandler {

  @Override
  public LdaptiveException map(final LdapException ldapException) {
    if (ldapException == null) {
      return new LdaptiveException();
    }
    final HttpStatus httpStatus = ldapException.getResultCode() == ResultCode.NO_SUCH_OBJECT
        ? HttpStatus.NOT_FOUND
        : HttpStatus.INTERNAL_SERVER_ERROR;
    return new LdaptiveException(httpStatus, ldapException);
  }

}
