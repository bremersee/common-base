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

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.bremersee.exception.ServiceException;
import org.reactivestreams.Publisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The reactive user context.
 *
 * @author Christian Bremer
 */
public class ReactiveUserContext {

  /**
   * The constant EMPTY_GROUPS_SUPPLIER.
   */
  public static final Supplier<Mono<Set<String>>> EMPTY_GROUPS_SUPPLIER = Mono::empty;

  /**
   * The constant EMPTY_USER_CONTEXT_SUPPLIER.
   */
  public static final Supplier<Mono<UserContext>> EMPTY_USER_CONTEXT_SUPPLIER = () -> Mono
      .just(new UserContext());

  /**
   * The constant FORBIDDEN_SUPPLIER.
   */
  public static final Supplier<Mono<UserContext>> FORBIDDEN_SUPPLIER = () -> Mono
      .error(ServiceException::forbidden);

  private final Function<Authentication, Mono<Set<String>>> groupsFn;

  private final Supplier<Mono<UserContext>> unauthenticatedSupplier;

  /**
   * Instantiates a new reactive user context.
   */
  public ReactiveUserContext() {
    this(EMPTY_GROUPS_SUPPLIER, null);
  }

  /**
   * Instantiates a new reactive user context.
   *
   * @param groupsSupplier the groups supplier
   * @param unauthenticatedSupplier the unauthenticated supplier
   */
  public ReactiveUserContext(
      Supplier<Mono<Set<String>>> groupsSupplier,
      Supplier<Mono<UserContext>> unauthenticatedSupplier) {
    this(
        groupsSupplier != null
            ? authentication -> groupsSupplier.get()
            : authentication -> EMPTY_GROUPS_SUPPLIER.get(),
        unauthenticatedSupplier);
  }

  /**
   * Instantiates a new reactive user context.
   *
   * @param groupsFn the groups fn
   * @param unauthenticatedSupplier the unauthenticated supplier
   */
  public ReactiveUserContext(
      Function<Authentication, Mono<Set<String>>> groupsFn,
      Supplier<Mono<UserContext>> unauthenticatedSupplier) {
    this.groupsFn = groupsFn != null ? groupsFn : authentication -> EMPTY_GROUPS_SUPPLIER.get();
    this.unauthenticatedSupplier = unauthenticatedSupplier != null
        ? unauthenticatedSupplier
        : FORBIDDEN_SUPPLIER;
  }

  private Set<String> toRoles(Authentication authentication) {
    return authentication.getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());
  }

  /**
   * One with user context mono.
   *
   * @param <R> the type parameter
   * @param function the function
   * @return the mono
   */
  public <R> Mono<R> oneWithUserContext(Function<UserContext, ? extends Mono<R>> function) {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .filter(Authentication::isAuthenticated)
        .zipWhen(groupsFn::apply)
        .map(tuple -> new UserContext(
            tuple.getT1().getName(),
            toRoles(tuple.getT1()),
            tuple.getT2()))
        .switchIfEmpty(unauthenticatedSupplier.get())
        .flatMap(function);
  }

  /**
   * One with user context mono.
   *
   * @param <R> the type parameter
   * @param function the function
   * @param groupsFn the groups fn
   * @param unauthenticatedSupplier the unauthenticated supplier
   * @return the mono
   */
  public static <R> Mono<R> oneWithUserContext(
      Function<UserContext, ? extends Mono<R>> function,
      Function<Authentication, Mono<Set<String>>> groupsFn,
      Supplier<Mono<UserContext>> unauthenticatedSupplier) {
    return new ReactiveUserContext(groupsFn, unauthenticatedSupplier)
        .oneWithUserContext(function);
  }

  /**
   * One with user context mono.
   *
   * @param <R> the type parameter
   * @param function the function
   * @param groupsSupplier the groups supplier
   * @param unauthenticatedSupplier the unauthenticated supplier
   * @return the mono
   */
  public static <R> Mono<R> oneWithUserContext(
      Function<UserContext, ? extends Mono<R>> function,
      Supplier<Mono<Set<String>>> groupsSupplier,
      Supplier<Mono<UserContext>> unauthenticatedSupplier) {
    return new ReactiveUserContext(groupsSupplier, unauthenticatedSupplier)
        .oneWithUserContext(function);
  }

  /**
   * Many with user context flux.
   *
   * @param <R> the type parameter
   * @param function the function
   * @return the flux
   */
  public <R> Flux<R> manyWithUserContext(Function<UserContext, ? extends Publisher<R>> function) {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .filter(Authentication::isAuthenticated)
        .zipWhen(groupsFn::apply)
        .map(tuple -> new UserContext(
            tuple.getT1().getName(),
            toRoles(tuple.getT1()),
            tuple.getT2()))
        .switchIfEmpty(unauthenticatedSupplier.get())
        .flatMapMany(function);
  }

  /**
   * Many with user context flux.
   *
   * @param <R> the type parameter
   * @param function the function
   * @param groupsFn the groups fn
   * @param unauthenticatedSupplier the unauthenticated supplier
   * @return the flux
   */
  public static <R> Flux<R> manyWithUserContext(
      Function<UserContext, ? extends Publisher<R>> function,
      Function<Authentication, Mono<Set<String>>> groupsFn,
      Supplier<Mono<UserContext>> unauthenticatedSupplier) {
    return new ReactiveUserContext(groupsFn, unauthenticatedSupplier)
        .manyWithUserContext(function);
  }

  /**
   * Many with user context flux.
   *
   * @param <R> the type parameter
   * @param function the function
   * @param groupsSupplier the groups supplier
   * @param unauthenticatedSupplier the unauthenticated supplier
   * @return the flux
   */
  public static <R> Flux<R> manyWithUserContext(
      Function<UserContext, ? extends Publisher<R>> function,
      Supplier<Mono<Set<String>>> groupsSupplier,
      Supplier<Mono<UserContext>> unauthenticatedSupplier) {
    return new ReactiveUserContext(groupsSupplier, unauthenticatedSupplier)
        .manyWithUserContext(function);
  }

}
