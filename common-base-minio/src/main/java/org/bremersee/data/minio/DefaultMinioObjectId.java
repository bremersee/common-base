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
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * The default minio object id.
 */
@Getter
@ToString
@EqualsAndHashCode
public class DefaultMinioObjectId implements MinioObjectId {

  private final String name;

  private final String versionId;

  /**
   * Instantiates a new default minio object id.
   *
   * @param minioObjectId the minio object id
   */
  public DefaultMinioObjectId(@NotNull MinioObjectId minioObjectId) {
    this(minioObjectId.getName(), minioObjectId.getVersionId());
  }

  /**
   * Instantiates a new default minio object id.
   *
   * @param name the name
   */
  public DefaultMinioObjectId(@NotEmpty String name) {
    this(name, null);
  }

  /**
   * Instantiates a new default minio object id.
   *
   * @param name the name
   * @param versionId the version id
   */
  public DefaultMinioObjectId(@NotEmpty String name, String versionId) {
    this.name = name;
    this.versionId = versionId;
  }

}
