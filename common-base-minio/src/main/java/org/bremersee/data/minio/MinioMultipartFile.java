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

import io.minio.GetObjectArgs;
import io.minio.ObjectStat;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * The minio multipart file.
 *
 * @author Christian Bremer
 */
public class MinioMultipartFile implements MultipartFile {

  private final MinioOperations minioOperations;

  private final ObjectStat objectStatus;

  /**
   * Instantiates a new minio multipart file.
   *
   * @param minioOperations the minio operations
   * @param objectStatus the object status
   */
  public MinioMultipartFile(MinioOperations minioOperations, ObjectStat objectStatus) {
    Assert.notNull(minioOperations, "Minio operations must not be null.");
    Assert.notNull(objectStatus, "Object status must not be null.");
    this.minioOperations = minioOperations;
    this.objectStatus = objectStatus;
  }

  @NonNull
  @Override
  public String getName() {
    return objectStatus.bucketName();
  }

  @Override
  public String getOriginalFilename() {
    return objectStatus.name();
  }

  @Override
  public String getContentType() {
    return objectStatus.contentType();
  }

  @Override
  public boolean isEmpty() {
    return getSize() <= 0;
  }

  @Override
  public long getSize() {
    return objectStatus.length();
  }

  @NonNull
  @Override
  public byte[] getBytes() throws IOException {
    return FileCopyUtils.copyToByteArray(getInputStream());
  }

  @NonNull
  @Override
  public InputStream getInputStream() {
    return minioOperations.getObject(GetObjectArgs.builder()
        .bucket(objectStatus.bucketName())
        .object(objectStatus.name())
        .build());
  }

  @Override
  public void transferTo(@NonNull File dest) throws IOException, IllegalStateException {
    FileCopyUtils.copy(getInputStream(), Files.newOutputStream(dest.toPath()));
  }
}
