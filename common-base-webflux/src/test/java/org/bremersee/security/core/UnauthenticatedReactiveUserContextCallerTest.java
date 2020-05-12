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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.bremersee.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.ReactorContextTestExecutionListener;
import org.springframework.test.context.TestExecutionListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * The unauthenticated reactive user context test.
 *
 * @author Christian Bremer
 */
class UnauthenticatedReactiveUserContextCallerTest {

  private static final UserContext expected = UserContext.newInstance();

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
    Authentication authentication = mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(false);
    when(authentication.getName()).thenReturn("guest");
    when(authentication.getAuthorities()).then(invocation -> Collections.emptyList());
    TestSecurityContextHolder.setAuthentication(authentication);
    //noinspection ConstantConditions
    reactorContextTestExecutionListener.beforeTestMethod(null);
  }

  /**
   * One with user context.
   */
  @Test
  void oneWithUserContext() {
    StepVerifier
        .create(ReactiveUserContextCaller.oneWithUserContext(
            userContext -> this.serviceMono(userContext, new Object()),
            ReactiveUserContextCaller.EMPTY_GROUPS_SUPPLIER,
            ReactiveUserContextCaller.EMPTY_USER_CONTEXT_SUPPLIER))
        .assertNext(userContext -> {
          assertFalse(userContext.isUserIdPresent());
          assertEquals(expected, userContext);
        })
        .verifyComplete();
  }

  /**
   * One with user context and expect forbidden.
   */
  @Test
  void oneWithUserContextAndExpectForbidden() {
    ReactiveUserContextCaller ctx = new ReactiveUserContextCaller();
    StepVerifier
        .create(ctx.oneWithUserContext(userContext -> this.serviceMono(userContext, new Object())))
        .expectError(ServiceException.class)
        .verify();
  }

  /**
   * Many with user context.
   */
  @Test
  void manyWithUserContext() {
    StepVerifier
        .create(ReactiveUserContextCaller.manyWithUserContext(
            userContext -> this.serviceFlux(userContext, new Object()),
            ReactiveUserContextCaller.EMPTY_GROUPS_SUPPLIER,
            ReactiveUserContextCaller.EMPTY_USER_CONTEXT_SUPPLIER))
        .assertNext(userContext -> {
          assertFalse(userContext.isUserIdPresent());
          assertEquals(expected, userContext);
        })
        .verifyComplete();
  }

  /**
   * Many with user context and expect forbidden.
   */
  @Test
  void manyWithUserContextAndExpectForbidden() {
    ReactiveUserContextCaller ctx = new ReactiveUserContextCaller();
    StepVerifier
        .create(ctx.manyWithUserContext(userContext -> this.serviceFlux(userContext, new Object())))
        .expectError(ServiceException.class)
        .verify();
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