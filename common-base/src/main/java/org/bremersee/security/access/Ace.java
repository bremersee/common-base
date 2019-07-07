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

package org.bremersee.security.access;

import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * The access control entry interface.
 *
 * @author Christian Bremer
 */
@Validated
public interface Ace {

  /**
   * Determines whether guests have access.
   *
   * @return {@code true} if guests have access, otherwise {@code false}
   */
  boolean isGuest();

  /**
   * Sets guest access.
   *
   * @param guest {@code true} if guests have access, otherwise {@code false}
   */
  void setGuest(boolean guest);

  /**
   * Gets users.
   *
   * @return the users
   */
  @NotNull
  Set<String> getUsers();

  /**
   * Gets roles.
   *
   * @return the roles
   */
  @NotNull
  Set<String> getRoles();

  /**
   * Gets groups.
   *
   * @return the groups
   */
  @NotNull
  Set<String> getGroups();

}
