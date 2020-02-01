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

package org.bremersee.base.app;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bremersee.security.access.Ace;

/**
 * The access control entry entity.
 *
 * @author Christian Bremer
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
// @TypeAlias("ace")
public class AceEntity implements Ace {

  // @Indexed
  private boolean guest;

  // @Indexed
  private Set<String> users = new LinkedHashSet<>();

  // @Indexed
  private Set<String> roles = new LinkedHashSet<>();

  // @Indexed
  private Set<String> groups = new LinkedHashSet<>();

  /**
   * Instantiates a new access control entry entity.
   *
   * @param ace the ace
   */
  AceEntity(final Ace ace) {
    if (ace != null) {
      this.guest = ace.isGuest();
      this.users.addAll(treeSet(ace.getUsers()));
      this.roles.addAll(treeSet(ace.getRoles()));
      this.groups.addAll(treeSet(ace.getGroups()));
    }
  }

  /**
   * Unmodifiable ace entity.
   *
   * @return the ace entity
   */
  AceEntity unmodifiable() {
    final AceEntity ace = new AceEntity();
    ace.guest = this.guest;
    ace.users = Collections.unmodifiableSet(this.users);
    ace.roles = Collections.unmodifiableSet(this.roles);
    ace.groups = Collections.unmodifiableSet(this.groups);
    return ace;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Ace)) {
      return false;
    }
    Ace ace = (Ace) o;
    return guest == ace.isGuest() &&
        treeSet(groups).equals(treeSet(ace.getGroups())) &&
        treeSet(roles).equals(treeSet(ace.getRoles())) &&
        treeSet(users).equals(treeSet(ace.getUsers()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(guest, treeSet(users), treeSet(roles), treeSet(groups));
  }

  private TreeSet<String> treeSet(Collection<String> collection) {
    if (collection == null) {
      return new TreeSet<>();
    }
    if (collection instanceof TreeSet) {
      return (TreeSet<String>) collection;
    }
    return new TreeSet<>(collection);
  }
}