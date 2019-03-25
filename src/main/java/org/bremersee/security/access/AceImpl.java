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

import java.util.HashSet;
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
    this.users = new HashSet<>();
    this.roles = new HashSet<>();
    this.groups = new HashSet<>();
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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Ace)) {
      return false;
    }
    Ace ace = (Ace) o;
    TreeSet<String> g1 = new TreeSet<>(getGroups());
    TreeSet<String> g2 = new TreeSet<>(ace.getGroups());
    TreeSet<String> r1 = new TreeSet<>(getRoles());
    TreeSet<String> r2 = new TreeSet<>(ace.getRoles());
    TreeSet<String> u1 = new TreeSet<>(getUsers());
    TreeSet<String> u2 = new TreeSet<>(ace.getUsers());
    return guest == ace.isGuest() &&
        g1.equals(g2) &&
        r1.equals(r2) &&
        u1.equals(u2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(guest, new TreeSet<>(users), new TreeSet<>(roles), new TreeSet<>(groups));
  }
}
