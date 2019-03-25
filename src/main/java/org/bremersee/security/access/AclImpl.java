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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import lombok.ToString;

/**
 * @author Christian Bremer
 */
@ToString(callSuper = true)
class AclImpl extends TreeMap<String, Ace> implements Acl<Ace> {

  private final String owner;

  AclImpl(
      final String owner,
      final Map<String, ? extends Ace> entries) {
    this.owner = owner;
    if (entries != null) {
      putAll(entries);
    }
  }

  @Override
  public String getOwner() {
    return owner;
  }

  @Override
  public Map<String, ? extends Ace> entryMap() {
    return Collections.unmodifiableMap(this);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Acl)) {
      return false;
    }
    Acl<? extends Ace> acl = (Acl<? extends Ace>) o;
    return Objects.equals(owner, acl.getOwner())
        && (new TreeMap<>(this)).equals(new TreeMap<>(acl.entryMap()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(owner, new TreeMap<>(this));
  }

}
