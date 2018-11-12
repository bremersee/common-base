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

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

/**
 * @author Christian Bremer
 */
public class ClientResponseException extends RuntimeException {

  @Getter
  private HttpStatus httpStatus;

  public ClientResponseException(HttpStatus httpStatus, String body) {
    super("Server returned status code "
        + httpStatus.value() + (StringUtils.hasText(body) ? ": " + body : ""));
    this.httpStatus = httpStatus;
  }

}
