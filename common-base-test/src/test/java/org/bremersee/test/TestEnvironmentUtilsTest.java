/*
 * Copyright 2021 the original author or authors.
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

package org.bremersee.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * The test environment utils test.
 *
 * @author Christian Bremer
 */
@Slf4j
class TestEnvironmentUtilsTest {

  /**
   * Gets executor.
   */
  @Test
  void getExecutor() {
    String rawValue = System.getProperty(TestEnvironmentUtils.EXECUTOR_SYSTEM_PROPERTY);
    log.info("System property {} = {}", TestEnvironmentUtils.EXECUTOR_SYSTEM_PROPERTY, rawValue);
    if (rawValue == null) {
      assertEquals(TestEnvironmentUtils.EXECUTOR_NOT_SPECIFIED, TestEnvironmentUtils.getExecutor());
    } else if (!TestEnvironmentUtils.EXECUTOR_BUILD_SYSTEM.equals(rawValue)) {
      try {
        String newValue = UUID.randomUUID().toString().replace("-", "");
        System.setProperty(TestEnvironmentUtils.EXECUTOR_SYSTEM_PROPERTY, newValue);
        assertEquals(newValue, TestEnvironmentUtils.getExecutor());

      } finally {
        System.setProperty(TestEnvironmentUtils.EXECUTOR_SYSTEM_PROPERTY, rawValue);
      }
    } else {
      assertEquals(rawValue, TestEnvironmentUtils.getExecutor());
    }
  }

}