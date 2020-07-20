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

package org.bremersee.data.minio;

import javax.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;

/**
 * The minio object id interface.
 *
 * @author Christian Bremer
 */
@Validated
public interface MinioObjectId {

  /**
   * From object name.
   *
   * @param name the name
   * @return the minio object id
   */
  static MinioObjectId from(@NotEmpty String name) {
    return from(name, null);
  }

  /**
   * From object name and version ID.
   *
   * @param name the name
   * @param version the version
   * @return the minio object id
   */
  static MinioObjectId from(@NotEmpty String name, String version) {
    return new DefaultMinioObjectId(name, version);
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  @NotEmpty
  String getName();

  /**
   * Gets version id.
   *
   * @return the version id
   */
  String getVersionId();

}
