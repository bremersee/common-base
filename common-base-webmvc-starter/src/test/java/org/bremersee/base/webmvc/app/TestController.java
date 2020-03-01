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

package org.bremersee.base.webmvc.app;

import org.bremersee.exception.ServiceException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The test controller.
 *
 * @author Christian Bremer
 */
@RestController
public class TestController {

  /**
   * Current user name.
   *
   * @return the current user name
   */
  @GetMapping(path = "/api/name", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> currentUserName() {
    return ResponseEntity.ok(SecurityContextHolder.getContext().getAuthentication().getName());
  }

  /**
   * Current admin name.
   *
   * @return the name of the admin user
   */
  @GetMapping(path = "/api/admin/name", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> currentAdminName() {
    return ResponseEntity.ok(SecurityContextHolder.getContext().getAuthentication().getName());
  }

  /**
   * Throws an exception.
   *
   * @return nothing - throws always an exception
   */
  @GetMapping(path = "/api/exception", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> exception() {
    throw ServiceException.badRequest(
        "A reason",
        "TEST:4711",
        new Exception("A cause message"));
  }

}
