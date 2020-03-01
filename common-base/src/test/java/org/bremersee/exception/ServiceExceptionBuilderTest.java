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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * The type Service exception builder test.
 *
 * @author Christian Bremer
 */
class ServiceExceptionBuilderTest {

  /**
   * Http status.
   */
  @Test
  void httpStatus() {
    ServiceExceptionBuilder<? extends ServiceException> builder = ServiceException.builder();
    builder.httpStatus(404);
    assertEquals(404, builder.build().status());
  }

  /**
   * Error code.
   */
  @Test
  void errorCode() {
    ServiceExceptionBuilder<? extends ServiceException> builder = ServiceException.builder();
    builder.errorCode("ERROR:CODE");
    assertEquals("ERROR:CODE", builder.build().getErrorCode());
  }

  /**
   * Reason.
   */
  @Test
  void reason() {
    ServiceExceptionBuilder<? extends ServiceException> builder = ServiceException.builder();
    builder.reason("Something went wrong");
    assertEquals("Something went wrong", builder.build().getMessage());
  }

  /**
   * Cause.
   */
  @Test
  void cause() {
    Exception cause = new Exception("Something went wrong");
    ServiceExceptionBuilder<? extends ServiceException> builder = ServiceException.builder();
    builder.cause(cause);
    assertEquals("Something went wrong", builder.build().getCause().getMessage());
  }

  /**
   * Build.
   */
  @Test
  void build() {
    Exception cause = new Exception("Something went wrong");
    ServiceExceptionBuilder<? extends ServiceException> builder = ServiceException.builder();
    builder.cause(cause);
    builder.reason("Something went wrong");
    builder.errorCode("ERROR:CODE");
    builder.httpStatus(404);
    ServiceException exception = builder.build();
    assertEquals(404, exception.status());
    assertEquals("ERROR:CODE", exception.getErrorCode());
    assertEquals("Something went wrong", exception.getMessage());
    assertEquals("Something went wrong", exception.getCause().getMessage());
  }

  /**
   * Build with null values.
   */
  @Test
  void buildWithNullValues() {
    ServiceExceptionBuilder<? extends ServiceException> builder = ServiceException.builder();
    builder.cause(null);
    builder.reason(null);
    builder.errorCode(null);
    ServiceException exception = builder.build();
    assertEquals(0, exception.status());
    assertNull(exception.getErrorCode());
    assertNull(exception.getMessage());
    assertNull(exception.getCause());
  }

}