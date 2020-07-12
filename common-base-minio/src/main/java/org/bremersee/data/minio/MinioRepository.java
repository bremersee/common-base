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

@Validated
public interface MinioRepository {

  @NotNull
  MinioOperations getMinioOperations();

  default String getPresignedObjectUrl(@NotNull Method method, @NotNull String objectName) {
    return getPresignedObjectUrl(method, objectName, null);
  }

  String getPresignedObjectUrl(
      @NotNull Method method,
      @NotNull String objectName,
      @Nullable Duration duration);

  Optional<ObjectWriteResponse> putObject(
      @Nullable MultipartFile multipartFile,
      @NotEmpty String objectName,
      @NotNull DeleteMode deleteMode);

}
