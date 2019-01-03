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

package org.bremersee.web.reactive.function.client;

import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

/**
 * An abstract implementation of the error decoder for the {@link org.springframework.web.reactive.function.client.WebClient}.
 *
 * @author Christian Bremer
 */
public abstract class AbstractWebClientErrorDecoder<E extends Throwable>
    implements WebClientErrorDecoder<E> {

  @Override
  public Mono<? extends Throwable> apply(ClientResponse clientResponse) {
    return clientResponse
        .bodyToMono(String.class)
        .switchIfEmpty(Mono.just(""))
        .map(response -> buildException(clientResponse, response));
  }

}
