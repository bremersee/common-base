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

package org.bremersee.security.authentication;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * The role based authorization manager test.
 *
 * @author Christian Bremer
 */
class RoleBasedAuthorizationManagerTest {

  /**
   * Check.
   */
  @Test
  void check() {
    Authentication authentication = mock(Authentication.class);
    when(authentication.isAuthenticated())
        .thenReturn(true);
    when(authentication.getAuthorities())
        .thenAnswer((Answer<Collection<? extends GrantedAuthority>>) invocation -> Collections
            .singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
    AuthorizationContext authorizationContext = mock(AuthorizationContext.class);
    RoleBasedAuthorizationManager manager = new RoleBasedAuthorizationManager(
        Arrays.asList("ROLE_USER", "ADMIN"));
    StepVerifier
        .create(manager.check(Mono.just(authentication), authorizationContext))
        .assertNext(decision -> assertTrue(decision.isGranted()))
        .expectNextCount(0)
        .verifyComplete();

    manager = new RoleBasedAuthorizationManager(
        Arrays.asList("ROLE_USER", "ROLE_LOCAL_USER"));
    StepVerifier
        .create(manager.check(Mono.just(authentication), authorizationContext))
        .assertNext(decision -> assertFalse(decision.isGranted()))
        .expectNextCount(0)
        .verifyComplete();
  }

}