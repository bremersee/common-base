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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import lombok.ToString;

/**
 * @author Christian Bremer
 */
@ToString
class AclImpl implements Acl<Ace> {

  private final String owner;

  private final Map<String, ? extends Ace> entries;

  AclImpl(
      final String owner,
      final Map<String, ? extends Ace> entries) {
    this.owner = owner;
    this.entries = entries != null ? entries : new HashMap<>();
  }

  @Override
  public String getOwner() {
    return owner;
  }

  @Override
  public Map<String, ? extends Ace> entryMap() {
    return Collections.unmodifiableMap(entries);
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
        && sort(entries).equals(sort(acl.entryMap()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(owner, sort(entries));
  }

  private LinkedHashMap<String, Ace> sort(final Map<String, ? extends Ace> map) {
    final LinkedHashMap<String, Ace> sortedMap = new LinkedHashMap<>();
    if (map != null) {
      map
          .entrySet()
          .stream()
          .sorted((Comparator<Entry<String, ? extends Ace>>) (o1, o2) -> o1.getKey()
              .compareToIgnoreCase(o2.getKey()))
          .forEachOrdered(entry -> sortedMap.put(entry.getKey(), entry.getValue()));
    }
    return sortedMap;
  }
}
