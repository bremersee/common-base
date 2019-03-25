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

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import javax.validation.constraints.NotNull;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

/**
 * The internal access control entry implementation.
 *
 * @author Christian Bremer
 */
@ToString
@Validated
class AceImpl implements Ace {

  private boolean guest;

  private final Set<String> users;

  private final Set<String> roles;

  private final Set<String> groups;

  /**
   * Instantiates a new access control entry.
   */
  AceImpl() {
    this.users = new TreeSet<>();
    this.roles = new TreeSet<>();
    this.groups = new TreeSet<>();
  }

  @Override
  public boolean isGuest() {
    return guest;
  }

  @Override
  public void setGuest(boolean guest) {
    this.guest = guest;
  }

  @Override
  public @NotNull Set<String> getUsers() {
    return users;
  }

  @Override
  public @NotNull Set<String> getRoles() {
    return roles;
  }

  @Override
  public @NotNull Set<String> getGroups() {
    return groups;
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
