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

package org.bremersee.security.core;

/**
 * Some authority constants.
 *
 * @author Christian Bremer
 */
@SuppressWarnings("unused")
public abstract class AuthorityConstants {

  /**
   * The constant ADMIN_ROLE_NAME.
   */
  public static final String ADMIN_ROLE_NAME = "ROLE_ADMIN";

  /**
   * The constant ACTUATOR_ADMIN_ROLE_NAME.
   */
  public static final String ACTUATOR_ADMIN_ROLE_NAME = "ROLE_ACTUATOR_ADMIN";

  /**
   * The constant ACTUATOR_ROLE_NAME.
   */
  public static final String ACTUATOR_ROLE_NAME = "ROLE_ACTUATOR";

  /**
   * The constant USER_ROLE_NAME.
   */
  public static final String USER_ROLE_NAME = "ROLE_USER";

  /**
   * The constant LOCAL_USER_ROLE_NAME.
   */
  public static final String LOCAL_USER_ROLE_NAME = "ROLE_LOCAL_USER";

  private AuthorityConstants() {
  }

}
