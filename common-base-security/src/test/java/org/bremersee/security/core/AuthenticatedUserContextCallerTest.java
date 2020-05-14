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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

/**
 * The authenticated user context caller test.
 *
 * @author Christian Bremer
 */
class AuthenticatedUserContextCallerTest {

  private static final String userId = UUID.randomUUID().toString();

  private static final String role = UUID.randomUUID().toString();

  private static final String group = UUID.randomUUID().toString();

  private static final UserContext expected = UserContext.newInstance(
      userId, Collections.singleton(role), Collections.singleton(group));

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
    Collection<GrantedAuthority> roles = Collections.singleton(new SimpleGrantedAuthority(role));
    Authentication authentication = Mockito.mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getName()).thenReturn(userId);
    when(authentication.getAuthorities()).then(invocation -> roles);
    SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
  }

  /**
   * Call with required user context.
   */
  @Test
  void callWithRequiredUserContext() {
    UserContextCaller caller = new UserContextCaller(
        () -> Collections.singleton(group),
        UserContextCaller.FORBIDDEN_SUPPLIER);
    UserContext actual = caller
        .callWithRequiredUserContext(userContext -> serviceMethod(userContext, new Object()));
    assertNotNull(actual);
    assertEquals(expected, actual);

    caller = new UserContextCaller(
        auth -> Collections.singleton(group),
        UserContextCaller.FORBIDDEN_SUPPLIER);
    Optional<UserContext> optActual = caller
        .callWithRequiredUserContext(userContext -> optServiceMethod(userContext, new Object()));
    assertNotNull(optActual);
    assertTrue(optActual.isPresent());
    assertEquals(expected, optActual.get());
  }

  /**
   * Call with optional user context.
   */
  @Test
  void callWithOptionalUserContext() {
    UserContextCaller caller = new UserContextCaller(() -> Collections.singleton(group));
    UserContext actual = caller
        .callWithOptionalUserContext(userContext -> serviceMethod(userContext, new Object()));
    assertNotNull(actual);
    assertEquals(expected, actual);

    caller = new UserContextCaller(auth -> Collections.singleton(group));
    Optional<UserContext> optActual = caller
        .callWithOptionalUserContext(userContext -> optServiceMethod(userContext, new Object()));
    assertNotNull(optActual);
    assertTrue(optActual.isPresent());
    assertEquals(expected, optActual.get());
  }

  /**
   * Response with required user context.
   */
  @Test
  void responseWithRequiredUserContext() {
    UserContextCaller caller = new UserContextCaller(() -> Collections.singleton(group), null);
    ResponseEntity<UserContext> response = caller
        .responseWithRequiredUserContext(userContext -> serviceMethod(userContext, new Object()));
    assertNotNull(response);
    UserContext actual = response.getBody();
    assertNotNull(actual);
    assertEquals(expected, actual);

    caller = new UserContextCaller(auth -> Collections.singleton(group), null);
    response = caller
        .responseWithRequiredUserContext(
            userContext -> optServiceMethod(userContext, new Object()));
    assertNotNull(response);
    actual = response.getBody();
    assertNotNull(actual);
    assertEquals(expected, actual);
  }

  /**
   * Response with optional user context.
   */
  @Test
  void responseWithOptionalUserContext() {
    UserContextCaller caller = new UserContextCaller(() -> Collections.singleton(group), null);
    ResponseEntity<UserContext> response = caller
        .responseWithOptionalUserContext(userContext -> serviceMethod(userContext, new Object()));
    assertNotNull(response);
    UserContext actual = response.getBody();
    assertNotNull(actual);
    assertEquals(expected, actual);

    caller = new UserContextCaller(auth -> Collections.singleton(group), null);
    response = caller
        .responseWithOptionalUserContext(
            userContext -> optServiceMethod(userContext, new Object()));
    assertNotNull(response);
    actual = response.getBody();
    assertNotNull(actual);
    assertEquals(expected, actual);
  }

  private UserContext serviceMethod(UserContext userContext, Object arg) {
    assertNotNull(arg);
    return userContext;
  }

  private Optional<UserContext> optServiceMethod(UserContext userContext, Object arg) {
    assertNotNull(arg);
    return Optional.of(userContext);
  }

}