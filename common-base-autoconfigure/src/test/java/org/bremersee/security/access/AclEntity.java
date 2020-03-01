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

package org.bremersee.security.access;

import java.util.Map;

/**
 * The test acl entity.
 *
 * @author Christian Bremer
 */
public class AclEntity extends AclImpl {

  /**
   * Instantiates a new acl entity. The default constructor is required by the model mapper.
   */
  @SuppressWarnings("unused")
  public AclEntity() {
  }

  /**
   * Instantiates a new acl entity.
   *
   * @param owner the owner
   * @param entries the entries
   */
  public AclEntity(String owner,
      Map<String, ? extends Ace> entries) {
    super(owner, entries);
  }

}
