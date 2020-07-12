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

import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.given;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.minio.BucketExistsArgs;
import io.minio.DisableVersioningArgs;
import io.minio.DownloadObjectArgs;
import io.minio.EnableVersioningArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.IsVersioningEnabledArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.UploadObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.EventType;
import io.minio.messages.Item;
import io.minio.messages.NotificationConfiguration;
import io.minio.messages.ObjectLockConfiguration;
import io.minio.messages.QueueConfiguration;
import io.minio.messages.Retention;
import io.minio.messages.RetentionDurationDays;
import io.minio.messages.RetentionMode;
import io.minio.messages.Upload;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.bremersee.data.minio.app.TestConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.util.FileCopyUtils;
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
        // "embedded.minio.enabled=false",
        "embedded.minio.browser=off"
    })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS) // allows us to use @BeforeAll with a non-static method
@Slf4j
public class MinioTemplateTest {

  private final static SimpleDateFormat SDF = new SimpleDateFormat(
      "yyyy-MM-dd-HH-mm-ss", Locale.GERMANY);

  private static final String DEFAULT_BUCKET = "bremersee-"
      + SDF.format(new Date()) + "-"
      + UUID.randomUUID().toString().replace("-", "").toLowerCase();

  @Value("${embedded.minio.host}")
  private String minioHost;

  @Value("${embedded.minio.port}")
  private int minioPort;

  @Value("${embedded.minio.accessKey}")
  private String accessKey;

  @Value("${embedded.minio.secretKey}")
  private String secretKey;

  private MinioTemplate embeddedMinio;

  private MinioTemplate playMinio;

  /**
   * Sets up.
   */
  @BeforeAll
  void setUp() {
    log.info("minioHost = {}, minioPort = {}, accessKey = {}, secretKey = {}",
        minioHost, minioPort, accessKey, secretKey);
    MinioClient minioClient = MinioClient.builder()
        .endpoint(minioHost, minioPort, false)
        .credentials(accessKey, secretKey)
        .build();
    embeddedMinio = new MinioTemplate(minioClient);
    embeddedMinio.setErrorHandler(null);
    embeddedMinio.setErrorHandler(new DefaultMinioErrorHandler());

    MinioClient playMinioClient = MinioClient.builder()
        .endpoint(HttpUrl.get("https://play.min.io"))
        .credentials("Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG")
        .build();
    playMinio = new MinioTemplate(playMinioClient);

    if (!embeddedMinio.bucketExists(BucketExistsArgs.builder().bucket(DEFAULT_BUCKET).build())) {
      embeddedMinio.makeBucket(MakeBucketArgs.builder().bucket(DEFAULT_BUCKET).build());
    }
    if (!playMinio.bucketExists(BucketExistsArgs.builder().bucket(DEFAULT_BUCKET).build())) {
      playMinio.makeBucket(MakeBucketArgs.builder().bucket(DEFAULT_BUCKET).build());
    }
  }

  @AfterAll
  void afterAll() {
    if (!embeddedMinio.bucketExists(BucketExistsArgs.builder().bucket(DEFAULT_BUCKET).build())) {
      embeddedMinio.removeBucket(RemoveBucketArgs.builder().bucket(DEFAULT_BUCKET).build());
    }
    if (!playMinio.bucketExists(BucketExistsArgs.builder().bucket(DEFAULT_BUCKET).build())) {
      playMinio.removeBucket(RemoveBucketArgs.builder().bucket(DEFAULT_BUCKET).build());
    }

  }

  private String newBucketName() {
    return "bremersee-" + UUID.randomUUID().toString().toLowerCase();
  }

  /**
   * Clone minio template.
   */
  @Order(1)
  @Test
  void cloneMinioTemplate() {
    assertNotNull(embeddedMinio.clone());
    assertNotNull(embeddedMinio.clone(new DefaultMinioErrorHandler()));
  }

  /**
   * Make and remove bucket.
   */
  @Order(10)
  @Test
  void makeAndRemoveBucket() {
    String bucketName = newBucketName();
    embeddedMinio.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    assertTrue(embeddedMinio.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()));
    List<Bucket> buckets = embeddedMinio.listBuckets();
    assertNotNull(buckets);
    assertFalse(buckets.isEmpty());
    assertTrue(buckets.stream().anyMatch(bucket -> bucket.name().equals(bucketName)));
    embeddedMinio.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    assertFalse(embeddedMinio.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()));
  }

  /**
   * Make and remove bucket with object lock.
   */
  @Order(12)
  @Test
  void makeAndRemoveBucketWithObjectLock() {
    String bucketName = newBucketName();
    embeddedMinio.makeBucket(MakeBucketArgs.builder()
        .bucket(bucketName)
        .objectLock(true)
        .build());
    assertTrue(embeddedMinio.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()));
    List<Bucket> buckets = embeddedMinio.listBuckets();
    assertNotNull(buckets);
    assertFalse(buckets.isEmpty());
    assertTrue(buckets.stream().anyMatch(bucket -> bucket.name().equals(bucketName)));
    embeddedMinio.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    assertFalse(embeddedMinio.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()));
  }

  @Order(20)
  @Test
  void versioning() {

    final String bucketName = newBucketName();
    playMinio.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    try {
      assertFalse(playMinio
          .isVersioningEnabled(IsVersioningEnabledArgs.builder().bucket(bucketName).build()));
      playMinio.enableVersioning(
          EnableVersioningArgs.builder().bucket(bucketName).build());
      assertTrue(playMinio
          .isVersioningEnabled(IsVersioningEnabledArgs.builder().bucket(bucketName).build()));
      playMinio.disableVersioning(
          DisableVersioningArgs.builder().bucket(bucketName).build());
      assertFalse(playMinio
          .isVersioningEnabled(IsVersioningEnabledArgs.builder().bucket(bucketName).build()));

    } finally {
      playMinio.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    }
  }

  /**
   * Enable versioning and expect not implemented.
   */
  @Order(22)
  @Test
  void enableVersioningAndExpectNotImplemented() {

    final String bucketName = newBucketName();
    embeddedMinio.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    try {
      assertThrows(MinioException.class, () -> embeddedMinio.enableVersioning(
          EnableVersioningArgs.builder().bucket(bucketName).build()));

    } finally {
      embeddedMinio.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    }
  }

  /**
   * Disable versioning and expect not implemented.
   */
  @Order(24)
  @Test
  void disableVersioningAndExpectNotImplemented() {

    final String bucketName = newBucketName();
    embeddedMinio.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    try {
      assertThrows(MinioException.class, () -> embeddedMinio.disableVersioning(
          DisableVersioningArgs.builder().bucket(bucketName).build()));

    } finally {
      embeddedMinio.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    }
  }

  /*
  @Order(30)
  @Test
  void defaultRetention() {
    final String bucketName = newBucketName();
    embeddedMinio.makeBucket(bucketName, null, true);
    try {
      ObjectLockConfiguration config = new ObjectLockConfiguration(
          RetentionMode.COMPLIANCE,
          new RetentionDurationDays(5));
      embeddedMinio.setDefaultRetention(bucketName, config);

      ObjectLockConfiguration readConfig = embeddedMinio.getDefaultRetention(bucketName);
      assertNotNull(readConfig);

    } finally {
      embeddedMinio.removeBucket(bucketName);
    }
  }

  @Order(40)
  @Test
  void objectRetention() {
    final String bucketName = newBucketName().toLowerCase();
    playMinio.makeBucket(bucketName, null, true);

    final String objectName = UUID.randomUUID().toString().toLowerCase() + ".txt";
    final UploadedItem<?> item = new UploadedByteArray(
        "Hello".getBytes(StandardCharsets.UTF_8),
        MediaType.TEXT_PLAIN_VALUE,
        objectName);
    playMinio.putObject(
        bucketName,
        objectName,
        item,
        DeleteMode.ALWAYS);
    try {
      long seconds = 10L;
      ZonedDateTime until = ZonedDateTime.now().plusSeconds(seconds);

      Retention config = new Retention(RetentionMode.COMPLIANCE, until);
      playMinio.setObjectRetention(bucketName, objectName, null, config, true);

      Retention retention = playMinio.getObjectRetention(bucketName, objectName, null);
      assertNotNull(retention);
      assertNotNull(retention.mode());
      assertNotNull(retention.retainUntilDate());
      assertEquals(RetentionMode.COMPLIANCE, retention.mode());
      assertEquals(until.toEpochSecond(), retention.retainUntilDate().toEpochSecond());

    } finally {
      playMinio.removeObject(bucketName, objectName);
      playMinio.listObjects(bucketName).forEach(itemResult -> {
        try {
          System.out.println("===> " + itemResult.get());
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
      //playMinio.removeBucket(bucketName);
    }
  }

  @Test
  void unlock() {
    String bucketName = "bremersee-55a05fa2-46b1-439c-8d7c-f586dc4eedea";
    //playMinio.disableVersioning(bucketName);
    //playMinio.removeBucket(bucketName);

    //playMinio.deleteBucketLifeCycle(bucketName);
    //playMinio.removeBucket(bucketName);
    //playMinio.setDefaultRetention(bucketName, null);
    playMinio.removeBucket(bucketName);
  }

  @Order(50)
  @Test
  void objectLegalHold() {
    final String bucketName = newBucketName();
    embeddedMinio.makeBucket(bucketName);

    final String objectName = UUID.randomUUID().toString() + ".txt";
    final UploadedItem<?> item = new UploadedByteArray(
        "Hello".getBytes(StandardCharsets.UTF_8),
        MediaType.TEXT_PLAIN_VALUE,
        objectName);
    embeddedMinio.putObject(
        bucketName,
        objectName,
        item,
        DeleteMode.ALWAYS);
    try {
      // We need a https connection
      assertThrows(
          MinioException.class,
          () -> embeddedMinio.enableObjectLegalHold(bucketName, objectName, null));
      assertThrows(
          MinioException.class,
          () -> embeddedMinio.disableObjectLegalHold(bucketName, objectName, null));
      assertThrows(
          MinioException.class,
          () -> embeddedMinio.isObjectLegalHoldEnabled(bucketName, objectName, null));

    } finally {
      embeddedMinio.removeObject(bucketName, objectName);
      embeddedMinio.removeBucket(bucketName);
    }
  }

  @Order(60)
  @Test
  void bucketPolicy() {
    final String bucketName = newBucketName();
    embeddedMinio.makeBucket(bucketName);
    assertDoesNotThrow(() -> embeddedMinio.getBucketPolicy(bucketName));
    assertThrows(MinioException.class, () -> embeddedMinio.setBucketPolicy(bucketName, "{}"));
    embeddedMinio.removeBucket(bucketName);
  }

  @Order(70)
  @Test
  void bucketLifeCycle() {
    final String bucketName = newBucketName();
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
    embeddedMinio.makeBucket(bucketName);
    embeddedMinio.setBucketLifeCycle(bucketName, lifecycle);
    String readLifecycle = embeddedMinio.getBucketLifeCycle(bucketName);
    assertTrue(StringUtils.hasText(readLifecycle));
    assertTrue(readLifecycle.contains("30"));
    embeddedMinio.deleteBucketLifeCycle(bucketName);
    embeddedMinio.removeBucket(bucketName);
  }

  @Order(80)
  @Test
  void bucketNotification() {
    final String bucketName = newBucketName();
    embeddedMinio.makeBucket(bucketName);
    try {
      NotificationConfiguration config = embeddedMinio.getBucketNotification(bucketName);
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
          () -> embeddedMinio.setBucketNotification(bucketName, newConfig));

      embeddedMinio.removeAllBucketNotification(bucketName);

      assertThrows(MinioException.class, () -> embeddedMinio.listenBucketNotification(
          bucketName,
          "images",
          ".png",
          new String[]{EventType.OBJECT_CREATED_PUT.name()}));

    } finally {
      embeddedMinio.removeBucket(bucketName);
    }
  }

  @Order(90)
  @Test
  void listIncompleteUploads() {
    final String bucketName = newBucketName();
    embeddedMinio.makeBucket(bucketName);
    try {
      Iterable<Result<Upload>> results = embeddedMinio.listIncompleteUploads(bucketName);
      assertNotNull(results);
      results = embeddedMinio.listIncompleteUploads(bucketName, "images");
      assertNotNull(results);
      results = embeddedMinio.listIncompleteUploads(bucketName, "images", true);
      assertNotNull(results);

    } finally {
      embeddedMinio.removeBucket(bucketName);
    }
  }

  @Order(100)
  @Test
  void removeIncompleteUpload() {
    final String bucketName = newBucketName();
    embeddedMinio.makeBucket(bucketName);
    try {
      embeddedMinio.removeIncompleteUpload(bucketName, "unknown");

    } finally {
      embeddedMinio.removeBucket(bucketName);
    }
  }
  */

  @Order(1010)
  @Test
  void putAndRemoveObject() throws Exception {

    String objectName = UUID.randomUUID().toString() + ".txt";
    byte[] value = "Hello Minio".getBytes(StandardCharsets.UTF_8);
    ObjectWriteResponse response = embeddedMinio.putObject(PutObjectArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .object(objectName)
        .stream(new ByteArrayInputStream(value), value.length, -1)
        .contentType(MediaType.TEXT_PLAIN_VALUE)
        .build());
    assertNotNull(response);

    Iterable<Result<Item>> results = embeddedMinio
        .listObjects(ListObjectsArgs.builder().bucket(DEFAULT_BUCKET).build());
    boolean contains = false;
    for (Result<Item> result : results) {
      Item item = result.get();
      if (objectName.equals(item.objectName())) {
        contains = true;
        break;
      }
    }
    assertTrue(contains);

    embeddedMinio.removeObject(RemoveObjectArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .object(objectName)
        .build());

    results = embeddedMinio
        .listObjects(ListObjectsArgs.builder().bucket(DEFAULT_BUCKET).build());
    contains = false;
    for (Result<Item> result : results) {
      Item item = result.get();
      if (objectName.equals(item.objectName())) {
        contains = true;
        break;
      }
    }
    assertFalse(contains);
  }

  @Order(1020)
  @Test
  void objectUrls() throws Exception {

    String objectName = UUID.randomUUID().toString() + ".txt";
    byte[] value = "Hello Minio".getBytes(StandardCharsets.UTF_8);
    ObjectWriteResponse response = embeddedMinio.putObject(PutObjectArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .object(objectName)
        .stream(new ByteArrayInputStream(value), value.length, -1)
        .contentType(MediaType.TEXT_PLAIN_VALUE)
        .build());
    assertNotNull(response);

    try {
      String url = embeddedMinio.getObjectUrl(DEFAULT_BUCKET, objectName);
      assertNotNull(url);

      url = embeddedMinio.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .method(Method.GET)
          .expiry(3600 * 24)
          .build());
      assertNotNull(url);
      try (InputStream in = new URL(url).openStream()) {
        byte[] readBytes = FileCopyUtils.copyToByteArray(in);
        assertArrayEquals(value, readBytes);
      }

    } finally {
      embeddedMinio.removeObject(RemoveObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .build());
    }
  }

  @Order(1030)
  @Test
  void downloadObject() throws Exception {

    String objectName = UUID.randomUUID().toString() + ".txt";
    byte[] value = "Hello Minio".getBytes(StandardCharsets.UTF_8);
    ObjectWriteResponse response = embeddedMinio.putObject(PutObjectArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .object(objectName)
        .stream(new ByteArrayInputStream(value), value.length, -1)
        .contentType(MediaType.TEXT_PLAIN_VALUE)
        .build());
    assertNotNull(response);

    File file = null;
    try {
      file = File.createTempFile(
          "junit", ".tmp", new File(System.getProperty("java.io.tmpdir")));
      embeddedMinio.downloadObject(DownloadObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .filename(file.getAbsolutePath())
          .build());
      byte[] fileBytes = FileCopyUtils.copyToByteArray(file);
      assertArrayEquals(value, fileBytes);

    } finally {
      if (file != null) {
        Files.delete(file.toPath());
      }
      embeddedMinio.removeObject(RemoveObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .build());
    }
  }

  @Order(1040)
  @Test
  void uploadObject() throws Exception{
    String objectName = UUID.randomUUID().toString() + ".txt";
    byte[] value = "Hello Minio".getBytes(StandardCharsets.UTF_8);
    try {
      File file = File.createTempFile(
          "junit", ".tmp", new File(System.getProperty("java.io.tmpdir")));
      FileCopyUtils.copy(
          value,
          Files.newOutputStream(
              file.toPath(),
              StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING));
      embeddedMinio.uploadObject(
          UploadObjectArgs.builder()
              .bucket(DEFAULT_BUCKET)
              .object(objectName)
              .filename(file.getAbsolutePath())
              .contentType(MediaType.TEXT_PLAIN_VALUE)
              .build(),
          DeleteMode.ALWAYS);

      assertFalse(file.exists());
      String url = embeddedMinio.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .method(Method.GET)
          .expiry(3600 * 24)
          .build());
      assertNotNull(url);
      try (InputStream in = new URL(url).openStream()) {
        byte[] readBytes = FileCopyUtils.copyToByteArray(in);
        assertArrayEquals(value, readBytes);
      }


    } finally {
      embeddedMinio.removeObject(RemoveObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .build());
    }
  }


}
