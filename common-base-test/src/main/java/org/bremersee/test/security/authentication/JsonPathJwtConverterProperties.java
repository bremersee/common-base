/*
 * Copyright 2020 the original author or authors.
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

package org.bremersee.test.security.authentication;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The json path jwt converter properties.
 *
 * @author Christian Bremer
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonPathJwtConverterProperties {

  /**
   * Roles json path.
   *
   * @return the string
   */
  String rolesJsonPath() default "$.realm_access.roles";

  /**
   * Specifies whether the roles value is a list (json array) or a simple string.
   *
   * @return the boolean
   */
  boolean rolesValueList() default true;

  /**
   * Roles value separator.
   *
   * @return the string
   */
  String rolesValueSeparator() default " ";

  /**
   * Role prefix.
   *
   * @return the string
   */
  String rolePrefix() default "ROLE_";

  /**
   * Name json path.
   *
   * @return the string
   */
  String nameJsonPath() default "$.preferred_username";

}
