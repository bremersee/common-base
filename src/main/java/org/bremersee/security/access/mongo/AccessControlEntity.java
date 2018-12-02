/*
 * Copyright 2018 the original author or authors.
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

package org.bremersee.security.access.mongo;

import java.util.Optional;
import org.bremersee.security.access.AuthorizationSet;
import org.bremersee.security.access.DefaultAuthorizationSet;
import org.bremersee.security.access.MutableAccessControl;
import org.bremersee.security.access.PermissionConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Christian Bremer
 */
@Document(collection = "access-control")
@TypeAlias("AccessControl")
@CompoundIndexes({
    @CompoundIndex(name = "target",
        def = "{'targetId': 1, 'targetType': 1 }",
        unique = true)
})
public class AccessControlEntity implements MutableAccessControl {

  @Id
  private String id;

  @Version
  private Long version;

  private String targetId;

  private String targetType;

  private String owner;

  private DefaultAuthorizationSet administration = new DefaultAuthorizationSet();

  private DefaultAuthorizationSet create = new DefaultAuthorizationSet();

  private DefaultAuthorizationSet delete = new DefaultAuthorizationSet();

  private DefaultAuthorizationSet read = new DefaultAuthorizationSet();

  private DefaultAuthorizationSet write = new DefaultAuthorizationSet();

  public AccessControlEntity() {
  }

  public AccessControlEntity(String targetId, String targetType, String owner) {
    this.targetId = targetId;
    this.targetType = targetType;
    this.owner = owner;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public String getTargetId() {
    return targetId;
  }

  public void setTargetId(String targetId) {
    this.targetId = targetId;
  }

  public String getTargetType() {
    return targetType;
  }

  public void setTargetType(String targetType) {
    this.targetType = targetType;
  }

  @Override
  public String getOwner() {
    return null;
  }

  @Override
  public void setOwner(String owner) {
    this.owner = owner;
  }

  public DefaultAuthorizationSet getAdministration() {
    return administration;
  }

  public DefaultAuthorizationSet getCreate() {
    return create;
  }

  public DefaultAuthorizationSet getDelete() {
    return delete;
  }

  public DefaultAuthorizationSet getRead() {
    return read;
  }

  public DefaultAuthorizationSet getWrite() {
    return write;
  }

  @Override
  public Optional<AuthorizationSet> findAuthorizationSet(Object permission) {
    final String permissionStr = String.valueOf(permission);
    switch (permissionStr) {
      case PermissionConstants.ADMINISTRATION:
        return Optional.of(administration);
      case PermissionConstants.CREATE:
        return Optional.of(create);
      case PermissionConstants.DELETE:
        return Optional.of(delete);
      case PermissionConstants.READ:
        return Optional.of(read);
      case PermissionConstants.WRITE:
        return Optional.of(write);
      default:
        return Optional.empty();
    }
  }

}
