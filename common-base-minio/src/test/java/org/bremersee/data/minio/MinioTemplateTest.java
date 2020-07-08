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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.messages.Bucket;
import io.minio.messages.EventType;
import io.minio.messages.NotificationConfiguration;
import io.minio.messages.ObjectLockConfiguration;
import io.minio.messages.QueueConfiguration;
import io.minio.messages.Retention;
import io.minio.messages.RetentionDurationDays;
import io.minio.messages.RetentionMode;
import io.minio.messages.Upload;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.data.minio.app.TestConfiguration;
import org.bremersee.web.UploadedByteArray;
import org.bremersee.web.UploadedItem;
import org.bremersee.web.UploadedItem.DeleteMode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

/**
 * The minio template test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = {TestConfiguration.class},
    webEnvironment = WebEnvironment.NONE,
    properties = {
        "embedded.minio.browser=off"
    })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS) // allows us to use @BeforeAll with a non-static method
@Slf4j
public class MinioTemplateTest {

  @Value("${embedded.minio.host}")
  private String minioHost;

  @Value("${embedded.minio.port}")
  private int minioPort;

  @Value("${embedded.minio.accessKey}")
  private String accessKey;

  @Value("${embedded.minio.secretKey}")
  private String secretKey;

  private MinioTemplate minioTemplate;

  /**
   * Sets up.
   *
   * @throws InvalidPortException the invalid port exception
   * @throws InvalidEndpointException the invalid endpoint exception
   */
  @BeforeAll
  void setUp() throws InvalidPortException, InvalidEndpointException {
    log.info("minioHost = {}, minioPort = {}, accessKey = {}, secretKey = {}",
        minioHost, minioPort, accessKey, secretKey);
    MinioClient minioClient = new MinioClient(minioHost, minioPort, accessKey, secretKey, false);
    minioTemplate = new MinioTemplate(minioClient);
    minioTemplate.setErrorHandler(null);
    minioTemplate.setErrorHandler(new DefaultMinioErrorHandler());
  }

  /**
   * Clone minio template.
   */
  @Order(1)
  @Test
  void cloneMinioTemplate() {
    assertNotNull(minioTemplate.clone());
    assertNotNull(minioTemplate.clone(new DefaultMinioErrorHandler()));
  }

  /**
   * Make and remove bucket.
   */
  @Order(10)
  @Test
  void makeAndRemoveBucket() {
    String bucketName = UUID.randomUUID().toString();
    minioTemplate.makeBucket(bucketName);
    assertTrue(minioTemplate.bucketExists(bucketName));
    List<Bucket> buckets = minioTemplate.listBuckets();
    assertNotNull(buckets);
    assertFalse(buckets.isEmpty());
    assertTrue(buckets.stream().anyMatch(bucket -> bucket.name().equals(bucketName)));
    minioTemplate.removeBucket(bucketName);
    assertFalse(minioTemplate.bucketExists(bucketName));
  }

  /**
   * Make and remove bucket with object lock.
   */
  @Order(12)
  @Test
  void makeAndRemoveBucketWithObjectLock() {
    String bucketName = UUID.randomUUID().toString();
    minioTemplate.makeBucket(bucketName, null, true);
    assertTrue(minioTemplate.bucketExists(bucketName));
    List<Bucket> buckets = minioTemplate.listBuckets();
    assertNotNull(buckets);
    assertFalse(buckets.isEmpty());
    assertTrue(buckets.stream().anyMatch(bucket -> bucket.name().equals(bucketName)));
    minioTemplate.removeBucket(bucketName);
    assertFalse(minioTemplate.bucketExists(bucketName));
  }

  /**
   * Enable versioning and expect not implemented.
   */
  @Order(20)
  @Test
  void enableVersioningAndExpectNotImplemented() {

    final String bucketName = UUID.randomUUID().toString();
    minioTemplate.makeBucket(bucketName, null);
    try {
      assertThrows(MinioException.class, () -> minioTemplate.enableVersioning(bucketName));

    } finally {
      minioTemplate.removeBucket(bucketName);
    }
  }

  /**
   * Disable versioning and expect not implemented.
   */
  @Order(22)
  @Test
  void disableVersioningAndExpectNotImplemented() {

    final String bucketName = UUID.randomUUID().toString();
    minioTemplate.makeBucket(bucketName, null);
    try {
      assertThrows(MinioException.class, () -> minioTemplate.disableVersioning(bucketName));

    } finally {
      minioTemplate.removeBucket(bucketName);
    }
  }

  /**
   * Default retention.
   */
  @Order(30)
  @Test
  void defaultRetention() {
    final String bucketName = UUID.randomUUID().toString();
    minioTemplate.makeBucket(bucketName, null, true);
    try {
      ObjectLockConfiguration config = new ObjectLockConfiguration(
          RetentionMode.COMPLIANCE,
          new RetentionDurationDays(5));
      minioTemplate.setDefaultRetention(bucketName, config);

      ObjectLockConfiguration readConfig = minioTemplate.getDefaultRetention(bucketName);
      assertNotNull(readConfig);

    } finally {
      minioTemplate.removeBucket(bucketName);
    }
  }

  @Order(40)
  @Test
  void objectRetention() {
    final String bucketName = UUID.randomUUID().toString();
    minioTemplate.makeBucket(bucketName);

    final String objectName = UUID.randomUUID().toString() + ".txt";
    final UploadedItem<?> item = new UploadedByteArray(
        "Hello".getBytes(StandardCharsets.UTF_8),
        MediaType.TEXT_PLAIN_VALUE,
        objectName);
    minioTemplate.putObject(
        bucketName,
        objectName,
        item,
        DeleteMode.ALWAYS);
    try {
      // We need a https connection
      Retention config = new Retention(RetentionMode.COMPLIANCE, ZonedDateTime.now().plusYears(1));
      assertThrows(
          MinioException.class,
          () -> minioTemplate.setObjectRetention(bucketName, objectName, null, config, true));

      Retention retention = minioTemplate.getObjectRetention(bucketName, objectName, null);
      assertNotNull(retention);

    } finally {
      minioTemplate.removeObject(bucketName, objectName);
      minioTemplate.removeBucket(bucketName);
    }
  }

  @Order(50)
  @Test
  void objectLegalHold() {
    final String bucketName = UUID.randomUUID().toString();
    minioTemplate.makeBucket(bucketName);

    final String objectName = UUID.randomUUID().toString() + ".txt";
    final UploadedItem<?> item = new UploadedByteArray(
        "Hello".getBytes(StandardCharsets.UTF_8),
        MediaType.TEXT_PLAIN_VALUE,
        objectName);
    minioTemplate.putObject(
        bucketName,
        objectName,
        item,
        DeleteMode.ALWAYS);
    try {
      // We need a https connection
      assertThrows(
          MinioException.class,
          () -> minioTemplate.enableObjectLegalHold(bucketName, objectName, null));
      assertThrows(
          MinioException.class,
          () -> minioTemplate.disableObjectLegalHold(bucketName, objectName, null));
      assertThrows(
          MinioException.class,
          () -> minioTemplate.isObjectLegalHoldEnabled(bucketName, objectName, null));

    } finally {
      minioTemplate.removeObject(bucketName, objectName);
      minioTemplate.removeBucket(bucketName);
    }
  }

  /**
   * Bucket policy.
   */
  @Order(60)
  @Test
  void bucketPolicy() {
    final String bucketName = UUID.randomUUID().toString();
    minioTemplate.makeBucket(bucketName);
    assertDoesNotThrow(() -> minioTemplate.getBucketPolicy(bucketName));
    assertThrows(MinioException.class, () -> minioTemplate.setBucketPolicy(bucketName, "{}"));
    minioTemplate.removeBucket(bucketName);
  }

  /**
   * Bucket life cycle.
   */
  @Order(70)
  @Test
  void bucketLifeCycle() {
    final String bucketName = UUID.randomUUID().toString();
    final String lifecycle = ""
        + "<LifecycleConfiguration>"
        + "<Rule>"
        + "<ID>expire-bucket</ID>"
        + "<Prefix></Prefix>"
        + "<Status>Enabled</Status>"
        + "<Expiration>"
        + "<Days>30</Days>"
        + "</Expiration>"
        + "</Rule>"
        + "</LifecycleConfiguration>";
    minioTemplate.makeBucket(bucketName);
    minioTemplate.setBucketLifeCycle(bucketName, lifecycle);
    String readLifecycle = minioTemplate.getBucketLifeCycle(bucketName);
    assertTrue(StringUtils.hasText(readLifecycle));
    assertTrue(readLifecycle.contains("30"));
    minioTemplate.deleteBucketLifeCycle(bucketName);
    minioTemplate.removeBucket(bucketName);
  }

  @Order(80)
  @Test
  void bucketNotification() {
    final String bucketName = UUID.randomUUID().toString();
    minioTemplate.makeBucket(bucketName);
    try {
      NotificationConfiguration config = minioTemplate.getBucketNotification(bucketName);
      assertNotNull(config);

      List<EventType> eventList = new LinkedList<>();
      eventList.add(EventType.OBJECT_CREATED_PUT);
      eventList.add(EventType.OBJECT_CREATED_COPY);

      QueueConfiguration queueConfiguration = new QueueConfiguration();
      queueConfiguration.setQueue("arn:minio:sqs::1:webhook");
      queueConfiguration.setEvents(eventList);
      queueConfiguration.setPrefixRule("images");
      queueConfiguration.setSuffixRule("pg");

      List<QueueConfiguration> queueConfigurationList = new LinkedList<>();
      queueConfigurationList.add(queueConfiguration);

      NotificationConfiguration newConfig = new NotificationConfiguration();
      newConfig.setQueueConfigurationList(queueConfigurationList);

      assertThrows(
          MinioException.class,
          () -> minioTemplate.setBucketNotification(bucketName, newConfig));

      minioTemplate.removeAllBucketNotification(bucketName);

      assertThrows(MinioException.class, () -> minioTemplate.listenBucketNotification(
          bucketName,
          "images",
          ".png",
          new String[]{EventType.OBJECT_CREATED_PUT.name()}));

    } finally {
      minioTemplate.removeBucket(bucketName);
    }
  }

  @Order(90)
  @Test
  void listIncompleteUploads() {
    final String bucketName = UUID.randomUUID().toString();
    minioTemplate.makeBucket(bucketName);
    try {
      Iterable<Result<Upload>> results = minioTemplate.listIncompleteUploads(bucketName);
      assertNotNull(results);
      results = minioTemplate.listIncompleteUploads(bucketName, "images");
      assertNotNull(results);
      results = minioTemplate.listIncompleteUploads(bucketName, "images", true);
      assertNotNull(results);

    } finally {
      minioTemplate.removeBucket(bucketName);
    }
  }

  @Order(100)
  @Test
  void removeIncompleteUpload() {
    final String bucketName = UUID.randomUUID().toString();
    minioTemplate.makeBucket(bucketName);
    try {
      minioTemplate.removeIncompleteUpload(bucketName, "unknown");

    } finally {
      minioTemplate.removeBucket(bucketName);
    }
  }

}
