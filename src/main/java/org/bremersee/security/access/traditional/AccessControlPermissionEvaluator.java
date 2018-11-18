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

package org.bremersee.security.access.traditional;

import java.io.Serializable;
import java.util.Set;
import org.bremersee.security.access.AccessControl;
import org.bremersee.security.access.AuthorizationSet;
import org.bremersee.security.access.DomainObjectReference;
import org.bremersee.security.access.DomainObjectReferenceCreator;
import org.bremersee.security.access.DomainObjectReferenceCreatorImpl;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author Christian Bremer
 */
public class AccessControlPermissionEvaluator implements PermissionEvaluator {

  private DomainObjectReferenceCreator domainObjectReferenceCreator
      = new DomainObjectReferenceCreatorImpl();

  private AccessControlDao accessControlDao;

  private GroupMemberResolver groupMemberResolver;

  public AccessControlPermissionEvaluator(
      final AccessControlDao accessControlDao,
      final GroupMemberResolver groupMemberResolver) {
    this.accessControlDao = accessControlDao;
    this.groupMemberResolver = groupMemberResolver;
  }

  public void setDomainObjectReferenceCreator(
      DomainObjectReferenceCreator domainObjectReferenceCreator) {
    if (domainObjectReferenceCreator != null) {
      this.domainObjectReferenceCreator = domainObjectReferenceCreator;
    }
  }

  @Override
  public boolean hasPermission(
      final Authentication authentication,
      final Object targetDomainObject,
      final Object permission) {

    if (targetDomainObject instanceof DomainObjectReference) {
      final DomainObjectReference ref = (DomainObjectReference) targetDomainObject;
      return hasPermission(authentication, ref.getId(), ref.getType());
    }
    return domainObjectReferenceCreator
        .createReference(domainObjectReferenceCreator)
        .map(ref -> hasPermission(authentication, ref.getId(), ref.getType()))
        .orElse(Boolean.FALSE);
  }

  @Override
  public boolean hasPermission(
      final Authentication authentication,
      final Serializable targetId,
      final String targetType,
      final Object permission) {

    if (targetId == null || targetType == null) {
      return false;
    }
    return accessControlDao
        .findAccessControl(targetId, targetType)
        .map(ac -> isAuthorized(authentication, ac, permission))
        .orElse(Boolean.FALSE);
  }

  private boolean isAuthorized(
      final Authentication authentication,
      final AccessControl ac,
      final Object permission) {

    return (authentication != null
        && authentication.getName() != null
        && authentication.getName().equals(ac.getOwner()))
        || ac.findAuthorizationSet(permission)
        .map(as -> isAuthorized(authentication, as))
        .orElse(Boolean.FALSE);
  }

  private boolean isAuthorized(
      final Authentication authentication,
      final AuthorizationSet as) {

    if (as.isGuest()) {
      return true;
    }
    if (authentication == null || authentication.getName() == null) {
      return false;
    }
    return as.getUsers().contains(authentication.getName())
        || hasRole(authentication, as.getRoles())
        || isGroupMember(authentication, as.getGroups());
  }

  private boolean hasRole(final Authentication authentication, final Set<String> roles) {
    if (authentication == null || authentication.getAuthorities() == null || roles == null) {
      return false;
    }
    return authentication.getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch(roles::contains);
  }

  private boolean isGroupMember(final Authentication authentication, final Set<String> groups) {
    if (authentication == null || authentication.getName() == null || groups == null) {
      return false;
    }
    return groupMemberResolver.resolveMembership().stream().anyMatch(groups::contains);
  }

}
