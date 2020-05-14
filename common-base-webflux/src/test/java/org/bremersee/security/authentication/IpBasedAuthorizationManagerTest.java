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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * The ip based authorization manager test.
 *
 * @author Christian Bremer
 */
class IpBasedAuthorizationManagerTest {

  /**
   * Check.
   */
  @Test
  void check() {
    Authentication authentication = mock(Authentication.class);
    when(authentication.isAuthenticated())
        .thenReturn(false);
    when(authentication.getAuthorities())
        .thenAnswer((Answer<Collection<? extends GrantedAuthority>>) invocation -> Collections
            .emptyList());

    InetAddress inetAddress = mock(InetAddress.class);
    when(inetAddress.getHostAddress()).thenReturn("192.168.1.23");
    InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, 80);
    ServerHttpRequest serverHttpRequest = mock(ServerHttpRequest.class);
    when(serverHttpRequest.getRemoteAddress()).thenReturn(inetSocketAddress);
    ServerWebExchange serverWebExchange = mock(ServerWebExchange.class);
    when(serverWebExchange.getRequest()).thenReturn(serverHttpRequest);
    AuthorizationContext authorizationContext = mock(AuthorizationContext.class);
    when(authorizationContext.getExchange()).thenReturn(serverWebExchange);

    IpBasedAuthorizationManager manager = new IpBasedAuthorizationManager(
        Collections.singletonList("192.168.1.0/24"));
    StepVerifier
        .create(manager.check(Mono.just(authentication), authorizationContext))
        .assertNext(decision -> assertTrue(decision.isGranted()))
        .expectNextCount(0)
        .verifyComplete();

    manager = new IpBasedAuthorizationManager(
        Collections.singletonList("192.168.1.0/24"), false);
    StepVerifier
        .create(manager.check(Mono.just(authentication), authorizationContext))
        .assertNext(decision -> assertTrue(decision.isGranted()))
        .expectNextCount(0)
        .verifyComplete();

    manager = new IpBasedAuthorizationManager(
        Collections.singletonList("192.168.2.0/24"));
    StepVerifier
        .create(manager.check(Mono.just(authentication), authorizationContext))
        .assertNext(decision -> assertFalse(decision.isGranted()))
        .expectNextCount(0)
        .verifyComplete();

    manager = new IpBasedAuthorizationManager(
        Collections.singletonList("192.168.2.0/24"), false);
    StepVerifier
        .create(manager.check(Mono.just(authentication), authorizationContext))
        .assertNext(decision -> assertFalse(decision.isGranted()))
        .expectNextCount(0)
        .verifyComplete();
  }

  /**
   * Check with authentication.
   */
  @Test
  void checkWithAuthentication() {
    Authentication authentication = mock(Authentication.class);
    when(authentication.isAuthenticated())
        .thenReturn(true);
    when(authentication.getAuthorities())
        .thenAnswer((Answer<Collection<? extends GrantedAuthority>>) invocation -> Collections
            .emptyList());

    InetAddress inetAddress = mock(InetAddress.class);
    when(inetAddress.getHostAddress()).thenReturn("192.168.1.23");
    InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, 80);
    ServerHttpRequest serverHttpRequest = mock(ServerHttpRequest.class);
    when(serverHttpRequest.getRemoteAddress()).thenReturn(inetSocketAddress);
    ServerWebExchange serverWebExchange = mock(ServerWebExchange.class);
    when(serverWebExchange.getRequest()).thenReturn(serverHttpRequest);
    AuthorizationContext authorizationContext = mock(AuthorizationContext.class);
    when(authorizationContext.getExchange()).thenReturn(serverWebExchange);

    IpBasedAuthorizationManager manager = new IpBasedAuthorizationManager(
        Collections.singletonList("192.168.1.0/24"));
    StepVerifier
        .create(manager.check(Mono.just(authentication), authorizationContext))
        .assertNext(decision -> assertTrue(decision.isGranted()))
        .expectNextCount(0)
        .verifyComplete();

    manager = new IpBasedAuthorizationManager(
        Collections.singletonList("192.168.1.0/24"), false);
    StepVerifier
        .create(manager.check(Mono.just(authentication), authorizationContext))
        .assertNext(decision -> assertTrue(decision.isGranted()))
        .expectNextCount(0)
        .verifyComplete();

    manager = new IpBasedAuthorizationManager(
        Collections.singletonList("192.168.2.0/24"));
    StepVerifier
        .create(manager.check(Mono.just(authentication), authorizationContext))
        .assertNext(decision -> assertTrue(decision.isGranted()))
        .expectNextCount(0)
        .verifyComplete();

    manager = new IpBasedAuthorizationManager(
        Collections.singletonList("192.168.2.0/24"), false);
    StepVerifier
        .create(manager.check(Mono.just(authentication), authorizationContext))
        .assertNext(decision -> assertFalse(decision.isGranted()))
        .expectNextCount(0)
        .verifyComplete();
  }
}