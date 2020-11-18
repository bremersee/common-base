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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.minio.BucketExistsArgs;
import io.minio.CloseableIterator;
import io.minio.ComposeObjectArgs;
import io.minio.ComposeSource;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.DeleteBucketEncryptionArgs;
import io.minio.DeleteBucketNotificationArgs;
import io.minio.DeleteBucketPolicyArgs;
import io.minio.DeleteBucketTagsArgs;
import io.minio.DeleteObjectTagsArgs;
import io.minio.DisableObjectLegalHoldArgs;
import io.minio.DownloadObjectArgs;
import io.minio.EnableObjectLegalHoldArgs;
import io.minio.GetBucketEncryptionArgs;
import io.minio.GetBucketNotificationArgs;
import io.minio.GetBucketPolicyArgs;
import io.minio.GetBucketTagsArgs;
import io.minio.GetObjectRetentionArgs;
import io.minio.GetObjectTagsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.IsObjectLegalHoldEnabledArgs;
import io.minio.ListObjectsArgs;
import io.minio.ListenBucketNotificationArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.SelectObjectContentArgs;
import io.minio.SelectResponseStream;
import io.minio.SetBucketEncryptionArgs;
import io.minio.SetBucketNotificationArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.SetBucketTagsArgs;
import io.minio.SetObjectRetentionArgs;
import io.minio.SetObjectTagsArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.UploadObjectArgs;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.InputSerialization;
import io.minio.messages.Item;
import io.minio.messages.NotificationConfiguration;
import io.minio.messages.NotificationRecords;
import io.minio.messages.OutputSerialization;
import io.minio.messages.Retention;
import io.minio.messages.RetentionMode;
import io.minio.messages.SseAlgorithm;
import io.minio.messages.SseConfiguration;
import io.minio.messages.SseConfigurationRule;
import io.minio.messages.Tags;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import org.bremersee.data.minio.app.TestConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;

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

  private static final SimpleDateFormat SDF = new SimpleDateFormat(
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

  private MinioClient mockClient;

  private MinioTemplate mockMinio;

  private boolean playMinioEnabled = true;

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
    if (!embeddedMinio.bucketExists(BucketExistsArgs.builder().bucket(DEFAULT_BUCKET).build())) {
      embeddedMinio.makeBucket(MakeBucketArgs.builder().bucket(DEFAULT_BUCKET).build());
    }

    try {
      MinioClient playMinioClient = MinioClient.builder()
          .endpoint(HttpUrl.get("https://play.min.io"))
          .credentials("Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG")
          .build();
      playMinio = new MinioTemplate(playMinioClient);
      if (playMinio.bucketExists(BucketExistsArgs.builder().bucket(DEFAULT_BUCKET).build())) {
        playMinio.removeBucket(RemoveBucketArgs.builder().bucket(DEFAULT_BUCKET).build());
      }
      playMinio.makeBucket(MakeBucketArgs.builder().bucket(DEFAULT_BUCKET).build());
      playMinioEnabled = true;

    } catch (Exception e) {
      playMinioEnabled = false;
      e.printStackTrace();
    }
  }

  /**
   * After all.
   */
  @AfterAll
  void afterAll() {
    embeddedMinio.removeBucket(RemoveBucketArgs.builder().bucket(DEFAULT_BUCKET).build());
    if (playMinioEnabled
        && playMinio.bucketExists(BucketExistsArgs.builder().bucket(DEFAULT_BUCKET).build())) {
      playMinio.removeBucket(RemoveBucketArgs.builder().bucket(DEFAULT_BUCKET).build());
    }
  }

  /**
   * Before each.
   */
  @BeforeEach
  void beforeEach() {
    mockClient = Mockito.mock(MinioClient.class);
    mockMinio = new MinioTemplate(mockClient);
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

  /**
   * Versioning.
   *
   * @throws Exception the exception
   */
  @Order(20)
  @Test
  void versioning() throws Exception {

//    final String bucketName = newBucketName();
//    MinioTemplate minio;
//    if (playMinioEnabled) {
//      minio = playMinio;
//    } else {
//      minio = mockMinio;
//      when(mockClient.isVersioningEnabled(any(IsVersioningEnabledArgs.class)))
//          .thenReturn(true);
//    }
//
//    minio.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
//    try {
//      if (playMinioEnabled) {
//        assertFalse(minio
//            .isVersioningEnabled(IsVersioningEnabledArgs.builder().bucket(bucketName).build()));
//      }
//      minio.enableVersioning(
//          EnableVersioningArgs.builder().bucket(bucketName).build());
//      assertTrue(minio
//          .isVersioningEnabled(IsVersioningEnabledArgs.builder().bucket(bucketName).build()));
//      minio.disableVersioning(
//          DisableVersioningArgs.builder().bucket(bucketName).build());
//      if (playMinioEnabled) {
//        assertFalse(minio
//            .isVersioningEnabled(IsVersioningEnabledArgs.builder().bucket(bucketName).build()));
//      }
//
//    } finally {
//      minio.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
//    }
  }

  /**
   * Enable versioning and expect not implemented.
   */
  @Order(22)
  @Test
  void enableVersioningAndExpectNotImplemented() {

//    final String bucketName = newBucketName();
//    embeddedMinio.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
//    try {
//      assertThrows(MinioException.class, () -> embeddedMinio.enableVersioning(
//          EnableVersioningArgs.builder().bucket(bucketName).build()));
//
//    } finally {
//      embeddedMinio.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
//    }
  }

  /**
   * Disable versioning and expect not implemented.
   */
  @Order(24)
  @Test
  void disableVersioningAndExpectNotImplemented() {

    final String bucketName = newBucketName();
//    embeddedMinio.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
//    try {
//      assertThrows(MinioException.class, () -> embeddedMinio.disableVersioning(
//          DisableVersioningArgs.builder().bucket(bucketName).build()));
//
//    } finally {
//      embeddedMinio.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
//    }
  }

  /**
   * Default retention.
   *
   * @throws Exception the exception
   */
  @Order(30)
  @Test
  void defaultRetention() throws Exception {
    final String bucket = newBucketName();
//    MinioTemplate minio;
//    if (playMinioEnabled) {
//      minio = playMinio;
//    } else {
//      minio = mockMinio;
//      when(mockClient.getDefaultRetention(any(GetDefaultRetentionArgs.class)))
//          .thenReturn(new ObjectLockConfiguration(
//              RetentionMode.COMPLIANCE,
//              new RetentionDurationDays(1)));
//    }
//
//    minio.makeBucket(MakeBucketArgs.builder()
//        .bucket(bucket)
//        .objectLock(true)
//        .build());
//
//    try {
//      minio.setDefaultRetention(SetDefaultRetentionArgs.builder()
//          .bucket(bucket)
//          .config(new ObjectLockConfiguration(
//              RetentionMode.COMPLIANCE,
//              new RetentionDurationDays(1)))
//          .build());
//
//      ObjectLockConfiguration defaultConfig = minio.getDefaultRetention(GetDefaultRetentionArgs
//          .builder()
//          .bucket(bucket)
//          .build());
//      assertNotNull(defaultConfig);
//      assertEquals(RetentionMode.COMPLIANCE, defaultConfig.mode());
//      assertEquals(new RetentionDurationDays(1).duration(), defaultConfig.duration().duration());
//
//      minio.deleteDefaultRetention(DeleteDefaultRetentionArgs.builder()
//          .bucket(bucket)
//          .build());
//
//    } finally {
//      minio.removeBucket(RemoveBucketArgs.builder()
//          .bucket(bucket)
//          .build());
//    }
  }

  /**
   * Bucket encryption.
   *
   * @throws Exception the exception
   */
  @Order(40)
  @Test
  void bucketEncryption() throws Exception {
    final String bucket = newBucketName();
    MinioTemplate minio;
    if (playMinioEnabled) {
      minio = playMinio;
    } else {
      minio = mockMinio;
      when(mockClient.getBucketEncryption(any(GetBucketEncryptionArgs.class)))
          .thenReturn(new SseConfiguration(new SseConfigurationRule(SseAlgorithm.AES256, null)));
    }

    minio.makeBucket(MakeBucketArgs.builder()
        .bucket(bucket)
        .objectLock(true)
        .build());

    try {
      minio.setBucketEncryption(SetBucketEncryptionArgs.builder()
          .bucket(bucket)
          .config(new SseConfiguration(new SseConfigurationRule(SseAlgorithm.AES256, null)))
          .build());

      SseConfiguration config = minio.getBucketEncryption(GetBucketEncryptionArgs.builder()
          .bucket(bucket)
          .build());
      assertNotNull(config);

      minio.deleteBucketEncryption(DeleteBucketEncryptionArgs.builder()
          .bucket(bucket)
          .build());

    } finally {
      minio.removeBucket(RemoveBucketArgs.builder()
          .bucket(bucket)
          .build());
    }
  }

  /**
   * Bucket tags.
   *
   * @throws Exception the exception
   */
  @Order(50)
  @Test
  void bucketTags() throws Exception {
    Map<String, String> tagMap = Collections.singletonMap("my-key", "my-value");
    MinioTemplate minio;
    if (playMinioEnabled) {
      minio = playMinio;
    } else {
      minio = mockMinio;
      when(mockClient.getBucketTags(any(GetBucketTagsArgs.class)))
          .thenReturn(Tags.newBucketTags(tagMap));
    }
    minio.setBucketTags(SetBucketTagsArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .tags(tagMap)
        .build());
    Tags tags = minio.getBucketTags(GetBucketTagsArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .build());
    assertNotNull(tags);
    assertTrue(tags.get().containsKey("my-key"));
    assertTrue(tags.get().containsValue("my-value"));

    minio.deleteBucketTags(DeleteBucketTagsArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .build());
  }

  /**
   * Bucket policy.
   *
   * @throws Exception the exception
   */
  @Order(60)
  @Test
  void bucketPolicy() throws Exception {
    String policy = "{}";
    when(mockClient.getBucketPolicy(any(GetBucketPolicyArgs.class))).thenReturn(policy);
    mockMinio.setBucketPolicy(SetBucketPolicyArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .config(policy)
        .build());
    String readPolicy = mockMinio.getBucketPolicy(GetBucketPolicyArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .build());
    assertEquals(policy, readPolicy);
    mockMinio.deleteBucketPolicy(DeleteBucketPolicyArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .build());
  }

  /**
   * Bucket notification.
   *
   * @throws Exception the exception
   */
  @Order(70)
  @Test
  void bucketNotification() throws Exception {
    NotificationConfiguration config = new NotificationConfiguration();
    when(mockClient.getBucketNotification(any(GetBucketNotificationArgs.class)))
        .thenReturn(config);
    mockMinio.setBucketNotification(SetBucketNotificationArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .config(config)
        .build());
    NotificationConfiguration readConfig = mockMinio.getBucketNotification(
        GetBucketNotificationArgs.builder()
            .bucket(DEFAULT_BUCKET)
            .build());
    assertNotNull(readConfig);

    mockMinio.deleteBucketNotification(DeleteBucketNotificationArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .build());

    when(mockClient.listenBucketNotification(any(ListenBucketNotificationArgs.class)))
        .thenReturn(new CloseableIterator<>() {

          private boolean hasNext = true;

          @Override
          public void close() {
            // nothing to do
          }

          @Override
          public boolean hasNext() {
            return hasNext;
          }

          @Override
          public Result<NotificationRecords> next() {
            if (hasNext) {
              NotificationRecords records = new NotificationRecords();
              Result<NotificationRecords> result = new Result<>(records);
              hasNext = false;
              return result;
            }
            return null;
          }
        });

    try (CloseableIterator<Result<NotificationRecords>> in = mockMinio
        .listenBucketNotification(ListenBucketNotificationArgs.builder()
            .bucket(DEFAULT_BUCKET)
            .events(new String[]{"s3:ObjectCreated:*", "s3:ObjectAccessed:*"})
            .prefix("")
            .suffix("")
            .build())) {
      assertNotNull(in);
      assertTrue(in.hasNext());
    }

  }

  /**
   * Bucket life cycle.
   *
   * @throws Exception the exception
   */
  @Order(80)
  @Test
  void bucketLifeCycle() throws Exception {
    String lifeCycle = ""
        + "<LifecycleConfiguration>\n"
        + "  <Rule>\n"
        + "    <ID>expire-bucket</ID>\n"
        + "    <Prefix></Prefix>\n"
        + "    <Status>Enabled</Status>\n"
        + "    <Expiration>\n"
        + "      <Days>365</Days>\n"
        + "    </Expiration>\n"
        + "  </Rule>\n"
        + "</LifecycleConfiguration>";
//    when(mockClient.getBucketLifeCycle(any(GetBucketLifeCycleArgs.class))).thenReturn(lifeCycle);
//    mockMinio.setBucketLifeCycle(SetBucketLifeCycleArgs.builder()
//        .bucket(DEFAULT_BUCKET)
//        .config(lifeCycle)
//        .build());
//    String readLifeCycle = mockMinio.getBucketLifeCycle(GetBucketLifeCycleArgs.builder()
//        .bucket(DEFAULT_BUCKET)
//        .build());
//    assertEquals(lifeCycle, readLifeCycle);
  }


  /**
   * Put and remove object.
   *
   * @throws Exception the exception
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

  /**
   * Object urls.
   *
   * @throws Exception the exception
   */
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

  /**
   * Download object.
   *
   * @throws Exception the exception
   */
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

  /**
   * Upload object.
   *
   * @throws Exception the exception
   */
  @Order(1040)
  @Test
  void uploadObject() throws Exception {

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
      ObjectWriteResponse response = embeddedMinio.uploadObject(
          UploadObjectArgs.builder()
              .bucket(DEFAULT_BUCKET)
              .object(objectName)
              .filename(file.getAbsolutePath())
              .contentType(MediaType.TEXT_PLAIN_VALUE)
              .build(),
          DeleteMode.ALWAYS);
      assertNotNull(response);

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

  /**
   * Copy object.
   *
   * @throws Exception the exception
   */
  @Order(1050)
  @Test
  void copyObject() throws Exception {

    String objectName = UUID.randomUUID().toString() + ".txt";
    byte[] value = "Hello Minio".getBytes(StandardCharsets.UTF_8);
    ObjectWriteResponse response = embeddedMinio.putObject(PutObjectArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .object(objectName)
        .stream(new ByteArrayInputStream(value), value.length, -1)
        .contentType(MediaType.TEXT_PLAIN_VALUE)
        .build());
    assertNotNull(response);

    boolean destObjectExists = false;
    String destBucket = newBucketName();
    String destObjectName = UUID.randomUUID().toString() + ".txt";
    embeddedMinio.makeBucket(MakeBucketArgs.builder().bucket(destBucket).build());
    try {
      ObjectWriteResponse copyResponse = embeddedMinio.copyObject(CopyObjectArgs.builder()
          .bucket(destBucket)
          .object(destObjectName)
          .source(CopySource.builder()
              .bucket(DEFAULT_BUCKET)
              .object(objectName)
              .build())
          .build());
      assertNotNull(copyResponse);
      System.out.println(">> " + copyResponse.object());

      StatObjectResponse objectStat = embeddedMinio.statObject(StatObjectArgs.builder()
          .bucket(destBucket)
          .object(destObjectName)
          .build());
      assertNotNull(objectStat);
      destObjectExists = true;
      System.out.println(">> " + objectStat.size());
      assertEquals(value.length, (int) objectStat.size());

      Iterable<Result<DeleteError>> errors = embeddedMinio.removeObjects(RemoveObjectsArgs
          .builder()
          .bucket(destBucket)
          .objects(Collections.singletonList(new DeleteObject(destObjectName)))
          .build());
      boolean hasError = false;
      for (Result<DeleteError> error : errors) {
        System.out.println(">>>> " + error.get().objectName());
        hasError = true;
      }
      assertFalse(hasError);

      assertThrows(MinioException.class, () -> embeddedMinio.statObject(StatObjectArgs.builder()
          .bucket(destBucket)
          .object(destObjectName)
          .build()));
      destObjectExists = false;

    } finally {
      embeddedMinio.removeObject(RemoveObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .build());
      if (destObjectExists) {
        embeddedMinio.removeObject(RemoveObjectArgs.builder()
            .bucket(destBucket)
            .object(destObjectName)
            .build());
      }
      embeddedMinio.removeBucket(RemoveBucketArgs.builder().bucket(destBucket).build());
    }
  }

  /**
   * Compose object.
   *
   * @throws Exception the exception
   */
  @Order(1060)
  @Test
  void composeObject() throws Exception {

    Random random = new Random();
    final int size = 5242882;
    String objectName0 = UUID.randomUUID().toString() + ".dat";
    byte[] value0 = new byte[size];
    random.nextBytes(value0);
    ObjectWriteResponse response = embeddedMinio.putObject(PutObjectArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .object(objectName0)
        .stream(new ByteArrayInputStream(value0), value0.length, -1)
        .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
        .build());
    assertNotNull(response);

    String objectName1 = UUID.randomUUID().toString() + ".dat";
    byte[] value1 = new byte[size];
    random.nextBytes(value1);
    response = embeddedMinio.putObject(PutObjectArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .object(objectName1)
        .stream(new ByteArrayInputStream(value1), value1.length, -1)
        .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
        .build());
    assertNotNull(response);

    boolean destObjectExists = false;
    String destBucket = newBucketName();
    String destObjectName = UUID.randomUUID().toString() + ".txt";
    embeddedMinio.makeBucket(MakeBucketArgs.builder().bucket(destBucket).build());
    try {
      response = embeddedMinio.composeObject(ComposeObjectArgs.builder()
          .bucket(destBucket)
          .object(destObjectName)
          .sources(List.of(
              ComposeSource.builder()
                  .bucket(DEFAULT_BUCKET)
                  .object(objectName0)
                  .build(),
              ComposeSource.builder()
                  .bucket(DEFAULT_BUCKET)
                  .object(objectName1)
                  .build()))
          .build());
      assertNotNull(response);
      destObjectExists = true;

      String url = embeddedMinio.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
          .bucket(destBucket)
          .object(destObjectName)
          .method(Method.GET)
          .expiry(3600 * 24)
          .build());
      assertNotNull(url);
      try (InputStream in = new URL(url).openStream()) {
        byte[] readBytes = FileCopyUtils.copyToByteArray(in);
        assertEquals(2 * size, readBytes.length);
      }

    } finally {
      embeddedMinio.removeObject(RemoveObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName0)
          .build());
      embeddedMinio.removeObject(RemoveObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName1)
          .build());
      if (destObjectExists) {
        embeddedMinio.removeObject(RemoveObjectArgs.builder()
            .bucket(destBucket)
            .object(destObjectName)
            .build());
      }
      embeddedMinio.removeBucket(RemoveBucketArgs.builder().bucket(destBucket).build());
    }
  }

  /**
   * Tags.
   */
  @Order(1070)
  @Test
  void tags() {

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
      embeddedMinio.setObjectTags(SetObjectTagsArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .tags(Collections.singletonMap("my-key", "my-value"))
          .build());

      Tags tags = embeddedMinio.getObjectTags(GetObjectTagsArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .build());
      assertNotNull(tags);
      assertEquals("my-value", tags.get().get("my-key"));

      embeddedMinio.deleteObjectTags(DeleteObjectTagsArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .build());

      tags = embeddedMinio.getObjectTags(GetObjectTagsArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .build());
      assertNotNull(tags);
      assertTrue(tags.get().isEmpty());

    } finally {
      embeddedMinio.removeObject(RemoveObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .build());
    }
  }

  /**
   * Object legal hold.
   *
   * @throws Exception the exception
   */
  @Order(1080)
  @Test
  void objectLegalHold() throws Exception {
    String bucket = newBucketName();
    String objectName = UUID.randomUUID().toString() + ".txt";

    MinioTemplate minio;
    if (playMinioEnabled) {
      minio = playMinio;
    } else {
      minio = mockMinio;
      when(mockClient.putObject(any(PutObjectArgs.class)))
          .thenReturn(new ObjectWriteResponse(
              Headers.of(Collections.emptyMap()),
              bucket,
              null,
              objectName,
              null,
              null
          ));
      when(mockClient.isObjectLegalHoldEnabled(any(IsObjectLegalHoldEnabledArgs.class)))
          .thenReturn(true);
    }

    minio.makeBucket(MakeBucketArgs.builder()
        .bucket(bucket)
        .objectLock(true)
        .build());
    byte[] value = "Hello Minio".getBytes(StandardCharsets.UTF_8);
    ObjectWriteResponse response = minio.putObject(PutObjectArgs.builder()
        .bucket(bucket)
        .object(objectName)
        .stream(new ByteArrayInputStream(value), value.length, -1)
        .contentType(MediaType.TEXT_PLAIN_VALUE)
        .build());
    assertNotNull(response);

    try {
      minio.enableObjectLegalHold(EnableObjectLegalHoldArgs.builder()
          .bucket(bucket)
          .object(objectName)
          .build());

      boolean enabled = minio.isObjectLegalHoldEnabled(IsObjectLegalHoldEnabledArgs.builder()
          .bucket(bucket)
          .object(objectName)
          .build());
      assertTrue(enabled);

      minio.disableObjectLegalHold(DisableObjectLegalHoldArgs.builder()
          .bucket(bucket)
          .object(objectName)
          .build());

    } finally {
      minio.removeObject(RemoveObjectArgs.builder()
          .bucket(bucket)
          .object(objectName)
          .versionId(response.versionId())
          .build());
      minio.removeBucket(RemoveBucketArgs.builder()
          .bucket(bucket)
          .build());
    }
  }

  /**
   * Object retention.
   *
   * @throws Exception the exception
   */
  @Order(1090)
  @Test
  void objectRetention() throws Exception {
    String bucket = newBucketName();
    String objectName = UUID.randomUUID().toString() + ".txt";
    ZonedDateTime until = ZonedDateTime.now().plus(10L, ChronoUnit.SECONDS);

    MinioTemplate minio;
    if (playMinioEnabled) {
      minio = playMinio;
    } else {
      minio = mockMinio;
      when(mockClient.putObject(any(PutObjectArgs.class)))
          .thenReturn(new ObjectWriteResponse(
              Headers.of(Collections.emptyMap()),
              bucket,
              null,
              objectName,
              null,
              "1234"
          ));
      when(mockClient.getObjectRetention(any(GetObjectRetentionArgs.class)))
          .thenReturn(new Retention(RetentionMode.COMPLIANCE, until));
    }
    minio.makeBucket(MakeBucketArgs.builder()
        .bucket(bucket)
        .objectLock(true)
        .build());
    byte[] value = "Hello Minio".getBytes(StandardCharsets.UTF_8);
    ObjectWriteResponse response = minio.putObject(PutObjectArgs.builder()
        .bucket(bucket)
        .object(objectName)
        .stream(new ByteArrayInputStream(value), value.length, -1)
        .contentType(MediaType.TEXT_PLAIN_VALUE)
        .build());
    assertNotNull(response);

    try {
      minio.setObjectRetention(SetObjectRetentionArgs.builder()
          .bucket(bucket)
          .object(objectName)
          .bypassGovernanceMode(true)
          .config(new Retention(RetentionMode.COMPLIANCE, until))
          .build());

      Retention retention = minio.getObjectRetention(GetObjectRetentionArgs.builder()
          .bucket(bucket)
          .object(objectName)
          .versionId(response.versionId())
          .build());
      assertNotNull(retention);
      assertEquals(RetentionMode.COMPLIANCE, retention.mode());
      assertEquals(until.toEpochSecond(), retention.retainUntilDate().toEpochSecond());

      if (playMinioEnabled) {
        await().until(() -> ZonedDateTime.now().isAfter(until));
      }

    } finally {
      minio.removeObject(RemoveObjectArgs.builder()
          .bucket(bucket)
          .object(objectName)
          .versionId(response.versionId())
          .bypassGovernanceMode(true)
          .build());
      minio.removeBucket(RemoveBucketArgs.builder()
          .bucket(bucket)
          .build());
    }
  }

  /**
   * Select object content.
   *
   * @throws Exception the exception
   */
  @Order(1090)
  @Test
  void selectObjectContent() throws Exception {
    when(mockClient.selectObjectContent(any(SelectObjectContentArgs.class)))
        .thenAnswer(invocationOnMock -> new SelectResponseStream(
            new ByteArrayInputStream(new byte[0])));
    try (SelectResponseStream stream = mockMinio.selectObjectContent(
        SelectObjectContentArgs.builder()
            .bucket(DEFAULT_BUCKET)
            .object(UUID.randomUUID().toString())
            .sqlExpression("select * from S3Object")
            .inputSerialization(new InputSerialization())
            .outputSerialization(new OutputSerialization(';'))
            .build())) {
      assertNotNull(stream);
    }
  }

  /**
   * Presigned post policy.
   *
   * @throws Exception the exception
   */
  @Order(1100)
  @Test
  void presignedPostPolicy() throws Exception {
//    when(mockClient.presignedPostPolicy(any(PostPolicy.class)))
//        .thenReturn(Collections.singletonMap("key", "value"));
//
//    Map<String, String> readMap = mockMinio.presignedPostPolicy(
//        new PostPolicy(
//            DEFAULT_BUCKET,
//            "a.txt",
//            ZonedDateTime.now().plus(1L, ChronoUnit.DAYS)));
//    assertNotNull(readMap);
  }

  /**
   * Object exists.
   */
  @Order(1110)
  @Test
  void objectExists() {
    String objectName = UUID.randomUUID().toString() + ".txt";
    byte[] value = "Hello Minio".getBytes(StandardCharsets.UTF_8);
    try {
      ObjectWriteResponse response = embeddedMinio.putObject(PutObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .stream(new ByteArrayInputStream(value), value.length, -1)
          .contentType(MediaType.TEXT_PLAIN_VALUE)
          .build());
      assertNotNull(response);

      assertTrue(embeddedMinio.objectExists(StatObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(response.object())
          .build()));

      assertFalse(embeddedMinio.objectExists(StatObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(UUID.randomUUID().toString())
          .build()));

    } finally {
      embeddedMinio.removeObject(RemoveObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .build());
    }
  }
}
