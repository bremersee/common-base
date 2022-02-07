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

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The test environment utils test.
 *
 * @author Christian Bremer
 */
@Slf4j
@ExtendWith(SoftAssertionsExtension.class)
class TestEnvironmentUtilsTest {

  /**
   * Gets executor.
   */
  @Test
  void getExecutor(SoftAssertions softly) {
    String rawValue = System.getProperty(TestEnvironmentUtils.EXECUTOR_SYSTEM_PROPERTY);
    log.info("System property {} = {}", TestEnvironmentUtils.EXECUTOR_SYSTEM_PROPERTY, rawValue);
    if (rawValue == null) {
      softly.assertThat(TestEnvironmentUtils.getExecutor())
          .as("No system property is present.")
          .isEqualTo(TestEnvironmentUtils.EXECUTOR_NOT_SPECIFIED);
    } else if (!TestEnvironmentUtils.EXECUTOR_BUILD_SYSTEM.equals(rawValue)) {
      try {
        String newValue = UUID.randomUUID().toString().replace("-", "");
        System.setProperty(TestEnvironmentUtils.EXECUTOR_SYSTEM_PROPERTY, newValue);
        softly.assertThat(TestEnvironmentUtils.getExecutor())
            .as("New random test executor value.")
            .isEqualTo(newValue);

      } finally {
        System.setProperty(TestEnvironmentUtils.EXECUTOR_SYSTEM_PROPERTY, rawValue);
      }
    } else {
      softly.assertThat(TestEnvironmentUtils.getExecutor())
          .as("Existing executor value.")
          .isEqualTo(rawValue);
    }
  }

}