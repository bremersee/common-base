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
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

/**
 * The minio multipart file implementation.
 *
 * @author Christian Bremer
 */
@ToString(
    exclude = {"minioOperations", "etag", "size", "objectStatus"},
    doNotUseGetters = true)
@EqualsAndHashCode(
    exclude = {"minioOperations", "etag", "size", "objectStatus"},
    doNotUseGetters = true)
public class MinioMultipartFileImpl implements MinioMultipartFile {

  private final MinioOperations minioOperations;

  @Getter
  private final String region;

  @Getter
  private final String bucket;

  private final String name;

  @Getter
  private final String versionId;

  private String etag;

  private Long size;

  private StatObjectResponse objectStatus;

  /**
   * Instantiates a new minio multipart file.
   *
   * @param minioOperations the minio operations
   * @param minioObjectInfo the minio object info
   */
  public MinioMultipartFileImpl(
      MinioOperations minioOperations,
      MinioObjectInfo minioObjectInfo) {

    Assert.notNull(minioOperations, "Minio operations must not be null.");
    Assert.notNull(minioObjectInfo, "Object info must not be null.");
    Assert.hasText(minioObjectInfo.getBucket(), "Bucket must not be null.");
    this.minioOperations = minioOperations;
    this.region = minioObjectInfo.getRegion();
    this.bucket = minioObjectInfo.getBucket();
    this.name = minioObjectInfo.getName();
    this.versionId = minioObjectInfo.getVersionId();
  }

  /**
   * Instantiates a new minio multipart file.
   *
   * @param minioOperations the minio operations
   * @param region the region
   * @param objectStatus the object status
   */
  public MinioMultipartFileImpl(
      MinioOperations minioOperations,
      String region,
      StatObjectResponse objectStatus) {

    Assert.notNull(minioOperations, "Minio operations must not be null.");
    Assert.notNull(objectStatus, "Object status must not be null.");
    this.minioOperations = minioOperations;
    this.region = region;
    this.bucket = objectStatus.bucket();
    this.name = objectStatus.object();
    this.versionId = objectStatus.versionId();
    this.etag = objectStatus.etag();
    this.size = objectStatus.size();
    this.objectStatus = objectStatus;
  }

  /**
   * Instantiates a new minio multipart file.
   *
   * @param minioOperations the minio operations
   * @param region the region
   * @param bucket the bucket
   * @param item the item
   */
  public MinioMultipartFileImpl(
      MinioOperations minioOperations,
      String region,
      String bucket,
      Item item) {

    Assert.notNull(minioOperations, "Minio operations must not be null.");
    Assert.hasText(bucket, "Bucket must be present.");
    Assert.notNull(item, "Item must not be null.");
    this.minioOperations = minioOperations;
    this.region = region;
    this.bucket = bucket;
    this.name = item.objectName();
    this.versionId = item.versionId();
    this.etag = item.etag();
    this.size = item.size();
  }

  /**
   * Gets object status.
   *
   * @return the object status
   */
  protected StatObjectResponse getObjectStatus() {
    if (objectStatus == null) {
      objectStatus = minioOperations.statObject(StatObjectArgs.builder()
          .region(region)
          .bucket(bucket)
          .object(name)
          .versionId(versionId)
          .build());
    }
    return objectStatus;
  }

  @NonNull
  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getOriginalFilename() {
    return name;
  }

  @Override
  public String getContentType() {
    return getObjectStatus().contentType();
  }

  @Override
  public boolean isEmpty() {
    return getSize() <= 0;
  }

  @Override
  public long getSize() {
    if (size == null) {
      size = getObjectStatus().size();
    }
    return size;
  }

  @NonNull
  @Override
  public byte[] getBytes() throws IOException {
    if (isEmpty()) {
      return new byte[0];
    }
    return FileCopyUtils.copyToByteArray(getInputStream());
  }

  @NonNull
  @Override
  public InputStream getInputStream() {
    if (isEmpty()) {
      return new ByteArrayInputStream(new byte[0]);
    }
    return minioOperations.getObject(GetObjectArgs.builder()
        .region(region)
        .bucket(bucket)
        .object(name)
        .versionId(versionId)
        .build());
  }

  @Override
  public void transferTo(@NonNull File dest) throws IOException, IllegalStateException {
    FileCopyUtils.copy(getInputStream(), Files.newOutputStream(dest.toPath()));
  }

  @Override
  public String getEtag() {
    if (etag == null) {
      etag = getObjectStatus().etag();
    }
    return etag;
  }

  @Override
  public OffsetDateTime getLastModified() {
    return Optional.ofNullable(getObjectStatus().lastModified())
        .map(time -> OffsetDateTime.ofInstant(time.toInstant(), ZoneOffset.UTC))
        .orElse(null);

  }

}
