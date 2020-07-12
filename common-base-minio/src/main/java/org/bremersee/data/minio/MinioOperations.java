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
import io.minio.CloseableIterator;
import io.minio.ComposeObjectArgs;
import io.minio.CopyObjectArgs;
import io.minio.DeleteBucketEncryptionArgs;
import io.minio.DeleteBucketNotificationArgs;
import io.minio.DeleteBucketPolicyArgs;
import io.minio.DeleteBucketTagsArgs;
import io.minio.DeleteDefaultRetentionArgs;
import io.minio.DeleteObjectTagsArgs;
import io.minio.DisableObjectLegalHoldArgs;
import io.minio.DisableVersioningArgs;
import io.minio.DownloadObjectArgs;
import io.minio.EnableObjectLegalHoldArgs;
import io.minio.EnableVersioningArgs;
import io.minio.GetBucketEncryptionArgs;
import io.minio.GetBucketLifeCycleArgs;
import io.minio.GetBucketNotificationArgs;
import io.minio.GetBucketPolicyArgs;
import io.minio.GetBucketTagsArgs;
import io.minio.GetDefaultRetentionArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectRetentionArgs;
import io.minio.GetObjectTagsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.IsObjectLegalHoldEnabledArgs;
import io.minio.IsVersioningEnabledArgs;
import io.minio.ListIncompleteUploadsArgs;
import io.minio.ListObjectsArgs;
import io.minio.ListenBucketNotificationArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.ObjectWriteResponse;
import io.minio.PostPolicy;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveIncompleteUploadArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.SelectObjectContentArgs;
import io.minio.SelectResponseStream;
import io.minio.SetBucketEncryptionArgs;
import io.minio.SetBucketLifeCycleArgs;
import io.minio.SetBucketNotificationArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.SetBucketTagsArgs;
import io.minio.SetDefaultRetentionArgs;
import io.minio.SetObjectRetentionArgs;
import io.minio.SetObjectTagsArgs;
import io.minio.StatObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.Item;
import io.minio.messages.NotificationConfiguration;
import io.minio.messages.NotificationRecords;
import io.minio.messages.ObjectLockConfiguration;
import io.minio.messages.Retention;
import io.minio.messages.SseConfiguration;
import io.minio.messages.Tags;
import io.minio.messages.Upload;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;

/**
 * The minio operations.
 *
 * @author Christian Bremer
 */
@Validated
public interface MinioOperations {

  /**
   * Execute minio callback.
   *
   * @param <T> the type of the result
   * @param callback the callback
   * @return the result
   */
  <T> T execute(MinioClientCallback<T> callback);

  // Bucket operations

  default boolean bucketExists(BucketExistsArgs args) {
    return execute(minioClient -> minioClient.bucketExists(args));
  }

  default void deleteBucketEncryption(DeleteBucketEncryptionArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .deleteBucketEncryption(args));
  }

  default void deleteBucketTags(DeleteBucketTagsArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .deleteBucketTags(args));
  }

  default void deleteBucketPolicy(DeleteBucketPolicyArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .deleteBucketPolicy(args));
  }

  default void deleteBucketNotification(DeleteBucketNotificationArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .deleteBucketNotification(args));
  }

  default void deleteDefaultRetention(DeleteDefaultRetentionArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .deleteDefaultRetention(args));
  }

  default void disableVersioning(DisableVersioningArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .disableVersioning(args));
  }

  default void enableVersioning(EnableVersioningArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .enableVersioning(args));
  }

  default boolean isVersioningEnabled(IsVersioningEnabledArgs args) {
    return execute(minioClient -> minioClient.isVersioningEnabled(args));
  }

  default SseConfiguration getBucketEncryption(GetBucketEncryptionArgs args) {
    return execute(minioClient -> minioClient.getBucketEncryption(args));
  }

  default String getBucketLifeCycle(GetBucketLifeCycleArgs args) {
    return execute(minioClient -> minioClient.getBucketLifeCycle(args));
  }

  default NotificationConfiguration getBucketNotification(GetBucketNotificationArgs args) {
    return execute(minioClient -> minioClient.getBucketNotification(args));
  }

  default String getBucketPolicy(GetBucketPolicyArgs args) {
    return execute(minioClient -> minioClient.getBucketPolicy(args));
  }

  default Tags getBucketTags(GetBucketTagsArgs args) {
    return execute(minioClient -> minioClient.getBucketTags(args));
  }

  default ObjectLockConfiguration getDefaultRetention(GetDefaultRetentionArgs args) {
    return execute(minioClient -> minioClient.getDefaultRetention(args));
  }

  /**
   * Lists bucket information of all buckets.
   *
   * <pre>Example:{@code
   * List<Bucket> bucketList = minioOperations.listBuckets();
   * for (Bucket bucket : bucketList) {
   *   System.out.println(bucket.creationDate() + ", " + bucket.name());
   * }
   * }
   * </pre>
   *
   * @return List &ltBucket&gt - List of bucket information.
   */
  default List<Bucket> listBuckets() {
    return execute(MinioClient::listBuckets);
  }

  default CloseableIterator<Result<NotificationRecords>> listenBucketNotification(
      ListenBucketNotificationArgs args) {
    return execute(minioClient -> minioClient.listenBucketNotification(args));
  }

  default Iterable<Result<Upload>> listIncompleteUploads(ListIncompleteUploadsArgs args) {
    return execute(minioClient -> minioClient.listIncompleteUploads(args));
  }

  default Iterable<Result<Item>> listObjects(ListObjectsArgs args) {
    return execute(minioClient -> minioClient.listObjects(args));
  }

  default void makeBucket(MakeBucketArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .makeBucket(args));
  }

  default void removeBucket(RemoveBucketArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .removeBucket(args));
  }

  default void removeIncompleteUpload(RemoveIncompleteUploadArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .removeIncompleteUpload(args));
  }

  default void setBucketEncryption(SetBucketEncryptionArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setBucketEncryption(args));
  }

  default void setBucketLifeCycle(SetBucketLifeCycleArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setBucketLifeCycle(args));
  }

  default void setBucketNotification(SetBucketNotificationArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setBucketNotification(args));
  }

  default void setBucketPolicy(SetBucketPolicyArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setBucketPolicy(args));
  }

  default void setBucketTags(SetBucketTagsArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setBucketTags(args));
  }

  default void setDefaultRetention(SetDefaultRetentionArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setDefaultRetention(args));
  }

  // Object operations

  default ObjectWriteResponse composeObject(ComposeObjectArgs args) {
    return execute(minioClient -> minioClient.composeObject(args));
  }

  default ObjectWriteResponse copyObject(CopyObjectArgs args) {
    return execute(minioClient -> minioClient.copyObject(args));
  }

  default void deleteObjectTags(DeleteObjectTagsArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .deleteObjectTags(args));
  }

  default void disableObjectLegalHold(DisableObjectLegalHoldArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .disableObjectLegalHold(args));
  }

  default void enableObjectLegalHold(EnableObjectLegalHoldArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .enableObjectLegalHold(args));
  }

  default InputStream getObject(GetObjectArgs args) {
    return execute(minioClient -> minioClient.getObject(args));
  }

  default void downloadObject(DownloadObjectArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .downloadObject(args));
  }

  default Retention getObjectRetention(GetObjectRetentionArgs args) {
    return execute(minioClient -> minioClient.getObjectRetention(args));
  }

  default Tags getObjectTags(GetObjectTagsArgs args) {
    return execute(minioClient -> minioClient.getObjectTags(args));
  }

  default String getObjectUrl(String bucketName, String objectName) {
    return execute(minioClient -> minioClient.getObjectUrl(bucketName, objectName));
  }

  default String getPresignedObjectUrl(GetPresignedObjectUrlArgs args) {
    return execute(minioClient -> minioClient.getPresignedObjectUrl(args));
  }

  default boolean isObjectLegalHoldEnabled(IsObjectLegalHoldEnabledArgs args) {
    return execute(minioClient -> minioClient.isObjectLegalHoldEnabled(args));
  }

  default Map<String, String> presignedPostPolicy(PostPolicy policy) {
    return execute(minioClient -> minioClient.presignedPostPolicy(policy));
  }

  default ObjectWriteResponse putObject(PutObjectArgs args) {
    return execute(minioClient -> minioClient.putObject(args));
  }

  default void uploadObject(UploadObjectArgs args, DeleteMode deleteMode) {
    final Path file = Paths.get(args.filename());
    try {
      execute((MinioClientCallbackWithoutResult) minioClient -> {
        minioClient.uploadObject(args);
        if (DeleteMode.ON_SUCCESS == deleteMode) {
          Files.delete(file);
        }
      });

    } finally {
      if (DeleteMode.ALWAYS == deleteMode) {
        execute((MinioClientCallbackWithoutResult) minioClient -> Files.delete(file));
      }
    }
  }

  default void removeObject(RemoveObjectArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .removeObject(args));
  }

  default Iterable<Result<DeleteError>> removeObjects(RemoveObjectsArgs args) {
    return execute(minioClient -> minioClient.removeObjects(args));
  }

  default SelectResponseStream selectObjectContent(SelectObjectContentArgs args) {
    return execute(minioClient -> minioClient.selectObjectContent(args));
  }

  default void setObjectRetention(SetObjectRetentionArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setObjectRetention(args));
  }

  default void setObjectTags(SetObjectTagsArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setObjectTags(args));
  }

  default ObjectStat statObject(StatObjectArgs args) {
    return execute(minioClient -> minioClient.statObject(args));
  }

}
