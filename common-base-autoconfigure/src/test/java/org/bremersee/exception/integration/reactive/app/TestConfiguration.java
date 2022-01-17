/*
 * Copyright 2020-2022 the original author or authors.
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

package org.bremersee.exception.integration.reactive.app;

import org.bremersee.exception.ServiceException;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * The test configuration.
 *
 * @author Christian Bremer
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
    RedisAutoConfiguration.class,
    RedisReactiveAutoConfiguration.class
})
@ComponentScan(basePackageClasses = {TestConfiguration.class})
public class TestConfiguration {

  /**
   * The test controller.
   */
  @RestController
  static class TestController {

    /**
     * Throws an exception.
     *
     * @return the mono
     */
    @GetMapping(path = "/test/exception")
    public Mono<String> exception() {
      ServiceException exception = ServiceException.badRequest("Second",
          ServiceException.badRequest("First", "TEST:4711"));
      return Mono.error(exception);
    }
  }

}
