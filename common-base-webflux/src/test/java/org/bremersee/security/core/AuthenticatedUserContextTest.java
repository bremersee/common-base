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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.ReactorContextTestExecutionListener;
import org.springframework.test.context.TestExecutionListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * The authenticated user context test.
 *
 * @author Christian Bremer
 */
class AuthenticatedUserContextTest {

  private static final String userId = UUID.randomUUID().toString();

  private static final String role = UUID.randomUUID().toString();

  private static final String group = UUID.randomUUID().toString();

  private static final UserContext expected = UserContext.newInstance(
      userId, Collections.singleton(role), Collections.singleton(group));

  private final TestExecutionListener reactorContextTestExecutionListener =
      new ReactorContextTestExecutionListener();

  /**
   * Sets up.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  void setUp() throws Exception {
    TestSecurityContextHolder.clearContext();
    Collection<GrantedAuthority> roles = Collections.singleton(new SimpleGrantedAuthority(role));
    Authentication authentication = mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getName()).thenReturn(userId);
    when(authentication.getAuthorities()).then(invocation -> roles);
    TestSecurityContextHolder.setAuthentication(authentication);
    //noinspection ConstantConditions
    reactorContextTestExecutionListener.beforeTestMethod(null);
  }

  /**
   * One with user context and groups supplier.
   */
  @Test
  void oneWithUserContextAndGroupsSupplier() {
    StepVerifier
        .create(ReactiveUserContext.oneWithUserContext(
            userContext -> this.serviceMono(userContext, new Object()),
            () -> Mono.just(Collections.singleton(group)),
            ReactiveUserContext.FORBIDDEN_SUPPLIER))
        .assertNext(userContext -> assertEquals(expected, userContext))
        .verifyComplete();
  }

  /**
   * One with user context and groups function.
   */
  @Test
  void oneWithUserContextAndGroupsFunction() {
    StepVerifier
        .create(ReactiveUserContext.oneWithUserContext(
            userContext -> this.serviceMono(userContext, new Object()),
            auth -> Mono.just(Collections.singleton(group)),
            ReactiveUserContext.FORBIDDEN_SUPPLIER))
        .assertNext(userContext -> assertEquals(expected, userContext))
        .verifyComplete();
  }

  /**
   * Many with user context and groups supplier.
   */
  @Test
  void manyWithUserContextAndGroupsSupplier() {
    StepVerifier
        .create(ReactiveUserContext.manyWithUserContext(
            userContext -> this.serviceFlux(userContext, new Object()),
            () -> Mono.just(Collections.singleton(group)),
            ReactiveUserContext.FORBIDDEN_SUPPLIER))
        .assertNext(userContext -> assertEquals(expected, userContext))
        .verifyComplete();
  }

  /**
   * Many with user context and groups function.
   */
  @Test
  void manyWithUserContextAndGroupsFunction() {
    StepVerifier
        .create(ReactiveUserContext.manyWithUserContext(
            userContext -> this.serviceFlux(userContext, new Object()),
            auth -> Mono.just(Collections.singleton(group)),
            ReactiveUserContext.FORBIDDEN_SUPPLIER))
        .assertNext(userContext -> assertEquals(expected, userContext))
        .verifyComplete();
  }

  private Mono<UserContext> serviceMono(UserContext userContext, Object arg) {
    assertNotNull(arg);
    return Mono.just(userContext);
  }

  private Flux<UserContext> serviceFlux(UserContext userContext, Object arg) {
    assertNotNull(arg);
    return Flux.fromArray(new UserContext[]{userContext});
  }
}