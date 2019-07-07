/*
 * Copyright 2018 the original author or authors.
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

package org.bremersee.security.access;

/**
 * The permission constants.
 *
 * @author Christian Bremer
 */
@SuppressWarnings("WeakerAccess")
public abstract class PermissionConstants {

  /**
   * The constant ADMINISTRATION.
   */
  public static final String ADMINISTRATION = "administration";

  /**
   * The constant CREATE.
   */
  public static final String CREATE = "create";

  /**
   * The constant DELETE.
   */
  public static final String DELETE = "delete";

  /**
   * The constant READ.
   */
  public static final String READ = "read";

  /**
   * The constant WRITE.
   */
  public static final String WRITE = "write";

  /**
   * The constant ALL.
   */
  public static final String[] ALL = new String[]{
      ADMINISTRATION,
      CREATE,
      DELETE,
      READ,
      WRITE
  };

  private PermissionConstants() {
  }

}
