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

import java.util.Map;

/**
 * The access control list interface.
 *
 * @param <E> the ace type parameter
 * @author Christian Bremer
 */
public interface Acl<E extends Ace> {

  /**
   * Gets owner.
   *
   * @return the owner
   */
  String getOwner();

  /**
   * Sets owner.
   *
   * @param owner the owner
   */
  void setOwner(String owner);

  /**
   * Returns the entries of this access control list. The key of the map is the permission. This map is normally
   * unmodifiable.
   *
   * @return the map
   */
  Map<String, ? extends E> entryMap();

}
