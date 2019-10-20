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

import lombok.extern.slf4j.Slf4j;
import org.ldaptive.LdapException;

/**
 * The abstract ldaptive error handler.
 *
 * @author Christian Bremer
 */
@Slf4j
public abstract class AbstractLdaptiveErrorHandler implements LdaptiveErrorHandler {

  @Override
  public void handleError(final Throwable t) {
    final LdaptiveException ldaptiveException;
    if (t instanceof LdaptiveException) {
      ldaptiveException = (LdaptiveException) t;
    } else if (t instanceof LdapException) {
      ldaptiveException = map((LdapException) t);
    } else if (t != null) {
      ldaptiveException = new LdaptiveException(t);
    } else {
      ldaptiveException = new LdaptiveException();
    }
    log.error("LDAP operation failed.", ldaptiveException);
    throw ldaptiveException;
  }

}
