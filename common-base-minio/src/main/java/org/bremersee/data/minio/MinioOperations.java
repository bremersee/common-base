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

import io.minio.CloseableIterator;
import io.minio.ComposeSource;
import io.minio.CopyConditions;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.PostPolicy;
import io.minio.PutObjectOptions;
import io.minio.Result;
import io.minio.SelectResponseStream;
import io.minio.ServerSideEncryption;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.InputSerialization;
import io.minio.messages.Item;
import io.minio.messages.NotificationConfiguration;
import io.minio.messages.NotificationRecords;
import io.minio.messages.ObjectLockConfiguration;
import io.minio.messages.OutputSerialization;
import io.minio.messages.Retention;
import io.minio.messages.Upload;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;

/**
 * The minio operations.
 *
 * @author Christian Bremer
 */
@Validated
public interface MinioOperations {

  /**
   * The constant DEFAULT_EXPIRY_TIME.
   */
  int DEFAULT_EXPIRY_TIME = 7 * 24 * 3600;

  /**
   * Execute minio callback.
   *
   * @param <T> the type of the result
   * @param callback the callback
   * @return the result
   */
  <T> T execute(MinioClientCallback<T> callback);

  /**
   * Lists bucket information of all buckets.
   *
   * <pre>Example:{@code
   * List<Bucket> bucketList = minioOperations.listBuckets();
   * for (Bucket bucket : bucketList) {
   *   System.out.println(bucket.creationDate() + ", " + bucket.name());
   * }
   * }*</pre>
   *
   * @return List &ltBucket&gt - List of bucket information.
   */
  default List<Bucket> listBuckets() {
    return execute(MinioClient::listBuckets);
  }

  /**
   * Checks if a bucket exists.
   *
   * <pre>Example:{@code
   * boolean found = minioOperations.bucketExists("my-bucketname");
   * if (found) {
   *   System.out.println("my-bucketname exists");
   * } else {
   *   System.out.println("my-bucketname does not exist");
   * }
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @return boolean - True if the bucket exists.
   */
  default boolean bucketExists(String bucketName) {
    return execute(minioClient -> minioClient.bucketExists(bucketName));
  }

  /**
   * Creates a bucket with default region.
   *
   * <pre>Example:{@code
   * minioOperations.makeBucket("my-bucketname");
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   */
  default void makeBucket(@NotEmpty String bucketName) {
    this.makeBucket(bucketName, null);
  }

  /**
   * Creates a bucket with given region.
   *
   * <pre>Example:{@code
   * minioOperations.makeBucket("my-bucketname", "eu-west-1");
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param region Region in which the bucket will be created.
   */
  default void makeBucket(
      @NotEmpty String bucketName,
      @Nullable String region) {
    this.makeBucket(bucketName, region, false);
  }

  /**
   * Creates a bucket with object lock feature enabled.
   *
   * <pre>Example:{@code
   * minioOperations.makeBucket("my-bucketname", "eu-west-2", true);
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param region Region in which the bucket will be created.
   * @param objectLock Flag to enable object lock feature.
   */
  default void makeBucket(
      @NotEmpty String bucketName,
      @Nullable String region,
      boolean objectLock) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .makeBucket(bucketName, region, objectLock));
  }

  /**
   * Enables object versioning feature in a bucket.
   *
   * <pre>Example:{@code
   * minioOperations.enableVersioning("my-bucketname");
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   */
  default void enableVersioning(@NotEmpty String bucketName) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .enableVersioning(bucketName));
  }

  /**
   * Disables object versioning feature in a bucket.
   *
   * <pre>Example:{@code
   * minioOperations.disableVersioning("my-bucketname");
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   */
  default void disableVersioning(@NotEmpty String bucketName) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .disableVersioning(bucketName));
  }

  /**
   * Sets default object retention in a bucket.
   *
   * <pre>Example:{@code
   * ObjectLockConfiguration config = new ObjectLockConfiguration(
   *     RetentionMode.COMPLIANCE, new RetentionDurationDays(100));
   * minioOperations.setDefaultRetention("my-bucketname", config);
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param config Object lock configuration.
   */
  default void setDefaultRetention(@NotEmpty String bucketName, ObjectLockConfiguration config) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setDefaultRetention(bucketName, config));
  }

  /**
   * Gets default object retention in a bucket.
   *
   * <pre>Example:{@code
   * // bucket must be created with object lock enabled.
   * minioOperations.makeBucket("my-bucketname", null, true);
   * ObjectLockConfiguration config = minioOperations.getDefaultRetention("my-bucketname");
   * System.out.println("Mode: " + config.mode());
   * System.out.println(
   *     "Duration: " + config.duration().duration() + " " + config.duration().unit());
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @return {@link ObjectLockConfiguration} - Default retention configuration.
   */
  default ObjectLockConfiguration getDefaultRetention(String bucketName) {
    return execute(minioClient -> minioClient.getDefaultRetention(bucketName));
  }

  /**
   * Sets retention configuration to an object.
   *
   * <pre>Example:{@code
   * Retention retention =
   *     new Retention(RetentionMode.COMPLIANCE, ZonedDateTime.now().plusYears(1));
   * minioOperations.setObjectRetention(
   *     "my-bucketname", "my-objectname", null, retention, true);
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @param versionId Version ID of the object.
   * @param config Object retention configuration.
   * @param bypassGovernanceRetention Bypass Governance retention.
   */
  default void setObjectRetention(
      String bucketName,
      String objectName,
      String versionId,
      Retention config,
      boolean bypassGovernanceRetention) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setObjectRetention(bucketName, objectName, versionId, config, bypassGovernanceRetention));
  }

  /**
   * Gets retention configuration of an object.
   *
   * <pre>Example:{@code
   * Retention retention =
   *     minioOperations.getObjectRetention("my-bucketname", "my-objectname", null);
   * System.out.println(
   *     "mode: " + retention.mode() + "until: " + retention.retainUntilDate());
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @param versionId Version ID of the object.
   * @return object retention configuration.
   */
  default Retention getObjectRetention(String bucketName, String objectName, String versionId) {
    return execute(minioClient -> minioClient
        .getObjectRetention(bucketName, objectName, versionId));
  }

  /**
   * Enables legal hold on an object.
   *
   * <pre>Example:{@code
   * minioOperations.enableObjectLegalHold("my-bucketname", "my-object", null);
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @param versionId Version ID of the object.
   */
  default void enableObjectLegalHold(String bucketName, String objectName, String versionId) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .enableObjectLegalHold(bucketName, objectName, versionId));
  }

  /**
   * Disables legal hold on an object.
   *
   * <pre>Example:{@code
   * minioOperations.disableObjectLegalHold("my-bucketname", "my-object", null);
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @param versionId Version ID of the object.
   */
  default void disableObjectLegalHold(String bucketName, String objectName, String versionId) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .disableObjectLegalHold(bucketName, objectName, versionId));
  }

  /**
   * Returns true if legal hold is enabled on an object.
   *
   * <pre>Example:{@code
   * boolean status =
   *     s3Client.isObjectLegalHoldEnabled("my-bucketname", "my-objectname", null);
   * if (status) {
   *   System.out.println("Legal hold is on");
   * } else {
   *   System.out.println("Legal hold is off");
   * }
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @param versionId Version ID of the object.
   * @return boolean - True if legal hold is enabled.
   */
  default boolean isObjectLegalHoldEnabled(String bucketName, String objectName, String versionId) {
    return execute(minioClient -> minioClient
        .isObjectLegalHoldEnabled(bucketName, objectName, versionId));
  }

  /**
   * Removes an empty bucket.
   *
   * <pre>Example:{@code
   * minioOperations.removeBucket("my-bucketname");
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   */
  default void removeBucket(String bucketName) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .removeBucket(bucketName));
  }

  /**
   * Gets bucket policy configuration of a bucket.
   *
   * <pre>Example:{@code
   * String config = minioOperations.getBucketPolicy("my-bucketname");
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @return String - Bucket policy configuration as JSON string.
   */
  default String getBucketPolicy(String bucketName) {
    return execute(minioClient -> minioClient.getBucketPolicy(bucketName));
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
   * minioOperations.setBucketPolicy("my-bucketname", policyJson);
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param policy Bucket policy configuration as JSON string.
   */
  default void setBucketPolicy(String bucketName, String policy) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setBucketPolicy(bucketName, policy));
  }

  /**
   * Sets life cycle configuration to a bucket.
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
   * minioOperations.setBucketLifecycle("my-bucketname", lifeCycleXml);
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param lifeCycle Life cycle configuraion as XML string.
   */
  default void setBucketLifeCycle(String bucketName, String lifeCycle) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setBucketLifeCycle(bucketName, lifeCycle));
  }

  /**
   * Deletes life cycle configuration of a bucket.
   *
   * <pre>Example:{@code
   * deleteBucketLifeCycle("my-bucketname");
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   */
  default void deleteBucketLifeCycle(String bucketName) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .deleteBucketLifeCycle(bucketName));
  }

  /**
   * Gets life cycle configuration of a bucket.
   *
   * <pre>Example:{@code
   * String lifecycle = minioOperations.getBucketLifecycle("my-bucketname");
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @return String - Life cycle configuration as XML string.
   */
  default String getBucketLifeCycle(String bucketName) {
    return execute(minioClient -> minioClient.getBucketLifeCycle(bucketName));
  }

  /**
   * Gets notification configuration of a bucket.
   *
   * <pre>Example:{@code
   * NotificationConfiguration config =
   *     minioOperations.getBucketNotification("my-bucketname");
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @return {@link NotificationConfiguration} - Notification configuration.
   */
  default NotificationConfiguration getBucketNotification(String bucketName) {
    return execute(minioClient -> minioClient.getBucketNotification(bucketName));
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
   * minioOperations.setBucketNotification("my-bucketname", config);
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param notificationConfiguration {@link NotificationConfiguration} to be set.
   */
  default void setBucketNotification(
      String bucketName, NotificationConfiguration notificationConfiguration) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .setBucketNotification(bucketName, notificationConfiguration));
  }

  /**
   * Removes notification configuration of a bucket.
   *
   * <pre>Example:{@code
   * minioOperations.removeAllBucketNotification("my-bucketname");
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   */
  default void removeAllBucketNotification(String bucketName) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .removeAllBucketNotification(bucketName));
  }

  /**
   * Lists incomplete object upload information of a bucket.
   *
   * <pre>Example:{@code
   * Iterable<Result<Upload>> results =
   *     minioOperations.listIncompleteUploads("my-bucketname");
   * for (Result<Upload> result : results) {
   *   Upload upload = result.get();
   *   System.out.println(upload.uploadId() + ", " + upload.objectName());
   * }
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @return Iterable &ltResult&ltUpload&gt&gt - Lazy iterator contains object upload information.
   * @see #listIncompleteUploads(String, String, boolean) #listIncompleteUploads(String, String,
   *     boolean)
   */
  default Iterable<Result<Upload>> listIncompleteUploads(String bucketName) {
    return execute(minioClient -> minioClient.listIncompleteUploads(bucketName));
  }

  /**
   * Lists incomplete object upload information of a bucket for prefix.
   *
   * <pre>Example:{@code
   * Iterable<Result<Upload>> results =
   *     minioOperations.listIncompleteUploads("my-bucketname", "my-obj");
   * for (Result<Upload> result : results) {
   *   Upload upload = result.get();
   *   System.out.println(upload.uploadId() + ", " + upload.objectName());
   * }
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param prefix Object name starts with prefix.
   * @return Iterable &ltResult&ltUpload&gt&gt - Lazy iterator contains object upload information.
   * @see #listIncompleteUploads(String, String, boolean) #listIncompleteUploads(String, String,
   *     boolean)
   */
  default Iterable<Result<Upload>> listIncompleteUploads(String bucketName, String prefix) {
    return execute(minioClient -> minioClient.listIncompleteUploads(bucketName, prefix));
  }

  /**
   * Lists incomplete object upload information of a bucket for prefix recursively.
   *
   * <pre>Example:{@code
   * Iterable<Result<Upload>> results =
   *     minioOperations.listIncompleteUploads("my-bucketname", "my-obj", true);
   * for (Result<Upload> result : results) {
   *   Upload upload = result.get();
   *   System.out.println(upload.uploadId() + ", " + upload.objectName());
   * }
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param prefix Object name starts with prefix.
   * @param recursive List recursively than directory structure emulation.
   * @return Iterable &ltResult&ltUpload&gt&gt - Lazy iterator contains object upload information.
   * @see #listIncompleteUploads(String bucketName) #listIncompleteUploads(String bucketName)
   * @see #listIncompleteUploads(String bucketName, String prefix) #listIncompleteUploads(String
   *     bucketName, String prefix)
   */
  default Iterable<Result<Upload>> listIncompleteUploads(
      String bucketName, String prefix, boolean recursive) {
    return execute(minioClient -> minioClient.listIncompleteUploads(bucketName, prefix, recursive));
  }

  /**
   * Removes incomplete uploads of an object.
   *
   * <pre>Example:{@code
   * minioOperations.removeIncompleteUpload("my-bucketname", "my-objectname");
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   */
  default void removeIncompleteUpload(String bucketName, String objectName) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .removeIncompleteUpload(bucketName, objectName));
  }

  /**
   * Listens events of object prefix and suffix of a bucket. The returned closable iterator is
   * lazily evaluated hence its required to iterate to get new records and must be used with
   * try-with-resource to release underneath network resources.
   *
   * <pre>Example:{@code
   * String[] events = {"s3:ObjectCreated:*", "s3:ObjectAccessed:*"};
   * try (CloseableIterator<Result<NotificationInfo>> ci =
   *     minioOperations.listenBucketNotification("bcketName", "", "", events)) {
   *   while (ci.hasNext()) {
   *     NotificationRecords records = ci.next().get();
   *     for (Event event : records.events()) {
   *       System.out.println("Event " + event.eventType() + " occurred at "
   *           + event.eventTime() + " for " + event.bucketName() + "/"
   *           + event.objectName());
   *     }
   *   }
   * }
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param prefix Listen events of object starts with prefix.
   * @param suffix Listen events of object ends with suffix.
   * @param events Events to listen.
   * @return CloseableIterator &ltResult&ltNotificationRecords&gt&gt - Lazy closable iterator
   *     contains event records.
   */
  default CloseableIterator<Result<NotificationRecords>> listenBucketNotification(
      String bucketName, String prefix, String suffix, String[] events) {
    return execute(minioClient -> minioClient
        .listenBucketNotification(bucketName, prefix, suffix, events));
  }

  /**
   * Selects content of a object by SQL expression.
   *
   * <pre>Example:{@code
   * String sqlExpression = "select * from S3Object";
   * InputSerialization is =
   *     new InputSerialization(null, false, null, null, FileHeaderInfo.USE, null, null,
   *         null);
   * OutputSerialization os =
   *     new OutputSerialization(null, null, null, QuoteFields.ASNEEDED, null);
   * SelectResponseStream stream =
   *     minioOperations.selectObjectContent("my-bucketname", "my-objectName", sqlExpression,
   *         is, os, true, null, null, null);
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
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @param sqlExpression SQL expression.
   * @param is Input specification of object data.
   * @param os Output specification of result.
   * @param requestProgress Flag to request progress information.
   * @param scanStartRange scan start range of the object.
   * @param scanEndRange scan end range of the object.
   * @param sse SSE-C type server-side encryption.
   * @return contains filtered records and progress.
   */
  default SelectResponseStream selectObjectContent(
      String bucketName,
      String objectName,
      String sqlExpression,
      InputSerialization is,
      OutputSerialization os,
      boolean requestProgress,
      Long scanStartRange,
      Long scanEndRange,
      ServerSideEncryption sse) {

    return execute(minioClient -> minioClient.selectObjectContent(
        bucketName,
        objectName,
        sqlExpression,
        is,
        os,
        requestProgress,
        scanStartRange,
        scanEndRange,
        sse));
  }

  /**
   * Lists object information of a bucket.
   *
   * <pre>Example:{@code
   * Iterable<Result<Item>> results = minioOperations.listObjects("my-bucketname");
   * for (Result<Item> result : results) {
   *   Item item = result.get();
   *   System.out.println(
   *       item.lastModified() + ", " + item.size() + ", " + item.objectName());
   * }
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @return Iterable &ltResult&ltItem&gt&gt - Lazy iterator contains object information.
   * @throws XmlParserException the xml parser exception
   */
  default Iterable<Result<Item>> listObjects(final String bucketName) throws XmlParserException {
    return listObjects(bucketName, null);
  }

  /**
   * Lists object information of a bucket for prefix.
   *
   * <pre>Example:{@code
   * Iterable<Result<Item>> results = minioOperations.listObjects("my-bucketname", "my-obj");
   * for (Result<Item> result : results) {
   *   Item item = result.get();
   *   System.out.println(
   *       item.lastModified() + ", " + item.size() + ", " + item.objectName());
   * }
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param prefix Object name starts with prefix.
   * @return Iterable &ltResult&ltItem&gt&gt - Lazy iterator contains object information.
   * @throws XmlParserException the xml parser exception
   */
  default Iterable<Result<Item>> listObjects(final String bucketName, final String prefix)
      throws XmlParserException {
    // list all objects recursively
    return listObjects(bucketName, prefix, true);
  }

  /**
   * Lists object information of a bucket for prefix recursively.
   *
   * <pre>Example:{@code
   * Iterable<Result<Item>> results =
   *     minioOperations.listObjects("my-bucketname", "my-obj", true);
   * for (Result<Item> result : results) {
   *   Item item = result.get();
   *   System.out.println(
   *       item.lastModified() + ", " + item.size() + ", " + item.objectName());
   * }
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param prefix Object name starts with prefix.
   * @param recursive List recursively than directory structure emulation.
   * @return Iterable &ltResult&ltItem&gt&gt - Lazy iterator contains object information.
   * @see #listObjects(String bucketName) #listObjects(String bucketName)
   * @see #listObjects(String bucketName, String prefix) #listObjects(String bucketName, String
   *     prefix)
   * @see #listObjects(String bucketName, String prefix, boolean recursive, boolean useVersion1)
   *     #listObjects(String bucketName, String prefix, boolean recursive, boolean useVersion1)
   */
  default Iterable<Result<Item>> listObjects(
      final String bucketName, final String prefix, final boolean recursive) {
    return listObjects(bucketName, prefix, recursive, false);
  }

  /**
   * Lists object information of a bucket for prefix recursively using S3 API version 1.
   *
   * <pre>Example:{@code
   * Iterable<Result<Item>> results =
   *     minioOperations.listObjects("my-bucketname", "my-obj", true, true);
   * for (Result<Item> result : results) {
   *   Item item = result.get();
   *   System.out.println(
   *       item.lastModified() + ", " + item.size() + ", " + item.objectName());
   * }
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param prefix Object name starts with prefix.
   * @param recursive List recursively than directory structure emulation.
   * @param useVersion1 when true, version 1 of REST API is used.
   * @return Iterable &ltResult&ltItem&gt&gt - Lazy iterator contains object information.
   * @see #listObjects(String bucketName) #listObjects(String bucketName)
   * @see #listObjects(String bucketName, String prefix) #listObjects(String bucketName, String
   *     prefix)
   * @see #listObjects(String bucketName, String prefix, boolean recursive) #listObjects(String
   *     bucketName, String prefix, boolean recursive)
   */
  default Iterable<Result<Item>> listObjects(
      final String bucketName,
      final String prefix,
      final boolean recursive,
      final boolean useVersion1) {
    return listObjects(bucketName, prefix, recursive, false, false);
  }

  /**
   * Lists object information with user metadata of a bucket for prefix recursively using S3 API
   * version 1.
   *
   * <pre>Example:{@code
   * Iterable<Result<Item>> results =
   *     minioOperations.listObjects("my-bucketname", "my-obj", true, true, false);
   * for (Result<Item> result : results) {
   *   Item item = result.get();
   *   System.out.println(
   *       item.lastModified() + ", " + item.size() + ", " + item.objectName());
   * }
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param prefix Object name starts with prefix.
   * @param recursive List recursively than directory structure emulation.
   * @param includeUserMetadata include user metadata of each object. This is MinIO specific
   *     extension to ListObjectsV2.
   * @param useVersion1 when true, version 1 of REST API is used.
   * @return Iterable &ltResult&ltItem&gt&gt - Lazy iterator contains object information.
   * @see #listObjects(String bucketName) #listObjects(String bucketName)
   * @see #listObjects(String bucketName, String prefix) #listObjects(String bucketName, String
   *     prefix)
   * @see #listObjects(String bucketName, String prefix, boolean recursive) #listObjects(String
   *     bucketName, String prefix, boolean recursive)
   */
  default Iterable<Result<Item>> listObjects(
      String bucketName,
      String prefix,
      boolean recursive,
      boolean includeUserMetadata,
      boolean useVersion1) {
    return execute(minioClient -> minioClient
        .listObjects(bucketName, prefix, recursive, includeUserMetadata, useVersion1));
  }

  /**
   * Uploads data from a file to an object using {@link PutObjectOptions}.
   *
   * <pre>Example:{@code
   * minioOperations.putObject("my-bucketname", "my-objectname", "my-filename", null);
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @param in Stream contains object data.
   * @param options {@link PutObjectOptions} to be used during upload.
   */
  default void putObject(
      String bucketName,
      String objectName,
      InputStream in,
      PutObjectOptions options) {

    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .putObject(bucketName, objectName, in, options));
  }

  /**
   * Gets data of an object. Returned {@link InputStream} must be closed after use to release
   * network resources.
   *
   * <pre>Example:{@code
   * try (InputStream stream =
   *     minioOperations.getObject("my-bucketname", "my-objectname")) {
   *   // Read data from stream
   * }
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @return {@link InputStream} - Contains object data.
   */
  default InputStream getObject(String bucketName, String objectName) {
    return getObject(bucketName, objectName, null, null, null);
  }

  /**
   * Gets data of a SSE-C encrypted object. Returned {@link InputStream} must be closed after use to
   * release network resources.
   *
   * <pre>Example:{@code
   * try (InputStream stream =
   *     minioOperations.getObject("my-bucketname", "my-objectname", ssec)) {
   *   // Read data from stream
   * }
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @param sse SSE-C type server-side encryption.
   * @return {@link InputStream} - Contains object data.
   */
  default InputStream getObject(String bucketName, String objectName, ServerSideEncryption sse) {
    return getObject(bucketName, objectName, null, null, sse);
  }

  /**
   * Gets data from offset of an object. Returned {@link InputStream} must be closed after use to
   * release network resources.
   *
   * <pre>Example:{@code
   * try (InputStream stream =
   *     minioOperations.getObject("my-bucketname", "my-objectname", 1024L)) {
   *   // Read data from stream
   * }
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @param offset Start byte position of object data.
   * @return {@link InputStream} - Contains object data.
   */
  default InputStream getObject(String bucketName, String objectName, long offset) {
    return getObject(bucketName, objectName, offset, null, null);
  }

  /**
   * Gets data from offset to length of an object. Returned {@link InputStream} must be closed after
   * use to release network resources.
   *
   * <pre>Example:{@code
   * try (InputStream stream =
   *     minioOperations.getObject("my-bucketname", "my-objectname", 1024L, 4096L)) {
   *   // Read data from stream
   * }
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @param offset Start byte position of object data.
   * @param length Number of bytes of object data from offset.
   * @return {@link InputStream} - Contains object data.
   */
  default InputStream getObject(String bucketName, String objectName, long offset, Long length) {
    return getObject(bucketName, objectName, offset, length, null);
  }

  /**
   * Gets data from offset to length of a SSE-C encrypted object. Returned {@link InputStream} must
   * be closed after use to release network resources.
   *
   * <pre>Example:{@code
   * try (InputStream stream =
   *     minioOperations.getObject("my-bucketname", "my-objectname", 1024L, 4096L, ssec)) {
   *   // Read data from stream
   * }
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @param offset Start byte position of object data.
   * @param length Number of bytes of object data from offset.
   * @param sse SSE-C type server-side encryption.
   * @return {@link InputStream} - Contains object data.
   */
  default InputStream getObject(
      @NotEmpty String bucketName,
      @NotEmpty String objectName,
      @Nullable Long offset,
      @Nullable Long length,
      @Nullable ServerSideEncryption sse) {

    return execute(minioClient -> minioClient
        .getObject(bucketName, objectName, offset, length, sse));
  }

  /**
   * Gets object information and metadata of an object.
   *
   * <pre>Example:{@code
   * ObjectStat objectStat = minioOperations.statObject("my-bucketname", "my-objectname");
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @return populated object information and metadata.
   */
  default ObjectStat statObject(String bucketName, String objectName) {
    return statObject(bucketName, objectName, null);
  }

  /**
   * Gets object information and metadata of a SSE-C encrypted object.
   *
   * <pre>Example:{@code
   * ObjectStat objectStat =
   *     minioOperations.statObject("my-bucketname", "my-objectname", ssec);
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @param sse SSE-C type server-side encryption.
   * @return populated object information and metadata.
   */
  default ObjectStat statObject(
      @NotEmpty String bucketName,
      @NotEmpty String objectName,
      @Nullable ServerSideEncryption sse) {
    return execute(minioClient -> minioClient.statObject(bucketName, objectName, sse));
  }

  /**
   * Gets URL of an object useful when this object has public read access.
   *
   * <pre>Example:{@code
   * String url = minioOperations.getObjectUrl("my-bucketname", "my-objectname");
   * }*</pre>
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
   * String url = minioOperations.getPresignedObjectUrl(Method.DELETE, "my-bucketname",
   *     "my-objectname", 24 * 60 * 60, reqParams);
   * }*</pre>
   *
   * @param method HTTP {@link Method} to generate presigned URL.
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @param expires Expiry duration; defaults to 7 days.
   * @param reqParams Request parameters to override. Supported headers are response-expires,
   *     response-content-type, response-cache-control and response-content-disposition.
   * @return String - URL string.
   */
  default String getPresignedObjectUrl(
      Method method,
      String bucketName,
      String objectName,
      Duration expires,
      Map<String, String> reqParams) {

    final int expireSecs;
    if (expires == null || expires.toSeconds() < 1L || expires.toSeconds() > DEFAULT_EXPIRY_TIME) {
      expireSecs = DEFAULT_EXPIRY_TIME;
    } else {
      expireSecs = (int) expires.toSeconds();
    }
    return execute(minioClient -> minioClient
        .getPresignedObjectUrl(method, bucketName, objectName, expireSecs, reqParams));
  }

  /**
   * Gets presigned URL of an object to download its data for expiry time and request parameters.
   *
   * <pre>Example:{@code
   * // Get presigned URL to download my-objectname data with one day expiry and request
   * // parameters.
   * String url = minioOperations.presignedGetObject("my-bucketname", "my-objectname",
   *     24 * 60 * 60, reqParams);
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @param expires Expiry in seconds; defaults to 7 days.
   * @param reqParams Request parameters to override. Supported headers are response-expires,
   *     response-content-type, response-cache-control and response-content-disposition.
   * @return String - URL string to download the object.
   */
  default String presignedGetObject(
      String bucketName, String objectName, Duration expires, Map<String, String> reqParams) {
    return getPresignedObjectUrl(Method.GET, bucketName, objectName, expires, reqParams);
  }

  /**
   * Gets presigned URL of an object to download its data for expiry time.
   *
   * <pre>Example:{@code
   * // Get presigned URL to download my-objectname data with one day expiry.
   * String url = minioOperations.presignedGetObject("my-bucketname", "my-objectname",
   *     24 * 60 * 60);
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @param expires Expiry in seconds; defaults to 7 days.
   * @return String - URL string to download the object.
   */
  default String presignedGetObject(String bucketName, String objectName, Duration expires) {
    return presignedGetObject(bucketName, objectName, expires, null);
  }

  /**
   * Gets presigned URL of an object to download its data for 7 days.
   *
   * <pre>Example:{@code
   * String url = minioOperations.presignedGetObject("my-bucketname", "my-objectname");
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @return String - URL string to download the object.
   */
  default String presignedGetObject(String bucketName, String objectName) {
    return presignedGetObject(bucketName, objectName, null, null);
  }

  /**
   * Gets presigned URL of an object to upload data for expiry time.
   *
   * <pre>Example:{@code
   * // Get presigned URL to upload data to my-objectname with one day expiry.
   * String url =
   *     minioOperations.presignedPutObject("my-bucketname", "my-objectname", 24 * 60 * 60);
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @param expires Expiry in seconds; defaults to 7 days.
   * @return String - URL string to upload an object.
   */
  default String presignedPutObject(String bucketName, String objectName, Duration expires) {
    return getPresignedObjectUrl(Method.PUT, bucketName, objectName, expires, null);
  }

  /**
   * Gets presigned URL of an object to upload data for 7 days.
   *
   * <pre>Example:{@code
   * String url = minioOperations.presignedPutObject("my-bucketname", "my-objectname");
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   * @return String - URL string to upload an object.
   */
  default String presignedPutObject(String bucketName, String objectName) {
    return presignedPutObject(bucketName, objectName, null);
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
   * Map<String,String> formData = minioOperations.presignedPostPolicy(policy);
   *
   * // Print curl command to be executed by anonymous user to upload /tmp/userpic.png.
   * System.out.print("curl -X POST ");
   * for (Map.Entry<String,String> entry : formData.entrySet()) {
   *   System.out.print(" -F " + entry.getKey() + "=" + entry.getValue());
   * }
   * System.out.println(" -F file=@/tmp/userpic.png https://play.min.io/my-bucketname");
   * }*</pre>
   *
   * @param policy Post policy of an object.
   * @return Map &ltString, String&gt - Contains form-data to upload an object using POST method.
   */
  default Map<String, String> presignedPostPolicy(PostPolicy policy) {
    return execute(minioClient -> minioClient.presignedPostPolicy(policy));
  }

  /**
   * Removes an object.
   *
   * <pre>Example:{@code
   * minioOperations.removeObject("my-bucketname", "my-objectname");
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name in the bucket.
   */
  default void removeObject(String bucketName, String objectName) {
    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .removeObject(bucketName, objectName));
  }

  /**
   * Removes multiple objects lazily. Its required to iterate the returned Iterable to perform
   * removal.
   *
   * <pre>Example:{@code
   * List<String> myObjectNames = new LinkedList<String>();
   * objectNames.add("my-objectname1");
   * objectNames.add("my-objectname2");
   * objectNames.add("my-objectname3");
   * Iterable<Result<DeleteError>> results =
   *     minioOperations.removeObjects("my-bucketname", myObjectNames);
   * for (Result<DeleteError> result : results) {
   *   DeleteError error = errorResult.get();
   *   System.out.println(
   *       "Error in deleting object " + error.objectName() + "; " + error.message());
   * }
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectNames List of Object names in the bucket.
   * @return Iterable &ltResult&ltDeleteError&gt&gt - Lazy iterator contains object removal status.
   */
  default Iterable<Result<DeleteError>> removeObjects(
      final String bucketName, final Iterable<String> objectNames) {
    return execute(minioClient -> minioClient.removeObjects(bucketName, objectNames));
  }

  /**
   * Creates an object by server-side copying data from another object.
   *
   * <pre>Example:{@code
   * // Copy data from my-source-bucketname/my-objectname to my-bucketname/my-objectname.
   * minioOperations.copyObject("my-bucketname", "my-objectname", null, null,
   *     "my-source-bucketname", null, null, null);
   *
   * // Copy data from my-source-bucketname/my-source-objectname to
   * // my-bucketname/my-objectname.
   * minioOperations.copyObject("my-bucketname", "my-objectname", null, null,
   *     "my-source-bucketname", "my-source-objectname", null, null);
   *
   * // Copy data from my-source-bucketname/my-objectname to my-bucketname/my-objectname
   * // by server-side encryption.
   * minioOperations.copyObject("my-bucketname", "my-objectname", null, sse,
   *     "my-source-bucketname", null, null, null);
   *
   * // Copy data from SSE-C encrypted my-source-bucketname/my-objectname to
   * // my-bucketname/my-objectname.
   * minioOperations.copyObject("my-bucketname", "my-objectname", null, null,
   *     "my-source-bucketname", null, srcSsec, null);
   *
   * // Copy data from my-source-bucketname/my-objectname to my-bucketname/my-objectname
   * // with user metadata and copy conditions.
   * minioOperations.copyObject("my-bucketname", "my-objectname", headers, null,
   *     "my-source-bucketname", null, null, conditions);
   * }*</pre>
   *
   * @param bucketName Name of the bucket.
   * @param objectName Object name to be created.
   * @param headerMap (Optional) User metadata.
   * @param sse (Optional) Server-side encryption.
   * @param srcBucketName Source bucket name.
   * @param srcObjectName (Optional) Source object name.
   * @param srcSse (Optional) SSE-C type server-side encryption of source object.
   * @param copyConditions (Optional) Conditiions to be used in copy operation.
   */
  default void copyObject(
      @NotEmpty String bucketName,
      @NotEmpty String objectName,
      @Nullable Map<String, String> headerMap,
      @Nullable ServerSideEncryption sse,
      @NotEmpty String srcBucketName,
      @Nullable String srcObjectName,
      @Nullable ServerSideEncryption srcSse,
      @Nullable CopyConditions copyConditions) {

    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient.copyObject(
        bucketName,
        objectName,
        headerMap,
        sse,
        srcBucketName,
        srcObjectName,
        srcSse,
        copyConditions));
  }

  /**
   * Creates an object by combining data from different source objects using server-side copy.
   *
   * <pre>Example:{@code
   * List<ComposeSource> sourceObjectList = new ArrayList<ComposeSource>();
   * sourceObjectList.add(new ComposeSource("my-job-bucket", "my-objectname-part-one"));
   * sourceObjectList.add(new ComposeSource("my-job-bucket", "my-objectname-part-two"));
   * sourceObjectList.add(new ComposeSource("my-job-bucket", "my-objectname-part-three"));
   *
   * // Create my-bucketname/my-objectname by combining source object list.
   * minioOperations.composeObject("my-bucketname", "my-objectname", sourceObjectList,
   *     null, null);
   *
   * // Create my-bucketname/my-objectname with user metadata by combining source object
   * // list.
   * minioOperations.composeObject("my-bucketname", "my-objectname", sourceObjectList,
   *     userMetadata, null);
   *
   * // Create my-bucketname/my-objectname with user metadata and server-side encryption
   * // by combining source object list.
   * minioOperations.composeObject("my-bucketname", "my-objectname", sourceObjectList,
   *     userMetadata, sse);
   * }*</pre>
   *
   * @param bucketName Destination Bucket to be created upon compose.
   * @param objectName Destination Object to be created upon compose.
   * @param sources List of Source Objects used to compose Object.
   * @param headerMap User Meta data.
   * @param sse Server Side Encryption.
   */
  default void composeObject(
      @NotEmpty String bucketName,
      @NotEmpty String objectName,
      List<ComposeSource> sources,
      Map<String, String> headerMap,
      ServerSideEncryption sse) {

    execute((MinioClientCallbackWithoutResult) minioClient -> minioClient
        .composeObject(bucketName, objectName, sources, headerMap, sse));
  }
}
