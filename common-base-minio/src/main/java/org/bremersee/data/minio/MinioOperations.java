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

  /**
   * Checks if a bucket exists.
   *
   * <pre>Example:{@code
   * boolean found =
   *      minioClient.bucketExists(BucketExistsArgs.builder().bucket("my-bucketname").build());
   * if (found) {
   *   System.out.println("my-bucketname exists");
   * } else {
   *   System.out.println("my-bucketname does not exist");
   * }
   * }</pre>
   *
   * @param args {@link BucketExistsArgs} object.
   * @return boolean - True if the bucket exists.
   */
  default boolean bucketExists(BucketExistsArgs args) {
    return execute(minioClient -> minioClient.bucketExists(args));
  }

  /**
   * Deletes encryption configuration of a bucket.
   *
   * <pre>Example:{@code
   * minioClient.deleteBucketEncryption(
   *     DeleteBucketEncryptionArgs.builder().bucket("my-bucketname").build());
   * }</pre>
   *
   * @param args {@link DeleteBucketEncryptionArgs} object.
   */
  default void deleteBucketEncryption(DeleteBucketEncryptionArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .deleteBucketEncryption(args));
  }

  /**
   * Deletes tags of a bucket.
   *
   * <pre>Example:{@code
   * minioClient.deleteBucketTags(DeleteBucketTagsArgs.builder().bucket("my-bucketname").build());
   * }</pre>
   *
   * @param args {@link DeleteBucketTagsArgs} object.
   */
  default void deleteBucketTags(DeleteBucketTagsArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .deleteBucketTags(args));
  }

  /**
   * Deletes bucket policy configuration to a bucket.
   *
   * <pre>Example:{@code
   * minioClient.deleteBucketPolicy(DeleteBucketPolicyArgs.builder().bucket("my-bucketname"));
   * }</pre>
   *
   * @param args {@link DeleteBucketPolicyArgs} object.
   */
  default void deleteBucketPolicy(DeleteBucketPolicyArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .deleteBucketPolicy(args));
  }

  /**
   * Deletes notification configuration of a bucket.
   *
   * <pre>Example:{@code
   * minioClient.deleteBucketNotification(
   *     DeleteBucketNotificationArgs.builder().bucket("my-bucketname").build());
   * }</pre>
   *
   * @param args {@link DeleteBucketNotificationArgs} object.
   */
  default void deleteBucketNotification(DeleteBucketNotificationArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .deleteBucketNotification(args));
  }

  /**
   * Deletes default object retention in a bucket.
   *
   * <pre>Example:{@code
   * minioClient.deleteDefaultRetention(
   *     DeleteDefaultRetentionArgs.builder().bucket("my-bucketname").build());
   * }</pre>
   *
   * @param args {@link DeleteDefaultRetentionArgs} object.
   */
  default void deleteDefaultRetention(DeleteDefaultRetentionArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .deleteDefaultRetention(args));
  }

  /**
   * Disables object versioning feature in a bucket.
   *
   * <pre>Example:{@code
   * minioClient.disableVersioning(
   *     DisableVersioningArgs.builder().bucket("my-bucketname").build());
   * }</pre>
   *
   * @param args {@link DisableVersioningArgs} object.
   */
  default void disableVersioning(DisableVersioningArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .disableVersioning(args));
  }

  /**
   * Enables object versioning feature in a bucket.
   *
   * <pre>Example:{@code
   * minioClient.enableVersioning(EnableVersioningArgs.builder().bucket("my-bucketname").build());
   * }</pre>
   *
   * @param args {@link EnableVersioningArgs} object.
   */
  default void enableVersioning(EnableVersioningArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .enableVersioning(args));
  }

  /**
   * Returns true if versioning is enabled on the bucket.
   *
   * <pre>Example:{@code
   * boolean isVersioningEnabled =
   *  minioClient.isVersioningEnabled(
   *       IsVersioningEnabledArgs.builder().bucket("my-bucketname").build());
   * if (isVersioningEnabled) {
   *   System.out.println("Bucket versioning is enabled");
   * } else {
   *   System.out.println("Bucket versioning is disabled");
   * }
   * }</pre>
   *
   * @param args {@link IsVersioningEnabledArgs} object.
   * @return {@code true} if versioning is enabled, otherwise {@code false}
   */
  default boolean isVersioningEnabled(IsVersioningEnabledArgs args) {
    return execute(minioClient -> minioClient.isVersioningEnabled(args));
  }

  /**
   * Gets encryption configuration of a bucket.
   *
   * <pre>Example:{@code
   * SseConfiguration config =
   *     minioClient.getBucketEncryption(
   *         GetBucketEncryptionArgs.builder().bucket("my-bucketname").build());
   * }</pre>
   *
   * @param args {@link GetBucketEncryptionArgs} object.
   * @return {@link SseConfiguration} - Server-side encryption configuration.
   */
  default SseConfiguration getBucketEncryption(GetBucketEncryptionArgs args) {
    return execute(minioClient -> minioClient.getBucketEncryption(args));
  }

  /**
   * Gets life-cycle configuration of a bucket.
   *
   * <pre>Example:{@code
   * String lifecycle =
   *     minioClient.getBucketLifeCycle(
   *         GetBucketLifeCycleArgs.builder().bucket("my-bucketname").build());
   * }</pre>
   *
   * @param args {@link GetBucketLifeCycleArgs} object.
   * @return String - Life cycle configuration as XML string.
   */
  default String getBucketLifeCycle(GetBucketLifeCycleArgs args) {
    return execute(minioClient -> minioClient.getBucketLifeCycle(args));
  }

  /**
   * Gets notification configuration of a bucket.
   *
   * <pre>Example:{@code
   * NotificationConfiguration config =
   *     minioClient.getBucketNotification(
   *         GetBucketNotificationArgs.builder().bucket("my-bucketname").build());
   * }</pre>
   *
   * @param args {@link GetBucketNotificationArgs} object.
   * @return {@link NotificationConfiguration} - Notification configuration.
   */
  default NotificationConfiguration getBucketNotification(GetBucketNotificationArgs args) {
    return execute(minioClient -> minioClient.getBucketNotification(args));
  }

  /**
   * Gets bucket policy configuration of a bucket.
   *
   * <pre>Example:{@code
   * String config =
   *     minioClient.getBucketPolicy(GetBucketPolicyArgs.builder().bucket("my-bucketname").build());
   * }</pre>
   *
   * @param args {@link GetBucketPolicyArgs} object.
   * @return String - Bucket policy configuration as JSON string.
   */
  default String getBucketPolicy(GetBucketPolicyArgs args) {
    return execute(minioClient -> minioClient.getBucketPolicy(args));
  }

  /**
   * Gets tags of a bucket.
   *
   * <pre>Example:{@code
   * Tags tags =
   *     minioClient.getBucketTags(GetBucketTagsArgs.builder().bucket("my-bucketname").build());
   * }</pre>
   *
   * @param args {@link GetBucketTagsArgs} object.
   * @return {@link Tags} - Tags.
   */
  default Tags getBucketTags(GetBucketTagsArgs args) {
    return execute(minioClient -> minioClient.getBucketTags(args));
  }

  /**
   * Gets default object retention in a bucket.
   *
   * <pre>Example:{@code
   * ObjectLockConfiguration config =
   *     minioClient.getDefaultRetention(
   *         GetDefaultRetentionArgs.builder().bucket("my-bucketname").build());
   * System.out.println("Mode: " + config.mode());
   * System.out.println(
   *     "Duration: " + config.duration().duration() + " " + config.duration().unit());
   * }</pre>
   *
   * @param args {@link GetDefaultRetentionArgs} object.
   * @return {@link ObjectLockConfiguration} - Default retention configuration.
   */
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

  /**
   * Listens events of object prefix and suffix of a bucket. The returned closable iterator is
   * lazily evaluated hence its required to iterate to get new records and must be used with
   * try-with-resource to release underneath network resources.
   *
   * <pre>Example:{@code
   * String[] events = {"s3:ObjectCreated:*", "s3:ObjectAccessed:*"};
   * try (CloseableIterator<Result<NotificationRecords>> ci =
   *     minioClient.listenBucketNotification(
   *         ListenBucketNotificationArgs.builder()
   *             .bucket("bucketName")
   *             .prefix("")
   *             .suffix("")
   *             .events(events)
   *             .build())) {
   *   while (ci.hasNext()) {
   *     NotificationRecords records = ci.next().get();
   *     for (Event event : records.events()) {
   *       System.out.println("Event " + event.eventType() + " occurred at "
   *           + event.eventTime() + " for " + event.bucketName() + "/"
   *           + event.objectName());
   *     }
   *   }
   * }
   * }</pre>
   *
   * @param args {@link ListenBucketNotificationArgs} object.
   * @return CloseableIterator&ltResult&ltNotificationRecords&gt&gt - Lazy closable iterator
   *     contains event records.
   */
  default CloseableIterator<Result<NotificationRecords>> listenBucketNotification(
      ListenBucketNotificationArgs args) {
    return execute(minioClient -> minioClient.listenBucketNotification(args));
  }

  /**
   * Lists incomplete object upload information of a bucket for prefix recursively.
   *
   * <pre>Example:{@code
   *  // Lists incomplete object upload information of a bucket.
   *   Iterable<Result<Upload>> results =
   *       minioClient.listIncompleteUploads(
   *           ListIncompleteUploadsArgs.builder().bucket("my-bucketname").build());
   *   for (Result<Upload> result : results) {
   *     Upload upload = result.get();
   *     System.out.println(upload.uploadId() + ", " + upload.objectName());
   *   }
   *
   *   // Lists incomplete object upload information of a bucket for prefix.
   *   Iterable<Result<Upload>> results =
   *       minioClient.listIncompleteUploads(
   *           ListIncompleteUploadsArgs.builder()
   *               .bucket("my-bucketname")
   *               .prefix("my-obj")
   *               .build());
   *   for (Result<Upload> result : results) {
   *     Upload upload = result.get();
   *     System.out.println(upload.uploadId() + ", " + upload.objectName());
   *   }
   *
   *   // Lists incomplete object upload information of a bucket for prefix recursively.
   *   Iterable<Result<Upload>> results =
   *       minioClient.listIncompleteUploads(
   *           ListIncompleteUploadsArgs.builder()
   *               .bucket("my-bucketname")
   *               .prefix("my-obj")
   *               .recursive(true)
   *               .build());
   *   for (Result<Upload> result : results) {
   *    Upload upload = result.get();
   *    System.out.println(upload.uploadId() + ", " + upload.objectName());
   *   }
   *
   *   // Lists incomplete object upload information of a bucket for prefix, delimiter.
   *   //  keyMarker, uploadIdMarker and maxUpload to 500
   *   Iterable<Result<Upload>> results =
   *       minioClient.listIncompleteUploads(
   *           ListIncompleteUploadsArgs.builder()
   *               .bucket("my-bucketname")
   *               .prefix("my-obj")
   *               .delimiter("-")
   *               .keyMarker("b")
   *               .maxUploads(500)
   *               .uploadIdMarker("k")
   *               .build());
   *   for (Result<Upload> result : results) {
   *    Upload upload = result.get();
   *    System.out.println(upload.uploadId() + ", " + upload.objectName());
   *   }
   * }</pre>
   *
   * @param args {@link ListIncompleteUploadsArgs} objects.
   * @return Iterable&lt;Result&lt;Upload&gt;&gt; - Lazy iterator contains object upload
   *     information.
   */
  default Iterable<Result<Upload>> listIncompleteUploads(ListIncompleteUploadsArgs args) {
    return execute(minioClient -> minioClient.listIncompleteUploads(args));
  }

  /**
   * Lists objects information optionally with versions of a bucket. Supports both the versions 1
   * and 2 of the S3 API. By default, the <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_ListObjectsV2.html">version
   * 2</a> API is used. <br>
   * <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_ListObjects.html">Version 1</a>
   * can be used by passing the optional argument {@code useVersion1} as {@code true}.
   *
   * <pre>Example:{@code
   * // Lists objects information.
   * Iterable<Result<Item>> results = minioClient.listObjects(
   *     ListObjectsArgs.builder().bucket("my-bucketname").build());
   *
   * // Lists objects information recursively.
   * Iterable<Result<Item>> results = minioClient.listObjects(
   *     ListObjectsArgs.builder().bucket("my-bucketname").recursive(true).build());
   *
   * // Lists maximum 100 objects information those names starts with 'E' and after
   * // 'ExampleGuide.pdf'.
   * Iterable<Result<Item>> results = minioClient.listObjects(
   *     ListObjectsArgs.builder()
   *         .bucket("my-bucketname")
   *         .startAfter("ExampleGuide.pdf")
   *         .prefix("E")
   *         .maxKeys(100)
   *         .build());
   *
   * // Lists maximum 100 objects information with version those names starts with 'E' and after
   * // 'ExampleGuide.pdf'.
   * Iterable<Result<Item>> results = minioClient.listObjects(
   *     ListObjectsArgs.builder()
   *         .bucket("my-bucketname")
   *         .startAfter("ExampleGuide.pdf")
   *         .prefix("E")
   *         .maxKeys(100)
   *         .includeVersions(true)
   *         .build());
   * }</pre>
   *
   * @param args Instance of {@link ListObjectsArgs} built using the builder
   * @return Iterable&lt;Result&lt;Item&gt;&gt; - Lazy iterator contains object information.
   */
  default Iterable<Result<Item>> listObjects(ListObjectsArgs args) {
    return execute(minioClient -> minioClient.listObjects(args));
  }

  /**
   * Creates a bucket with region and object lock.
   *
   * <pre>Example:{@code
   * // Create bucket with default region.
   * minioClient.makeBucket(
   *     MakeBucketArgs.builder()
   *         .bucket("my-bucketname")
   *         .build());
   *
   * // Create bucket with specific region.
   * minioClient.makeBucket(
   *     MakeBucketArgs.builder()
   *         .bucket("my-bucketname")
   *         .region("us-west-1")
   *         .build());
   *
   * // Create object-lock enabled bucket with specific region.
   * minioClient.makeBucket(
   *     MakeBucketArgs.builder()
   *         .bucket("my-bucketname")
   *         .region("us-west-1")
   *         .objectLock(true)
   *         .build());
   * }</pre>
   *
   * @param args Object with bucket name, region and lock functionality
   */
  default void makeBucket(MakeBucketArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .makeBucket(args));
  }

  /**
   * Removes an empty bucket using arguments
   *
   * <pre>Example:{@code
   * minioClient.removeBucket(RemoveBucketArgs.builder().bucket("my-bucketname").build());
   * }</pre>
   *
   * @param args {@link RemoveBucketArgs} bucket.
   */
  default void removeBucket(RemoveBucketArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .removeBucket(args));
  }

  /**
   * Removes incomplete uploads of an object.
   *
   * <pre>Example:{@code
   * minioClient.removeIncompleteUpload(
   *     RemoveIncompleteUploadArgs.builder()
   *     .bucket("my-bucketname")
   *     .object("my-objectname")
   *     .build());
   * }</pre>
   *
   * @param args instance of {@link RemoveIncompleteUploadArgs}
   */
  default void removeIncompleteUpload(RemoveIncompleteUploadArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .removeIncompleteUpload(args));
  }

  /**
   * Sets encryption configuration of a bucket.
   *
   * <pre>Example:{@code
   * minioClient.setBucketEncryption(
   *     SetBucketEncryptionArgs.builder().bucket("my-bucketname").config(config).build());
   * }</pre>
   *
   * @param args {@link SetBucketEncryptionArgs} object.
   */
  default void setBucketEncryption(SetBucketEncryptionArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setBucketEncryption(args));
  }

  /**
   * Sets life-cycle configuration to a bucket.
   *
   * <pre>Example:{@code
   * // Lets consider variable 'lifeCycleXml' contains below XML String;
   * // <LifecycleConfiguration>
   * //   <Rule>
   * //     <ID>expire-bucket</ID>
   * //     <Prefix></Prefix>
   * //     <Status>Enabled</Status>
   * //     <Expiration>
   * //       <Days>365</Days>
   * //     </Expiration>
   * //   </Rule>
   * // </LifecycleConfiguration>
   * //
   * minioClient.setBucketLifeCycle(
   *     SetBucketLifeCycleArgs.builder().bucket("my-bucketname").config(lifeCycleXml).build());
   * }</pre>
   *
   * @param args {@link SetBucketLifeCycleArgs} object.
   */
  default void setBucketLifeCycle(SetBucketLifeCycleArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setBucketLifeCycle(args));
  }

  /**
   * Sets notification configuration to a bucket.
   *
   * <pre>Example:{@code
   * List<EventType> eventList = new LinkedList<>();
   * eventList.add(EventType.OBJECT_CREATED_PUT);
   * eventList.add(EventType.OBJECT_CREATED_COPY);
   *
   * QueueConfiguration queueConfiguration = new QueueConfiguration();
   * queueConfiguration.setQueue("arn:minio:sqs::1:webhook");
   * queueConfiguration.setEvents(eventList);
   * queueConfiguration.setPrefixRule("images");
   * queueConfiguration.setSuffixRule("pg");
   *
   * List<QueueConfiguration> queueConfigurationList = new LinkedList<>();
   * queueConfigurationList.add(queueConfiguration);
   *
   * NotificationConfiguration config = new NotificationConfiguration();
   * config.setQueueConfigurationList(queueConfigurationList);
   *
   * minioClient.setBucketNotification(
   *     SetBucketNotificationArgs.builder().bucket("my-bucketname").config(config).build());
   * }</pre>
   *
   * @param args {@link SetBucketNotificationArgs} object.
   */
  default void setBucketNotification(SetBucketNotificationArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setBucketNotification(args));
  }

  /**
   * Sets bucket policy configuration to a bucket.
   *
   * <pre>Example:{@code
   * // Assume policyJson contains below JSON string;
   * // {
   * //     "Statement": [
   * //         {
   * //             "Action": [
   * //                 "s3:GetBucketLocation",
   * //                 "s3:ListBucket"
   * //             ],
   * //             "Effect": "Allow",
   * //             "Principal": "*",
   * //             "Resource": "arn:aws:s3:::my-bucketname"
   * //         },
   * //         {
   * //             "Action": "s3:GetObject",
   * //             "Effect": "Allow",
   * //             "Principal": "*",
   * //             "Resource": "arn:aws:s3:::my-bucketname/myobject*"
   * //         }
   * //     ],
   * //     "Version": "2012-10-17"
   * // }
   * //
   * minioClient.setBucketPolicy(
   *     SetBucketPolicyArgs.builder().bucket("my-bucketname").config(policyJson).build());
   * }</pre>
   *
   * @param args {@link SetBucketPolicyArgs} object.
   */
  default void setBucketPolicy(SetBucketPolicyArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setBucketPolicy(args));
  }

  /**
   * Sets tags to a bucket.
   *
   * <pre>Example:{@code
   * Map<String, String> map = new HashMap<>();
   * map.put("Project", "Project One");
   * map.put("User", "jsmith");
   * minioClient.setBucketTags(
   *     SetBucketTagsArgs.builder().bucket("my-bucketname").tags(map).build());
   * }</pre>
   *
   * @param args {@link SetBucketTagsArgs} object.
   */
  default void setBucketTags(SetBucketTagsArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setBucketTags(args));
  }

  /**
   * Sets default object retention in a bucket.
   *
   * <pre>Example:{@code
   * ObjectLockConfiguration config = new ObjectLockConfiguration(
   *     RetentionMode.COMPLIANCE, new RetentionDurationDays(100));
   * minioClient.setDefaultRetention(
   *     SetDefaultRetentionArgs.builder().bucket("my-bucketname").config(config).build());
   * }</pre>
   *
   * @param args {@link SetDefaultRetentionArgs} object.
   */
  default void setDefaultRetention(SetDefaultRetentionArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setDefaultRetention(args));
  }

  // Object operations

  /**
   * Creates an object by combining data from different source objects using server-side copy.
   *
   * <pre>Example:{@code
   * List<ComposeSource> sourceObjectList = new ArrayList<ComposeSource>();
   *
   * sourceObjectList.add(
   *    ComposeSource.builder().bucket("my-job-bucket").object("my-objectname-part-one").build());
   * sourceObjectList.add(
   *    ComposeSource.builder().bucket("my-job-bucket").object("my-objectname-part-two").build());
   * sourceObjectList.add(
   *    ComposeSource.builder().bucket("my-job-bucket").object("my-objectname-part-three").build());
   *
   * // Create my-bucketname/my-objectname by combining source object list.
   * minioClient.composeObject(
   *    ComposeObjectArgs.builder()
   *        .bucket("my-bucketname")
   *        .object("my-objectname")
   *        .sources(sourceObjectList)
   *        .build());
   * }</pre>
   *
   * @param args {@link ComposeObjectArgs} object.
   */
  default ObjectWriteResponse composeObject(ComposeObjectArgs args) {
    return execute(minioClient -> minioClient.composeObject(args));
  }

  /**
   * Creates an object by server-side copying data from another object.
   *
   * @param args {@link CopyObjectArgs} object.
   */
  default ObjectWriteResponse copyObject(CopyObjectArgs args) {
    return execute(minioClient -> minioClient.copyObject(args));
  }

  /**
   * Deletes tags of an object.
   *
   * <pre>Example:{@code
   * minioClient.deleteObjectTags(
   *     DeleteObjectTags.builder().bucket("my-bucketname").object("my-objectname").build());
   * }</pre>
   *
   * @param args {@link DeleteObjectTagsArgs} object.
   */
  default void deleteObjectTags(DeleteObjectTagsArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .deleteObjectTags(args));
  }

  /**
   * Disables legal hold on an object.
   *
   * <pre>Example:{@code
   * minioClient.disableObjectLegalHold(
   *    DisableObjectLegalHoldArgs.builder()
   *        .bucket("my-bucketname")
   *        .object("my-objectname")
   *        .versionId("object-versionId")
   *        .build());
   * }</pre>
   *
   * @param args {@link DisableObjectLegalHoldArgs} object.
   */
  default void disableObjectLegalHold(DisableObjectLegalHoldArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .disableObjectLegalHold(args));
  }

  /**
   * Enables legal hold on an object.
   *
   * <pre>Example:{@code
   * minioClient.enableObjectLegalHold(
   *    EnableObjectLegalHoldArgs.builder()
   *        .bucket("my-bucketname")
   *        .object("my-objectname")
   *        .versionId("object-versionId")
   *        .build());
   * }</pre>
   *
   * @param args {@link EnableObjectLegalHoldArgs} object.
   */
  default void enableObjectLegalHold(EnableObjectLegalHoldArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .enableObjectLegalHold(args));
  }

  /**
   * Gets data from offset to length of a SSE-C encrypted object. Returned {@link InputStream} must
   * be closed after use to release network resources.
   *
   * <pre>Example:{@code
   * try (InputStream stream =
   *     minioClient.getObject(
   *   GetObjectArgs.builder()
   *     .bucket("my-bucketname")
   *     .object("my-objectname")
   *     .offset(offset)
   *     .length(len)
   *     .ssec(ssec)
   *     .build()
   * ) {
   *   // Read data from stream
   * }
   * }</pre>
   *
   * @param args Object of {@link GetObjectArgs}
   */
  default InputStream getObject(GetObjectArgs args) {
    return execute(minioClient -> minioClient.getObject(args));
  }

  /**
   * Downloads data of a SSE-C encrypted object to file.
   *
   * <pre>Example:{@code
   * minioClient.downloadObject(
   *   GetObjectArgs.builder()
   *     .bucket("my-bucketname")
   *     .object("my-objectname")
   *     .ssec(ssec)
   *     .fileName("my-filename")
   *     .build());
   * }</pre>
   *
   * @param args Object of {@link DownloadObjectArgs}
   */
  default void downloadObject(DownloadObjectArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .downloadObject(args));
  }

  /**
   * Gets retention configuration of an object.
   *
   * <pre>Example:{@code
   * Retention retention =
   *     minioClient.getObjectRetention(GetObjectRetentionArgs.builder()
   *        .bucket(bucketName)
   *        .object(objectName)
   *        .versionId(versionId)
   *        .build()););
   * System.out.println(
   *     "mode: " + retention.mode() + "until: " + retention.retainUntilDate());
   * }</pre>
   *
   * @param args {@link GetObjectRetentionArgs} object.
   * @return {@link Retention} - Object retention configuration.
   */
  default Retention getObjectRetention(GetObjectRetentionArgs args) {
    return execute(minioClient -> minioClient.getObjectRetention(args));
  }

  /**
   * Gets tags of an object.
   *
   * <pre>Example:{@code
   * Tags tags =
   *     minioClient.getObjectTags(
   *         GetObjectTagsArgs.builder().bucket("my-bucketname").object("my-objectname").build());
   * }</pre>
   *
   * @param args {@link GetObjectTagsArgs} object.
   * @return {@link Tags} - Tags.
   */
  default Tags getObjectTags(GetObjectTagsArgs args) {
    return execute(minioClient -> minioClient.getObjectTags(args));
  }

  /**
   * Gets URL of an object useful when this object has public read access.
   *
   * <pre>Example:{@code
   * String url = minioClient.getObjectUrl("my-bucketname", "my-objectname");
   * }</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @return String - URL string.
   */
  default String getObjectUrl(String bucketName, String objectName) {
    return execute(minioClient -> minioClient.getObjectUrl(bucketName, objectName));
  }

  /**
   * Gets presigned URL of an object for HTTP method, expiry time and custom request parameters.
   *
   * <pre>Example:{@code
   * // Get presigned URL string to delete 'my-objectname' in 'my-bucketname' and its life time
   * // is one day.
   * String url =
   *    minioClient.getPresignedObjectUrl(
   *        GetPresignedObjectUrlArgs.builder()
   *            .method(Method.DELETE)
   *            .bucket("my-bucketname")
   *            .object("my-objectname")
   *            .expiry(24 * 60 * 60)
   *            .build());
   * System.out.println(url);
   *
   * // Get presigned URL string to upload 'my-objectname' in 'my-bucketname'
   * // with response-content-type as application/json and life time as one day.
   * Map<String, String> reqParams = new HashMap<String, String>();
   * reqParams.put("response-content-type", "application/json");
   *
   * String url =
   *    minioClient.getPresignedObjectUrl(
   *        GetPresignedObjectUrlArgs.builder()
   *            .method(Method.PUT)
   *            .bucket("my-bucketname")
   *            .object("my-objectname")
   *            .expiry(1, TimeUnit.DAYS)
   *            .extraQueryParams(reqParams)
   *            .build());
   * System.out.println(url);
   *
   * // Get presigned URL string to download 'my-objectname' in 'my-bucketname' and its life time
   * // is 2 hours.
   * String url =
   *    minioClient.getPresignedObjectUrl(
   *        GetPresignedObjectUrlArgs.builder()
   *            .method(Method.GET)
   *            .bucket("my-bucketname")
   *            .object("my-objectname")
   *            .expiry(2, TimeUnit.HOURS)
   *            .build());
   * System.out.println(url);
   * }</pre>
   *
   * @param args {@link GetPresignedObjectUrlArgs} object.
   * @return String - URL string.
   */
  default String getPresignedObjectUrl(GetPresignedObjectUrlArgs args) {
    return execute(minioClient -> minioClient.getPresignedObjectUrl(args));
  }

  /**
   * Returns true if legal hold is enabled on an object.
   *
   * <pre>Example:{@code
   * boolean status =
   *     s3Client.isObjectLegalHoldEnabled(
   *        IsObjectLegalHoldEnabledArgs.builder()
   *             .bucket("my-bucketname")
   *             .object("my-objectname")
   *             .versionId("object-versionId")
   *             .build());
   * if (status) {
   *   System.out.println("Legal hold is on");
   *  } else {
   *   System.out.println("Legal hold is off");
   *  }
   * }</pre>
   * <p>
   * args {@link IsObjectLegalHoldEnabledArgs} object.
   *
   * @return boolean - True if legal hold is enabled.
   */
  default boolean isObjectLegalHoldEnabled(IsObjectLegalHoldEnabledArgs args) {
    return execute(minioClient -> minioClient.isObjectLegalHoldEnabled(args));
  }

  /**
   * Gets form-data of {@link PostPolicy} of an object to upload its data using POST method.
   *
   * <pre>Example:{@code
   * PostPolicy policy = new PostPolicy("my-bucketname", "my-objectname",
   *     ZonedDateTime.now().plusDays(7));
   *
   * // 'my-objectname' should be 'image/png' content type
   * policy.setContentType("image/png");
   *
   * // set success action status to 201 to receive XML document
   * policy.setSuccessActionStatus(201);
   *
   * Map<String,String> formData = minioClient.presignedPostPolicy(policy);
   *
   * // Print curl command to be executed by anonymous user to upload /tmp/userpic.png.
   * System.out.print("curl -X POST ");
   * for (Map.Entry<String,String> entry : formData.entrySet()) {
   *   System.out.print(" -F " + entry.getKey() + "=" + entry.getValue());
   * }
   * System.out.println(" -F file=@/tmp/userpic.png https://play.min.io/my-bucketname");
   * }</pre>
   *
   * @param policy Post policy of an object.
   * @return Map&ltString, String&gt - Contains form-data to upload an object using POST method.
   */
  default Map<String, String> presignedPostPolicy(PostPolicy policy) {
    return execute(minioClient -> minioClient.presignedPostPolicy(policy));
  }

  /**
   * Uploads data from a stream to an object.
   *
   * <pre>Example:{@code
   * // Upload known sized input stream.
   * minioClient.putObject(
   *     PutObjectArgs.builder().bucket("my-bucketname").object("my-objectname").stream(
   *             inputStream, size, -1)
   *         .contentType("video/mp4")
   *         .build());
   *
   * // Upload unknown sized input stream.
   * minioClient.putObject(
   *     PutObjectArgs.builder().bucket("my-bucketname").object("my-objectname").stream(
   *             inputStream, -1, 10485760)
   *         .contentType("video/mp4")
   *         .build());
   *
   * // Create object ends with '/' (also called as folder or directory).
   * minioClient.putObject(
   *     PutObjectArgs.builder().bucket("my-bucketname").object("path/to/").stream(
   *             new ByteArrayInputStream(new byte[] {}), 0, -1)
   *         .build());
   *
   * // Upload input stream with headers and user metadata.
   * Map<String, String> headers = new HashMap<>();
   * headers.put("X-Amz-Storage-Class", "REDUCED_REDUNDANCY");
   * Map<String, String> userMetadata = new HashMap<>();
   * userMetadata.put("My-Project", "Project One");
   * minioClient.putObject(
   *     PutObjectArgs.builder().bucket("my-bucketname").object("my-objectname").stream(
   *             inputStream, size, -1)
   *         .headers(headers)
   *         .userMetadata(userMetadata)
   *         .build());
   *
   * // Upload input stream with server-side encryption.
   * minioClient.putObject(
   *     PutObjectArgs.builder().bucket("my-bucketname").object("my-objectname").stream(
   *             inputStream, size, -1)
   *         .sse(sse)
   *         .build());
   * }</pre>
   *
   * @param args {@link PutObjectArgs} object.
   * @return {@link ObjectWriteResponse} object.
   */
  default ObjectWriteResponse putObject(PutObjectArgs args) {
    return execute(minioClient -> minioClient.putObject(args));
  }

  /**
   * Uploads data from a file to an object.
   *
   * <pre>Example:{@code
   * // Upload an JSON file.
   * minioClient.uploadObject(
   *     UploadObjectArgs.builder()
   *         .bucket("my-bucketname").object("my-objectname").filename("person.json").build());
   *
   * // Upload a video file.
   * minioClient.uploadObject(
   *     UploadObjectArgs.builder()
   *         .bucket("my-bucketname")
   *         .object("my-objectname")
   *         .filename("my-video.avi")
   *         .contentType("video/mp4")
   *         .build());
   * }</pre>
   *
   * @param args {@link UploadObjectArgs} object.
   * @param deleteMode the delete mode
   * @return {@link ObjectWriteResponse} object.
   */
  default ObjectWriteResponse uploadObject(UploadObjectArgs args, DeleteMode deleteMode) {
    final Path file = Paths.get(args.filename());
    try {
      return execute(minioClient -> {
        ObjectWriteResponse response = minioClient.uploadObject(args);
        if (DeleteMode.ON_SUCCESS == deleteMode) {
          Files.delete(file);
        }
        return response;
      });

    } finally {
      if (DeleteMode.ALWAYS == deleteMode) {
        execute((MinioClientCallbackWithoutResult) minioClient -> Files.delete(file));
      }
    }
  }

  /**
   * Removes an object.
   *
   * <pre>Example:{@code
   * // Remove object.
   * minioClient.removeObject(
   *     RemoveObjectArgs.builder().bucket("my-bucketname").object("my-objectname").build());
   *
   * // Remove versioned object.
   * minioClient.removeObject(
   *     RemoveObjectArgs.builder()
   *         .bucket("my-bucketname")
   *         .object("my-versioned-objectname")
   *         .versionId("my-versionid")
   *         .build());
   *
   * // Remove versioned object bypassing Governance mode.
   * minioClient.removeObject(
   *     RemoveObjectArgs.builder()
   *         .bucket("my-bucketname")
   *         .object("my-versioned-objectname")
   *         .versionId("my-versionid")
   *         .bypassRetentionMode(true)
   *         .build());
   * }</pre>
   *
   * @param args {@link RemoveObjectArgs} object.
   */
  default void removeObject(RemoveObjectArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .removeObject(args));
  }

  /**
   * Removes multiple objects lazily. Its required to iterate the returned Iterable to perform
   * removal.
   *
   * <pre>Example:{@code
   * List<DeleteObject> objects = new LinkedList<>();
   * objects.add(new DeleteObject("my-objectname1"));
   * objects.add(new DeleteObject("my-objectname2"));
   * objects.add(new DeleteObject("my-objectname3"));
   * Iterable<Result<DeleteError>> results =
   *     minioClient.removeObjects(
   *         RemoveObjectsArgs.builder().bucket("my-bucketname").objects(objects).build());
   * for (Result<DeleteError> result : results) {
   *   DeleteError error = errorResult.get();
   *   System.out.println(
   *       "Error in deleting object " + error.objectName() + "; " + error.message());
   * }
   * }</pre>
   *
   * @param args {@link RemoveObjectsArgs} object.
   * @return Iterable&ltResult&ltDeleteError&gt&gt - Lazy iterator contains object removal status.
   */
  default Iterable<Result<DeleteError>> removeObjects(RemoveObjectsArgs args) {
    return execute(minioClient -> minioClient.removeObjects(args));
  }

  /**
   * Selects content of an object by SQL expression.
   *
   * <pre>Example:{@code
   * String sqlExpression = "select * from S3Object";
   * InputSerialization is =
   *     new InputSerialization(null, false, null, null, FileHeaderInfo.USE, null, null,
   *         null);
   * OutputSerialization os =
   *     new OutputSerialization(null, null, null, QuoteFields.ASNEEDED, null);
   * SelectResponseStream stream =
   *     minioClient.selectObjectContent(
   *       SelectObjectContentArgs.builder()
   *       .bucket("my-bucketname")
   *       .object("my-objectname")
   *       .sqlExpression(sqlExpression)
   *       .inputSerialization(is)
   *       .outputSerialization(os)
   *       .requestProgress(true)
   *       .build());
   *
   * byte[] buf = new byte[512];
   * int bytesRead = stream.read(buf, 0, buf.length);
   * System.out.println(new String(buf, 0, bytesRead, StandardCharsets.UTF_8));
   *
   * Stats stats = stream.stats();
   * System.out.println("bytes scanned: " + stats.bytesScanned());
   * System.out.println("bytes processed: " + stats.bytesProcessed());
   * System.out.println("bytes returned: " + stats.bytesReturned());
   *
   * stream.close();
   * }</pre>
   *
   * @param args instance of {@link SelectObjectContentArgs}
   */
  default SelectResponseStream selectObjectContent(SelectObjectContentArgs args) {
    return execute(minioClient -> minioClient.selectObjectContent(args));
  }

  /**
   * Sets retention configuration to an object.
   *
   * <pre>Example:{@code
   *  Retention retention = new Retention(
   *       RetentionMode.COMPLIANCE, ZonedDateTime.now().plusYears(1));
   *  minioClient.setObjectRetention(
   *      SetObjectRetentionArgs.builder()
   *          .bucket("my-bucketname")
   *          .object("my-objectname")
   *          .config(config)
   *          .bypassGovernanceMode(true)
   *          .build());
   * }</pre>
   *
   * @param args {@link SetObjectRetentionArgs} object.
   */
  default void setObjectRetention(SetObjectRetentionArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setObjectRetention(args));
  }

  /**
   * Sets tags to an object.
   *
   * <pre>Example:{@code
   * Map<String, String> map = new HashMap<>();
   * map.put("Project", "Project One");
   * map.put("User", "jsmith");
   * minioClient.setObjectTags(
   *     SetObjectTagsArgs.builder()
   *         .bucket("my-bucketname")
   *         .object("my-objectname")
   *         .tags((map)
   *         .build());
   * }</pre>
   *
   * @param args {@link SetObjectTagsArgs} object.
   */
  default void setObjectTags(SetObjectTagsArgs args) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setObjectTags(args));
  }

  /**
   * Gets information of an object.
   *
   * <pre>Example:{@code
   * // Get information of an object.
   * ObjectStat objectStat =
   *     minioClient.statObject(
   *         StatObjectArgs.builder().bucket("my-bucketname").object("my-objectname").build());
   *
   * // Get information of SSE-C encrypted object.
   * ObjectStat objectStat =
   *     minioClient.statObject(
   *         StatObjectArgs.builder()
   *             .bucket("my-bucketname")
   *             .object("my-objectname")
   *             .ssec(ssec)
   *             .build());
   *
   * // Get information of a versioned object.
   * ObjectStat objectStat =
   *     minioClient.statObject(
   *         StatObjectArgs.builder()
   *             .bucket("my-bucketname")
   *             .object("my-objectname")
   *             .versionId("version-id")
   *             .build());
   *
   * // Get information of a SSE-C encrypted versioned object.
   * ObjectStat objectStat =
   *     minioClient.statObject(
   *         StatObjectArgs.builder()
   *             .bucket("my-bucketname")
   *             .object("my-objectname")
   *             .versionId("version-id")
   *             .ssec(ssec)
   *             .build());
   * }</pre>
   *
   * @param args {@link StatObjectArgs} object.
   * @return {@link ObjectStat} - Populated object information and metadata.
   */
  default ObjectStat statObject(StatObjectArgs args) {
    return execute(minioClient -> minioClient.statObject(args));
  }

  /**
   * Check whether an object exists or not.
   *
   * @param args {@link StatObjectArgs} object.
   * @return {@code true} if the object exists, otherwise {@code false}
   */
  default boolean objectExists(StatObjectArgs args) {
    try {
      return statObject(args) != null;
    } catch (MinioException e) {
      if (404 == e.status()) {
        return false;
      }
      throw e;
    }
  }

}
