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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;

/**
 * The default ldaptive error handler test.
 *
 * @author Christian Bremer
 */
class DefaultLdaptiveErrorHandlerTest {

  /**
   * Map.
   */
  @Test
  void map() {
    DefaultLdaptiveErrorHandler errorHandler = new DefaultLdaptiveErrorHandler();
    assertNotNull(errorHandler.map(null));

    LdapException ldapException = new LdapException(ResultCode.NO_SUCH_OBJECT, "Not found.");
    assertEquals(404, errorHandler.map(ldapException).status());
    assertEquals(ldapException, errorHandler.map(ldapException).getCause());

    ldapException = new LdapException(ResultCode.INVALID_RESPONSE, "Internal server error.");
    assertEquals(500, errorHandler.map(ldapException).status());
    assertEquals(ldapException, errorHandler.map(ldapException).getCause());
  }

  /**
   * Handle error.
   */
  @Test
  void handleError() {
    DefaultLdaptiveErrorHandler errorHandler = new DefaultLdaptiveErrorHandler();

    LdaptiveException ldaptiveException = LdaptiveException.builder()
        .reason("A reason")
        .build();
    assertThrows(LdaptiveException.class, () -> errorHandler.handleError(ldaptiveException));

    LdapException ldapException = new LdapException(ResultCode.NO_SUCH_OBJECT, "Not found.");
    assertThrows(LdaptiveException.class, () -> errorHandler.handleError(ldapException));

    assertThrows(LdaptiveException.class, () -> errorHandler.handleError(new Exception()));
  }
}