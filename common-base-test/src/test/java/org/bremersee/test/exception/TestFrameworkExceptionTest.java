/*
 * Copyright 2022 the original author or authors.
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

package org.bremersee.test.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * The test framework exception test.
 *
 * @author Christian Bremer
 */
class TestFrameworkExceptionTest {

  /**
   * Gets message.
   */
  @Test
  void getMessage() {
    String msg = "Something failed";
    assertThat(new TestFrameworkException(msg).getMessage())
        .isEqualTo(msg);
  }

  /**
   * Gets cause.
   */
  @Test
  void getCause() {
    String msg = "Something failed";
    Exception cause = new TestFrameworkException(msg);
    assertThat(new TestFrameworkException(msg, cause).getCause())
        .isEqualTo(cause);
  }

}