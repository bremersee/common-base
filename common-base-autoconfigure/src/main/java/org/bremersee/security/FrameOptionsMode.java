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

import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode;

/**
 * The enum Frame options mode.
 */
public enum FrameOptionsMode {

  /**
   * Disable frame options mode.
   */
  DISABLE(null),

  /**
   * Sameorigin frame options mode.
   */
  SAMEORIGIN(Mode.SAMEORIGIN),

  /**
   * Deny frame options mode.
   */
  DENY(Mode.DENY);

  private final Mode mode;

  FrameOptionsMode(Mode mode) {
    this.mode = mode;
  }

  /**
   * Gets mode.
   *
   * @return the mode
   */
  public Mode getMode() {
    return mode;
  }
}
