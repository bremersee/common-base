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

package org.bremersee.security.core;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.validation.constraints.NotNull;
import org.bremersee.exception.ServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;

/**
 * The user context caller.
 *
 * @author Christian Bremer
 */
@Validated
public class UserContextCaller {

  /**
   * The constant EMPTY_GROUPS_SUPPLIER.
   */
  public static final Supplier<Set<String>> EMPTY_GROUPS_SUPPLIER = Collections::emptySet;

  /**
   * The constant FORBIDDEN_SUPPLIER.
   */
  public static final Supplier<ServiceException> FORBIDDEN_SUPPLIER = ServiceException::forbidden;

  private final Function<Authentication, Set<String>> groupsFn;

  private final Supplier<ServiceException> unauthenticatedExceptionSupplier;

  /**
   * Instantiates a new user context caller.
   */
  public UserContextCaller() {
    this.groupsFn = authentication -> Collections.emptySet();
    this.unauthenticatedExceptionSupplier = FORBIDDEN_SUPPLIER;
  }

  /**
   * Instantiates a new user context caller.
   *
   * @param groupsSupplier the groups supplier
   */
  public UserContextCaller(@Nullable Supplier<Set<String>> groupsSupplier) {
    this(groupsSupplier, FORBIDDEN_SUPPLIER);
  }

  /**
   * Instantiates a new user context caller.
   *
   * @param groupsFn the groups fn
   */
  public UserContextCaller(@Nullable Function<Authentication, Set<String>> groupsFn) {
    this(groupsFn, FORBIDDEN_SUPPLIER);
  }

  /**
   * Instantiates a new user context caller.
   *
   * @param groupsSupplier the groups supplier
   * @param unauthenticatedExceptionSupplier the unauthenticated exception supplier
   */
  public UserContextCaller(
      @Nullable Supplier<Set<String>> groupsSupplier,
      @Nullable Supplier<ServiceException> unauthenticatedExceptionSupplier) {
    this.groupsFn = groupsSupplier != null
        ? authentication -> groupsSupplier.get()
        : authentication -> Collections.emptySet();
    this.unauthenticatedExceptionSupplier = unauthenticatedExceptionSupplier != null
        ? unauthenticatedExceptionSupplier
        : FORBIDDEN_SUPPLIER;
  }

  /**
   * Instantiates a new user context caller.
   *
   * @param groupsFn the groups fn
   * @param unauthenticatedExceptionSupplier the unauthenticated exception supplier
   */
  public UserContextCaller(
      @Nullable Function<Authentication, Set<String>> groupsFn,
      @Nullable Supplier<ServiceException> unauthenticatedExceptionSupplier) {
    this.groupsFn = groupsFn != null ? groupsFn : authentication -> Collections.emptySet();
    this.unauthenticatedExceptionSupplier = unauthenticatedExceptionSupplier != null
        ? unauthenticatedExceptionSupplier
        : FORBIDDEN_SUPPLIER;
  }

  /**
   * Call with required user context.
   *
   * @param <R> the type parameter
   * @param function the function
   * @return the response
   */
  public <R> R callWithRequiredUserContext(@NotNull Function<UserContext, R> function) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw unauthenticatedExceptionSupplier.get();
    }
    return function.apply(UserContext.newInstance(authentication, groupsFn.apply(authentication)));
  }

  /**
   * Call with optional user context.
   *
   * @param <R> the type parameter
   * @param function the function
   * @return the response
   */
  public <R> R callWithOptionalUserContext(@NotNull Function<UserContext, R> function) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return function.apply(UserContext.newInstance());
    }
    return function.apply(UserContext.newInstance(authentication, groupsFn.apply(authentication)));
  }

  /**
   * Response with required user context response entity.
   *
   * @param <R> the type parameter
   * @param <E> the type parameter
   * @param function the function
   * @return the response entity
   */
  public <R, E> ResponseEntity<E> responseWithRequiredUserContext(
      @NotNull Function<UserContext, R> function) {
    return toResponseEntity(callWithRequiredUserContext(function));
  }

  /**
   * Response with optional user context response entity.
   *
   * @param <R> the type parameter
   * @param <E> the type parameter
   * @param function the function
   * @return the response entity
   */
  public <R, E> ResponseEntity<E> responseWithOptionalUserContext(
      @NotNull Function<UserContext, R> function) {
    return toResponseEntity(callWithOptionalUserContext(function));
  }

  /**
   * To response entity response entity.
   *
   * @param <R> the type parameter
   * @param <E> the type parameter
   * @param result the result
   * @return the response entity
   */
  @SuppressWarnings("unchecked")
  protected <R, E> ResponseEntity<E> toResponseEntity(R result) {
    if (result == null) {
      return ResponseEntity.notFound().build();
    } else if (result instanceof Optional) {
      return ResponseEntity.of((Optional<E>) result);
    } else {
      return (ResponseEntity<E>) ResponseEntity.ok(result);
    }
  }

}
