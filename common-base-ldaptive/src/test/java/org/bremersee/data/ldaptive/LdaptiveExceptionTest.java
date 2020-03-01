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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;

/**
 * The ldaptive exception test.
 *
 * @author Christian Bremer
 */
class LdaptiveExceptionTest {

  /**
   * Builder.
   */
  @Test
  void builder() {
    LdaptiveException ldaptiveException = LdaptiveException.builder().build();
    assertEquals(0, ldaptiveException.status());
    assertNull(ldaptiveException.getErrorCode());
    assertNull(ldaptiveException.getLdapException());
    assertNull(ldaptiveException.getResultCode());

    ldaptiveException = LdaptiveException.builder()
        .httpStatus(400)
        .build();
    assertEquals(400, ldaptiveException.status());
    assertNull(ldaptiveException.getErrorCode());
    assertNull(ldaptiveException.getLdapException());
    assertNull(ldaptiveException.getResultCode());

    ldaptiveException = LdaptiveException.builder()
        .errorCode("1234")
        .build();
    assertEquals(0, ldaptiveException.status());
    assertEquals("1234", ldaptiveException.getErrorCode());
    assertNull(ldaptiveException.getLdapException());
    assertNull(ldaptiveException.getResultCode());

    ldaptiveException = LdaptiveException.builder()
        .httpStatus(505)
        .errorCode("1234")
        .cause(new LdapException("Something went wrong", ResultCode.CONNECT_ERROR))
        .build();
    assertEquals(505, ldaptiveException.status());
    assertEquals("1234", ldaptiveException.getErrorCode());
    assertNotNull(ldaptiveException.getLdapException());
    assertEquals(ResultCode.CONNECT_ERROR, ldaptiveException.getResultCode());

    ldaptiveException = LdaptiveException.builder()
        .httpStatus(505)
        .errorCode("1234")
        .cause(new LdapException("Something went wrong", ResultCode.CONNECT_ERROR))
        .reason("Oops")
        .build();
    assertEquals(505, ldaptiveException.status());
    assertEquals("1234", ldaptiveException.getErrorCode());
    assertNotNull(ldaptiveException.getLdapException());
    assertEquals(ResultCode.CONNECT_ERROR, ldaptiveException.getResultCode());
    assertEquals("Oops", ldaptiveException.getMessage());

    ldaptiveException = LdaptiveException.builder()
        .httpStatus(500)
        .errorCode("1234")
        .reason("Something went wrong.")
        .build();
    assertEquals(500, ldaptiveException.status());
    assertEquals("1234", ldaptiveException.getErrorCode());
    assertNull(ldaptiveException.getLdapException());
    assertNull(ldaptiveException.getResultCode());
    assertEquals("Something went wrong.", ldaptiveException.getMessage());

  }
}