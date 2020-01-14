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

import lombok.Builder;
import lombok.Getter;

/**
 * The rest api tester exclusion.
 *
 * @author Christian Bremer
 */
public class RestApiTesterExclusion {

  /**
   * Checks whether the given path and type are excluded via the given exclusions.
   *
   * @param path the path
   * @param type the type
   * @param exclusions the exclusions
   * @return the boolean
   */
  public static boolean isExcluded(
      RestApiTesterPath path,
      RestApiAssertionType type,
      RestApiTesterExclusion[] exclusions) {

    if (path == null || type == null || exclusions == null) {
      return false;
    }
    for (RestApiTesterExclusion exclusion : exclusions) {
      if (exclusion.getPath() != null && exclusion.getPath().equals(path)
          && (exclusion.getType() == RestApiAssertionType.ANY || exclusion.getType() == type)) {
        return true;
      }
    }
    return isExcluded(path, type.getParent(), exclusions);
  }

  @Getter
  private RestApiTesterPath path;

  @Getter
  private RestApiAssertionType type;

  /**
   * Instantiates a new rest api tester exclusion.
   *
   * @param path the path
   * @param type the type
   */
  @Builder(builderMethodName = "exclusionBuilder")
  public RestApiTesterExclusion(RestApiTesterPath path, RestApiAssertionType type) {
    this.path = path;
    this.type = type != null ? type : RestApiAssertionType.ANY;
  }

}
