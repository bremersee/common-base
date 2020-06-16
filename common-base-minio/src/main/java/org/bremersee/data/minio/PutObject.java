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

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;

/**
 * The put object.
 *
 * @param <T> the type parameter
 * @author Christian Bremer
 */
@Validated
public interface PutObject<T> {

  PutObject<?> EMPTY = InMemoryPutObject.empty();

  /**
   * Checks whether this put object is empty.
   *
   * @return {@code true} if it is empty, otherwise {@code false}
   */
  default boolean isEmpty() {
    T obj = getObject();
    if (obj instanceof byte[]) {
      return ((byte[]) obj).length == 0;
    }
    return obj == null;
  }

  /**
   * Gets object.
   *
   * @return the object
   */
  @Nullable
  T getObject();

  /**
   * Gets content type.
   *
   * @return the content type
   */
  @Nullable
  String getContentType();

  /**
   * Gets name.
   *
   * @return the name
   */
  @Nullable
  String getName();

  /**
   * Gets length of the object.
   *
   * @return the length
   */
  long getLength();

  /**
   * Delete.
   */
  void delete();

  /**
   * Upload.
   *
   * @param minioOperations the minio operations
   * @param bucketName the bucket name
   * @param name the name
   * @param deleteObject the delete object
   * @return the boolean
   */
  boolean upload(
      @Nullable MinioOperations minioOperations,
      @NotEmpty String bucketName,
      @NotEmpty String name,
      boolean deleteObject);

}
