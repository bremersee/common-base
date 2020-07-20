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

import io.minio.MinioClient;
import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * The minio client callback without result.
 *
 * @author Christian Bremer
 */
@Validated
public interface MinioClientCallbackWithoutResult extends MinioClientCallback<Object> {

  @Override
  default Object doWithMinioClient(@NotNull MinioClient minioClient) throws Exception {
    doWithoutResult(minioClient);
    return null;
  }

  /**
   * Do without result.
   *
   * @param minioClient the minio client
   * @throws Exception the exception
   */
  void doWithoutResult(@NotNull MinioClient minioClient) throws Exception;

}
