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

package org.bremersee.test.web;

import lombok.Getter;

/**
 * The rest api assertion type.
 *
 * @author Christian Bremer
 */
public enum RestApiAssertionType {

  /**
   * Any rest api assertion type.
   */
  ANY(null),

  /**
   * Class must be interface rest api assertion type.
   */
  CLASS_MUST_BE_INTERFACE(ANY),

  /**
   * Same method size rest api assertion type.
   */
  SAME_METHOD_SIZE(ANY),

  /**
   * Method must not be null rest api assertion type.
   */
  METHOD_MUST_NOT_BE_NULL(SAME_METHOD_SIZE),

  /**
   * Same annotation size rest api assertion type.
   */
  SAME_ANNOTATION_SIZE(ANY),

  /**
   * Annotation must not be null rest api assertion type.
   */
  ANNOTATION_MUST_NOT_BE_NULL(ANY),

  /**
   * Same annotation attributes size rest api assertion type.
   */
  SAME_ANNOTATION_ATTRIBUTES_SIZE(ANY),

  /**
   * Same annotation attribute value rest api assertion type.
   */
  SAME_ANNOTATION_ATTRIBUTE_VALUE(ANY);

  @Getter
  private final RestApiAssertionType parent;

  RestApiAssertionType(RestApiAssertionType parent) {
    this.parent = parent;
  }
}
