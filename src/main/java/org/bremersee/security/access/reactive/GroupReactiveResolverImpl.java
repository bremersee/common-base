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

import java.util.Set;
import java.util.stream.Collectors;
import org.bremersee.security.access.AccessControlProperties;
import org.bremersee.security.reactive.function.client.JwtAuthenticationTokenAppender;
import org.bremersee.web.ErrorDetectors;
import org.bremersee.web.reactive.function.client.DefaultWebClientErrorDecoder;
import org.bremersee.web.reactive.function.client.WebClientErrorDecoder;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * @author Christian Bremer
 */
public class GroupReactiveResolverImpl implements GroupReactiveResolver {

  private final AccessControlProperties properties;

  private WebClient webClient;

  private WebClientErrorDecoder<? extends Throwable> webClientErrorDecoder;

  public GroupReactiveResolverImpl(final AccessControlProperties properties) {
    this.properties = properties;
  }

  public GroupReactiveResolverImpl(
      final AccessControlProperties properties,
      final WebClient webClient,
      final WebClientErrorDecoder<? extends Throwable> webClientErrorDecoder) {

    this.properties = properties;
    this.webClient = webClient;
    this.webClientErrorDecoder = webClientErrorDecoder;
  }

  protected WebClient getWebClient() {
    if (webClient == null) {
      webClient = WebClient
          .builder()
          .filter(new JwtAuthenticationTokenAppender())
          .build();
    }
    return webClient;
  }

  protected WebClientErrorDecoder<? extends Throwable> getWebClientErrorDecoder() {
    if (webClientErrorDecoder == null) {
      webClientErrorDecoder = new DefaultWebClientErrorDecoder();
    }
    return webClientErrorDecoder;
  }

  @Override
  public Mono<Set<String>> resolveMembership() {
    //noinspection unchecked
    return getWebClient()
        .get()
        .uri(properties.getGroupMembershipUrl())
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .onStatus(ErrorDetectors.DEFAULT, getWebClientErrorDecoder())
        .bodyToMono(Set.class)
        .flatMapIterable(set -> (Set<String>) set)
        .collect(Collectors.toSet());
  }

}
