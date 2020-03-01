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

package org.bremersee.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * The error detectors test.
 *
 * @author Christian Bremer
 */
public class ErrorDetectorsTest {

  /**
   * Tests default error detectors.
   */
  @Test
  public void defaultErrorDetectors() {
    assertTrue(ErrorDetectors.DEFAULT.test(HttpStatus.INTERNAL_SERVER_ERROR));
    assertTrue(ErrorDetectors.DEFAULT.test(HttpStatus.NOT_FOUND));
    assertFalse(ErrorDetectors.DEFAULT.test(HttpStatus.OK));
  }

}