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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bremersee.security.access.Ace;
import org.bremersee.security.access.Acl;
import org.bremersee.security.access.PermissionConstants;

/**
 * The access control list entity.
 *
 * @author Christian Bremer
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class AclEntity implements Acl<AceEntity> {

  private String owner;

  private AceEntity administration = new AceEntity();

  private AceEntity create = new AceEntity();

  private AceEntity delete = new AceEntity();

  private AceEntity read = new AceEntity();

  private AceEntity write = new AceEntity();

  /**
   * Instantiates a new access control list entity.
   *
   * @param owner the owner
   * @param entries the entries
   */
  public AclEntity(final String owner, final Map<String, ? extends Ace> entries) {
    this.owner = owner;
    if (entries != null) {
      for (Map.Entry<String, ? extends Ace> entry : entries.entrySet()) {
        if (entry != null && entry.getKey() != null && entry.getValue() != null) {
          final String permission = entry.getKey().toLowerCase();
          final AceEntity ace = new AceEntity(entry.getValue());
          switch (permission) {
            case PermissionConstants.ADMINISTRATION:
              administration = ace;
              break;
            case PermissionConstants.CREATE:
              create = ace;
              break;
            case PermissionConstants.DELETE:
              delete = ace;
              break;
            case PermissionConstants.READ:
              read = ace;
              break;
            case PermissionConstants.WRITE:
              write = ace;
              break;
            default:
              break;
          }
        }
      }
    }
  }

  @Override
  public Map<String, ? extends AceEntity> entryMap() {
    final Map<String, AceEntity> map = new TreeMap<>();
    map.put(PermissionConstants.ADMINISTRATION, administration.unmodifiable());
    map.put(PermissionConstants.CREATE, create.unmodifiable());
    map.put(PermissionConstants.DELETE, delete.unmodifiable());
    map.put(PermissionConstants.READ, read.unmodifiable());
    map.put(PermissionConstants.WRITE, write.unmodifiable());
    return Collections.unmodifiableMap(map);
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
        && (new TreeMap<>(entryMap())).equals(new TreeMap<>(acl.entryMap()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(owner, new TreeMap<>(entryMap()));
  }

}