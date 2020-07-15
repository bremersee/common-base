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

import io.minio.BucketExistsArgs;
import io.minio.EnableVersioningArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.IsVersioningEnabledArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import org.bremersee.exception.ServiceException;
import org.bremersee.web.multipart.FileAwareMultipartFile;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * The minio repository implementation.
 *
 * @author Christian Bremer
 */
public class MinioRepositoryImpl implements MinioRepository {

  private final MinioOperations minio;

  @Getter
  private final String region;

  @Getter
  private final String bucket;

  private final boolean enableVersioning;

  private final Duration presignedObjectUrlDuration;

  /**
   * Instantiates a new minio repository.
   *
   * @param minioOperations the minio operations
   * @param region the region
   * @param bucket the bucket
   * @param enableVersioning the enable versioning
   * @param create the create
   * @param presignedObjectUrlDuration the presigned object url duration
   */
  public MinioRepositoryImpl(
      MinioOperations minioOperations,
      String region,
      String bucket,
      boolean enableVersioning,
      boolean create,
      Duration presignedObjectUrlDuration) {

    this.minio = minioOperations;
    this.region = region;
    this.bucket = bucket;
    this.enableVersioning = enableVersioning;
    this.presignedObjectUrlDuration = validateDuration(presignedObjectUrlDuration);
    if (create) {
      createBucket();
    }
  }

  /**
   * Instantiates a new minio repository.
   *
   * @param minioClient the minio client
   * @param region the region
   * @param bucket the bucket
   * @param enableVersioning the enable versioning
   * @param create the create
   * @param presignedObjectUrlDuration the presigned object url duration
   */
  public MinioRepositoryImpl(
      MinioClient minioClient,
      String region,
      String bucket,
      boolean enableVersioning,
      boolean create,
      Duration presignedObjectUrlDuration) {

    this(
        new MinioTemplate(minioClient),
        region,
        bucket,
        enableVersioning,
        create,
        presignedObjectUrlDuration);
  }

  private static Duration validateDuration(Duration presignedObjectUrlDuration) {
    return Optional.ofNullable(presignedObjectUrlDuration)
        .filter(duration -> duration.toMillis() > 1000L * 30L
            && duration.toMillis() < 1000L * 60L * 60L * 24L * 7L)
        .orElseGet(() -> Duration.of(1L, ChronoUnit.DAYS));
  }

  private void createBucket() {
    if (!minio.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
      minio.makeBucket(MakeBucketArgs.builder()
          .region(region)
          .bucket(bucket)
          .build());
    }
    if (enableVersioning && !minio.isVersioningEnabled(IsVersioningEnabledArgs.builder()
        .region(region)
        .bucket(bucket)
        .build())) {
      minio.enableVersioning(EnableVersioningArgs.builder()
          .region(region)
          .bucket(bucket)
          .build());
    }
  }

  @Override
  public MinioOperations getMinioOperations() {
    return minio;
  }

  @Override
  public boolean isVersioningEnabled() {
    return enableVersioning;
  }

  @Override
  public Optional<ObjectWriteResponse> save(
      MinioObjectId id,
      MultipartFile multipartFile,
      DeleteMode deleteMode) {

    return Optional.ofNullable(multipartFile)
        .filter(file -> !file.isEmpty())
        .map(file -> {
          try (InputStream in = file.getInputStream()) {
            ObjectWriteResponse response = minio.putObject(PutObjectArgs.builder()
                .contentType(StringUtils.hasText(file.getContentType())
                    ? file.getContentType()
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .stream(in, file.getSize(), -1)
                .region(region)
                .bucket(bucket)
                .object(id.getName())
                .build());
            if (DeleteMode.ON_SUCCESS == deleteMode) {
              FileAwareMultipartFile.delete(file);
            }
            return response;

          } catch (IOException e) {
            throw ServiceException.internalServerError("Put object failed.", e);

          } finally {
            if (DeleteMode.ALWAYS == deleteMode) {
              FileAwareMultipartFile.delete(file);
            }
          }
        });
  }

  @Override
  public boolean exists(MinioObjectId id) {
    return minio.objectExists(StatObjectArgs.builder()
        .region(region)
        .bucket(bucket)
        .object(id.getName())
        .versionId(id.getVersionId())
        .build());
  }

  @Override
  public Optional<MinioMultipartFile> findOne(MinioObjectId id) {
    try {
      ObjectStat objectStat = minio.statObject(StatObjectArgs.builder()
          .region(region)
          .bucket(bucket)
          .object(id.getName())
          .versionId(id.getVersionId())
          .build());
      return Optional.of(new MinioMultipartFileImpl(minio, region, objectStat, id.getVersionId()));

    } catch (MinioException e) {
      if (404 == e.status()) {
        return Optional.empty();
      }
      throw e;
    }
  }

  @Override
  public List<MinioMultipartFile> findAll(String prefix) {
    Iterable<Result<Item>> results = minio.listObjects(ListObjectsArgs.builder()
        .region(region)
        .bucket(bucket)
        .includeVersions(enableVersioning)
        .recursive(true)
        .prefix(prefix)
        .build());
    List<MinioMultipartFile> fileList = new ArrayList<>();
    for (Result<Item> result : results) {
      try {
        Item item = result.get();
        if (!item.isDir() && !item.isDeleteMarker()) {
          fileList.add(new MinioMultipartFileImpl(
              getMinioOperations(),
              region,
              bucket,
              item));
        }
      } catch (Exception ignored) {
        // ignored
      }
    }
    return fileList;
  }

  @Override
  public void delete(MinioObjectId id) {
    minio.removeObject(RemoveObjectArgs.builder()
        .region(region)
        .bucket(bucket)
        .object(id.getName())
        .versionId(id.getVersionId())
        .build());
  }

  @Override
  public List<DeleteError> deleteAll(Collection<MinioObjectId> ids) {

    if (ids == null || ids.isEmpty()) {
      return Collections.emptyList();
    }
    Iterable<Result<DeleteError>> errors = minio.removeObjects(RemoveObjectsArgs.builder()
        .region(region)
        .bucket(bucket)
        .objects(ids.stream()
            .map(id -> new DeleteObject(id.getName(), id.getVersionId()))
            .collect(Collectors.toSet()))
        .build());
    List<DeleteError> errorList = new ArrayList<>();
    for (Result<DeleteError> error : errors) {
      try {
        errorList.add(error.get());
      } catch (Exception ignored) {
        // ignored
      }
    }
    return errorList;
  }

  @Override
  public String getPresignedObjectUrl(MinioObjectId id, Method method, Duration duration) {
    Duration expiry = duration != null ? validateDuration(duration) : presignedObjectUrlDuration;
    return minio.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
        .expiry((int) expiry.toSeconds())
        .method(method)
        .region(region)
        .bucket(bucket)
        .object(id.getName())
        .versionId(id.getVersionId())
        .build());
  }

}
