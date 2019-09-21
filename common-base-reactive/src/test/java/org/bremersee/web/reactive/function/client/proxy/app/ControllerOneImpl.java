/*
 * Copyright 2019 the original author or authors.
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

package org.bremersee.web.reactive.function.client.proxy.app;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.bremersee.exception.ServiceException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Christian Bremer
 */
@RestController
public class ControllerOneImpl implements ControllerOne {

  @Override
  public Mono<String> simpleGet() {
    return Mono.just(OK_RESPONSE);
  }

  @Override
  public Flux<Map<String, Object>> getOks() {
    Map<String, Object> ok0 = new LinkedHashMap<>();
    ok0.put("value", "OK_0");
    Map<String, Object> ok1 = new LinkedHashMap<>();
    ok1.put("value", "OK_1");
    Map<String, Object> ok2 = new LinkedHashMap<>();
    ok2.put("value", "OK_2");
    return Flux.fromStream(Stream.of(ok0, ok1, ok2));
  }

  @Override
  public Mono<String> addOk(MultiValueMap<String, String> form) {
    return form != null && !form.isEmpty() ? Mono.just(OK_RESPONSE) : Mono.just("FAILED");
  }

  @Override
  public Mono<String> updateOk(String name, String payload) {
    return Mono.just(name + "=" + payload);
  }

  @Override
  public Mono<Void> patchOk(String name, String suffix, String payload) {
    if ("exception".equalsIgnoreCase(suffix)) {
      throw ServiceException.badRequest("'exception' is an illegal suffix");
    }
    return Mono.empty();
  }

  @Override
  public Mono<Boolean> deleteOk(String name) {
    return Mono.just(true);
  }

  @Override
  public Mono<Map<String, Object>> upload(
      String xHeaderValue,
      String lastValue,
      MultiValueMap<String, ?> data) {

    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    map.put("x-ok-flag", xHeaderValue);
    map.put("last", lastValue);
    map.putAll(data);
    return Mono.just(map);
  }
}
