/*
 * Copyright 2017 the original author or authors.
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

import java.util.function.Function;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

/**
 * @author Christian Bremer
 */
public abstract class ExceptionCreator<E extends Throwable>
    implements Function<ClientResponse, Mono<? extends Throwable>> {

  @Override
  public Mono<? extends Throwable> apply(ClientResponse clientResponse) {
    return clientResponse
        .bodyToMono(String.class)
        .map(body -> createException(clientResponse.statusCode(), body));
  }

  protected abstract E createException(HttpStatus httpStatus, String body);

  public static class Default extends ExceptionCreator<ClientResponseException> {

    @Override
    protected ClientResponseException createException(HttpStatus httpStatus, String body) {
      return new ClientResponseException(httpStatus, body);
    }
  }

}
