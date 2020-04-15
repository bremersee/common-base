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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * The type Reactive ip address matcher.
 *
 * @author Christian Bremer
 */
public class ReactiveIpAddressMatcher extends IpAddressMatcher implements ServerWebExchangeMatcher {

  /**
   * Takes a specific IP address or a range specified using the IP/Netmask (e.g. 192.168.1.0/24 or
   * 202.24.0.0/14).
   *
   * @param ipAddress the address or range of addresses from which the request must come.
   */
  public ReactiveIpAddressMatcher(String ipAddress) {
    super(ipAddress);
  }

  /**
   * Checks whether the remote address of the request matches the ip of this matcher.
   *
   * @param exchange the exchange
   * @return {@code true} if the remote adaress matches, otherwise {@code false}
   */
  public boolean matchesRemoteAddress(ServerWebExchange exchange) {
    return Optional.ofNullable(exchange)
        .map(ServerWebExchange::getRequest)
        .map(ServerHttpRequest::getRemoteAddress)
        .map(InetSocketAddress::getAddress)
        .map(InetAddress::getHostAddress)
        .map(this::matches)
        .orElse(false);
  }

  @Override
  public Mono<MatchResult> matches(ServerWebExchange exchange) {
    return matchesRemoteAddress(exchange) ? MatchResult.match() : MatchResult.notMatch();
  }

}
