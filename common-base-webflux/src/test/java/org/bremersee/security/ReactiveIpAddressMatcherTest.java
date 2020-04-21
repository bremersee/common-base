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

package org.bremersee.security;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.test.StepVerifier;

/**
 * The reactive ip address matcher test.
 *
 * @author Christian Bremer
 */
class ReactiveIpAddressMatcherTest {

  /**
   * The server web exchange.
   */
  ServerWebExchange serverWebExchange;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    InetAddress inetAddress = mock(InetAddress.class);
    when(inetAddress.getHostAddress()).thenReturn("192.168.1.23");
    InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, 80);
    ServerHttpRequest serverHttpRequest = mock(ServerHttpRequest.class);
    when(serverHttpRequest.getRemoteAddress()).thenReturn(inetSocketAddress);
    serverWebExchange = mock(ServerWebExchange.class);
    when(serverWebExchange.getRequest()).thenReturn(serverHttpRequest);
  }

  /**
   * Matches remote address.
   */
  @Test
  void matchesRemoteAddress() {
    ReactiveIpAddressMatcher matcher = new ReactiveIpAddressMatcher("192.168.1.23");
    assertTrue(matcher.matchesRemoteAddress(serverWebExchange));
  }

  /**
   * Matches.
   */
  @Test
  void matches() {
    ReactiveIpAddressMatcher matcher = new ReactiveIpAddressMatcher("192.168.1.23");
    StepVerifier.create(matcher.matches(serverWebExchange))
        .assertNext(matchResult -> assertTrue(matchResult.isMatch()))
        .expectNextCount(0)
        .verifyComplete();
  }
}