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

package org.bremersee.security.access.reactive;

import java.io.Serializable;
import java.util.Set;
import org.bremersee.security.access.AccessControl;
import org.bremersee.security.access.AuthorizationSet;
import org.bremersee.security.access.DomainObjectReference;
import org.bremersee.security.access.DomainObjectReferenceCreator;
import org.bremersee.security.access.DomainObjectReferenceCreatorImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import reactor.core.publisher.Mono;

/**
 * @author Christian Bremer
 */
public class AccessControlReactivePermissionEvaluator implements ReactivePermissionEvaluator {

  private DomainObjectReferenceReactiveCreator domainObjectReferenceCreator
      = new DomainObjectReferenceReactiveCreatorWrapper(new DomainObjectReferenceCreatorImpl());

  private AccessControlReactiveDao accessControlDao;

  private GroupMemberReactiveResolver groupMemberResolver;

  public AccessControlReactivePermissionEvaluator(
      final AccessControlReactiveDao accessControlDao,
      final GroupMemberReactiveResolver groupMemberResolver) {
    this.accessControlDao = accessControlDao;
    this.groupMemberResolver = groupMemberResolver;
  }

  public void setDomainObjectReferenceCreator(
      DomainObjectReferenceCreator domainObjectReferenceCreator) {
    if (domainObjectReferenceCreator != null) {
      this.domainObjectReferenceCreator = new DomainObjectReferenceReactiveCreatorWrapper(
          domainObjectReferenceCreator);
    }
  }

  public void setDomainObjectReferenceCreator(
      DomainObjectReferenceReactiveCreator domainObjectReferenceCreator) {
    if (domainObjectReferenceCreator != null) {
      this.domainObjectReferenceCreator = domainObjectReferenceCreator;
    }
  }

  @Override
  public Mono<Boolean> hasPermission(
      final Authentication authentication,
      final Object targetDomainObject,
      final Object permission) {

    if (targetDomainObject instanceof DomainObjectReference) {
      final DomainObjectReference ref = (DomainObjectReference) targetDomainObject;
      return hasPermission(authentication, ref.getId(), ref.getType());
    }
    return domainObjectReferenceCreator
        .createReference(domainObjectReferenceCreator)
        .flatMap(ref -> hasPermission(authentication, ref.getId(), ref.getType(), permission))
        .defaultIfEmpty(Boolean.FALSE);
  }

  @Override
  public Mono<Boolean> hasPermission(
      final Authentication authentication,
      final Serializable targetId,
      final String targetType,
      final Object permission) {

    if (targetId == null || targetType == null) {
      return Mono.just(Boolean.FALSE);
    }
    return accessControlDao
        .findAccessControl(targetId, targetType)
        .flatMap(ac -> isAuthorized(authentication, ac, permission))
        .defaultIfEmpty(Boolean.FALSE);
  }

  private Mono<Boolean> isAuthorized(
      final Authentication authentication,
      final AccessControl ac,
      final Object permission) {

    if (ac == null) {
      return Mono.just(Boolean.FALSE);
    }
    if (authentication != null
        && authentication.getName() != null
        && authentication.getName().equals(ac.getOwner())) {
      return Mono.just(Boolean.TRUE);
    }
    return ac.findAuthorizationSet(permission)
        .map(authorizationSet -> isAuthorized(authentication, authorizationSet))
        .orElseGet(() -> Mono.just(Boolean.FALSE));
  }

  private Mono<Boolean> isAuthorized(
      final Authentication authentication,
      final AuthorizationSet as) {

    if (as.isGuest()) {
      return Mono.just(Boolean.TRUE);
    }
    if (authentication == null || authentication.getName() == null) {
      return Mono.just(Boolean.FALSE);
    }
    if (as.getUsers().contains(authentication.getName()) || hasRole(authentication,
        as.getRoles())) {
      return Mono.just(Boolean.TRUE);
    }
    return isGroupMember(authentication, as.getGroups());
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

  private Mono<Boolean> isGroupMember(
      final Authentication authentication,
      final Set<String> groups) {

    if (authentication == null || authentication.getName() == null || groups == null) {
      return Mono.just(Boolean.FALSE);
    }
    return groupMemberResolver
        .resolveMembership()
        .flatMapIterable(allGroups -> allGroups)
        .any(groups::contains)
        .switchIfEmpty(Mono.just(Boolean.FALSE));
  }

}
