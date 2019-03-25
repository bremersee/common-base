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

import javax.validation.constraints.NotNull;
import org.bremersee.common.model.AccessControlList;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

/**
 * The acl mapper interface.
 *
 * @param <T> the acl type
 * @author Christian Bremer
 */
@Validated
public interface AclMapper<T extends Acl<? extends Ace>> {

  /**
   * Map access control list (entity) to dto.
   *
   * @param acl the acl
   * @return the access control list
   */
  default AccessControlList map(@Nullable T acl) {
    return AclBuilder.builder().from(acl).buildAccessControlList();
  }

  /**
   * Map access control list dto to entity.
   *
   * @param accessControlList the access control list
   * @param aclFactory        the acl factory
   * @return the acl of the specified type
   */
  default T map(@Nullable AccessControlList accessControlList, @NotNull AclFactory<T> aclFactory) {
    return AclBuilder.builder().from(accessControlList).build(aclFactory);
  }

  /**
   * Map access control list dto to entity.
   *
   * @param accessControlList the access control list
   * @return the acl of the specified type
   */
  T map(@Nullable AccessControlList accessControlList);

}
