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
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.bremersee.exception.ServiceException;
import org.bremersee.web.multipart.FileAwareMultipartFile;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class MinioRepositoryImpl implements MinioRepository {

  private final MinioOperations minio;

  private final String bucket;

  private final Duration presignedObjectUrlDuration;

  public MinioRepositoryImpl(
      MinioOperations minioOperations,
      String bucket,
      boolean create,
      Duration presignedObjectUrlDuration) {

    this.minio = minioOperations;
    this.bucket = bucket;
    this.presignedObjectUrlDuration = validateDuration(presignedObjectUrlDuration);
    if (create) {
      createBucket();
    }
  }

  public MinioRepositoryImpl(
      MinioClient minioClient,
      String bucket,
      boolean create,
      Duration presignedObjectUrlDuration) {

    this.minio = new MinioTemplate(minioClient);
    this.bucket = bucket;
    this.presignedObjectUrlDuration = validateDuration(presignedObjectUrlDuration);
    if (create) {
      createBucket();
    }
  }

  private static Duration validateDuration(Duration presignedObjectUrlDuration) {
    return Optional.ofNullable(presignedObjectUrlDuration)
        .filter(duration -> duration.toMillis() > 1000L * 30L
            && duration.toMillis() < 1000L * 60L * 60L * 24L * 7L)
        .orElseGet(() -> Duration.of(1L, ChronoUnit.DAYS));
  }

  private void createBucket() {
    if (!minio.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
      minio.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
    }
  }

  @Override
  public MinioOperations getMinioOperations() {
    return minio;
  }

  public String getPresignedObjectUrl(Method method, String objectName, Duration duration) {
    Duration expiry = duration != null ? validateDuration(duration) : presignedObjectUrlDuration;
    return minio.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
        .expiry((int)expiry.toSeconds())
        .method(method)
        .bucket(bucket)
        .object(objectName)
        .build());
  }

  @Override
  public Optional<ObjectWriteResponse> putObject(
      MultipartFile multipartFile,
      String objectName,
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
                .bucket(bucket)
                .object(objectName)
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
}
