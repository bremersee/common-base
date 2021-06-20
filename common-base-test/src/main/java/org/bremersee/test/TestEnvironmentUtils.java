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

/**
 * The test environment utils.
 *
 * @author Christian Bremer
 */
public class TestEnvironmentUtils {

  /**
   * The constant EXECUTOR_SYSTEM_PROPERTY.
   */
  public static final String EXECUTOR_SYSTEM_PROPERTY = "org.bremersee.test.executor";

  /**
   * The constant EXECUTOR_NOT_SPECIFIED.
   */
  public static final String EXECUTOR_NOT_SPECIFIED = "NOT_SPECIFIED";

  /**
   * The constant EXECUTOR_BUILD_SYSTEM.
   */
  public static final String EXECUTOR_BUILD_SYSTEM = "BUILD_SYSTEM";

  /**
   * Gets executor.
   *
   * @return the executor
   */
  public static String getExecutor() {
    return System.getProperty(EXECUTOR_SYSTEM_PROPERTY, EXECUTOR_NOT_SPECIFIED);
  }

}
