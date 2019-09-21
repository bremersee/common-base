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

package org.bremersee.data.ldaptive;

import javax.validation.constraints.NotNull;
import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.springframework.validation.annotation.Validated;

/**
 * The ldap connection callback.
 *
 * @param <T> the result type
 * @author Christian Bremer
 */
@Validated
public interface LdaptiveConnectionCallback<T> {

  /**
   * Execute ldap operations with the given connection.
   *
   * @param connection the connection
   * @return the result of the ldap operations
   * @throws LdapException the ldap exception
   */
  T doWithConnection(@NotNull Connection connection) throws LdapException;
}
