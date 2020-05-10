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

package org.bremersee.actuator.security.authentication.resourceserver.servlet.app;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The test configuration.
 *
 * @author Christian Bremer
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = {TestConfiguration.class})
public class TestConfiguration {

  /**
   * The test controller.
   */
  @RestController
  static class TestController {

    /**
     * Public resource.
     *
     * @return the response entity
     */
    @GetMapping(path = "/public")
    public ResponseEntity<String> publicResource() {
      return ResponseEntity.ok("public");
    }

    /**
     * Protected resource.
     *
     * @return the response entity
     */
    @GetMapping(path = "/protected")
    public ResponseEntity<String> protectedResource() {
      return ResponseEntity.ok("protected");
    }

    /**
     * Protected post resource.
     *
     * @param bode the bode
     * @return the response entity
     */
    @PostMapping(path = "/protected",
        consumes = MediaType.TEXT_PLAIN_VALUE,
        produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> protectedPostResource(@RequestBody String bode) {
      return ResponseEntity.ok(bode);
    }
  }

}
