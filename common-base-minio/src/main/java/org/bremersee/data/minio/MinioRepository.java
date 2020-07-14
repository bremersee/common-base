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

import io.minio.ObjectWriteResponse;
import io.minio.http.Method;
import java.time.Duration;
import java.util.Optional;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

/**
 * The minio repository interface.
 *
 * @author Christian Bremer
 */
@Validated
public interface MinioRepository {

  /**
   * Gets minio operations.
   *
   * @return the minio operations
   */
  @NotNull
  MinioOperations getMinioOperations();

  /**
   * Save multipart file.
   *
   * @param multipartFile the multipart file
   * @param objectName the object name
   * @param deleteMode the delete mode
   * @return the write response; will be empty, if the multipart file is {@code null} or empty
   */
  Optional<ObjectWriteResponse> save(
      @Nullable MultipartFile multipartFile,
      @NotEmpty String objectName,
      @NotNull DeleteMode deleteMode);

  /**
   * Checks whether an object with the specified name exists or not.
   *
   * @param objectName the object name
   * @return {@code true} if the object exists, otherwise {@code false}
   */
  boolean exists(@NotEmpty String objectName);

  /**
   * Find one.
   *
   * @param objectName the object name
   * @return the multipart file
   */
  Optional<MultipartFile> findOne(@NotEmpty String objectName);

  /**
   * Delete.
   *
   * @param objectName the object name
   */
  void delete(@NotEmpty String objectName);

  /**
   * Gets presigned object url.
   *
   * @param method the method
   * @param objectName the object name
   * @return the presigned object url
   */
  default String getPresignedObjectUrl(@NotNull Method method, @NotEmpty String objectName) {
    return getPresignedObjectUrl(method, objectName, null);
  }

  /**
   * Gets presigned object url.
   *
   * @param method the method
   * @param objectName the object name
   * @param duration the duration
   * @return the presigned object url
   */
  String getPresignedObjectUrl(
      @NotNull Method method,
      @NotEmpty String objectName,
      @Nullable Duration duration);

}
