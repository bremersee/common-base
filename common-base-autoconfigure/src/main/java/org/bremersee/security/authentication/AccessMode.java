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

package org.bremersee.security.authentication;

import lombok.Getter;

/**
 * The access mode.
 *
 * @author Christian Bremer
 */
public enum AccessMode {

  /**
   * Permit all access mode.
   */
  PERMIT_ALL(AccessExpressionUtils.PERMIT_ALL),

  /**
   * Authenticated access mode.
   */
  AUTHENTICATED(AccessExpressionUtils.IS_AUTHENTICATED),

  /**
   * Deny all access mode.
   */
  DENY_ALL(AccessExpressionUtils.DENY_ALL);

  @Getter
  private final String expressionValue;

  AccessMode(String expressionValue) {
    this.expressionValue = expressionValue;
  }

}
