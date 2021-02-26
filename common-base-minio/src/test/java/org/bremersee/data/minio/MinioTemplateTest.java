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
import io.minio.DeleteBucketLifecycleArgs;
import io.minio.DeleteBucketNotificationArgs;
import io.minio.DeleteBucketPolicyArgs;
import io.minio.DeleteBucketReplicationArgs;
import io.minio.DeleteBucketTagsArgs;
import io.minio.DeleteObjectLockConfigurationArgs;
import io.minio.DeleteObjectTagsArgs;
import io.minio.DisableObjectLegalHoldArgs;
import io.minio.DownloadObjectArgs;
import io.minio.EnableObjectLegalHoldArgs;
import io.minio.GetBucketEncryptionArgs;
import io.minio.GetBucketLifecycleArgs;
import io.minio.GetBucketNotificationArgs;
import io.minio.GetBucketPolicyArgs;
import io.minio.GetBucketReplicationArgs;
import io.minio.GetBucketTagsArgs;
import io.minio.GetBucketVersioningArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectLockConfigurationArgs;
import io.minio.GetObjectResponse;
import io.minio.GetObjectRetentionArgs;
import io.minio.GetObjectTagsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.IsObjectLegalHoldEnabledArgs;
import io.minio.ListBucketsArgs;
import io.minio.ListObjectsArgs;
import io.minio.ListenBucketNotificationArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PostPolicy;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.SelectObjectContentArgs;
import io.minio.SelectResponseStream;
import io.minio.SetBucketEncryptionArgs;
import io.minio.SetBucketLifecycleArgs;
import io.minio.SetBucketNotificationArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.SetBucketReplicationArgs;
import io.minio.SetBucketTagsArgs;
import io.minio.SetBucketVersioningArgs;
import io.minio.SetObjectLockConfigurationArgs;
import io.minio.SetObjectRetentionArgs;
import io.minio.SetObjectTagsArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.Time;
import io.minio.UploadObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import io.minio.messages.AndOperator;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteMarkerReplication;
import io.minio.messages.DeleteObject;
import io.minio.messages.ErrorResponse;
import io.minio.messages.Expiration;
import io.minio.messages.InputSerialization;
import io.minio.messages.Item;
import io.minio.messages.LifecycleConfiguration;
import io.minio.messages.LifecycleRule;
import io.minio.messages.NotificationConfiguration;
import io.minio.messages.NotificationRecords;
import io.minio.messages.ObjectLockConfiguration;
import io.minio.messages.OutputSerialization;
import io.minio.messages.ReplicationConfiguration;
import io.minio.messages.ReplicationDestination;
import io.minio.messages.ReplicationRule;
import io.minio.messages.Retention;
import io.minio.messages.RetentionDurationDays;
import io.minio.messages.RetentionMode;
import io.minio.messages.RuleFilter;
import io.minio.messages.SseAlgorithm;
import io.minio.messages.SseConfiguration;
import io.minio.messages.SseConfigurationRule;
import io.minio.messages.Status;
import io.minio.messages.Tags;
import io.minio.messages.VersioningConfiguration;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;

/**
 * The minio template test.
 *
 * @author Christian Bremer
 */
//@SpringBootTest(
//    classes = {TestConfiguration.class},
//    webEnvironment = WebEnvironment.NONE,
//    properties = {
//        "embedded.minio.browser=off"
//    })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS) // allows us to use @BeforeAll with a non-static method
@Slf4j
public class MinioTemplateTest {

  private static final SimpleDateFormat SDF = new SimpleDateFormat(
      "yyyy-MM-dd-HH-mm-ss", Locale.GERMANY);

  private static final String DEFAULT_BUCKET = "bremersee-"
      + SDF.format(new Date()) + "-"
      + UUID.randomUUID().toString().replace("-", "").toLowerCase();

//  @Value("${embedded.minio.host}")
//  private String minioHost;
//
//  @Value("${embedded.minio.port}")
//  private int minioPort;
//
//  @Value("${embedded.minio.accessKey}")
//  private String accessKey;
//
//  @Value("${embedded.minio.secretKey}")
//  private String secretKey;
//
//  private MinioTemplate embeddedMinio;

  private MinioTemplate playMinio;

  private MinioClient mockClient;

  private MinioTemplate mockMinio;

  private boolean playMinioEnabled = true;

  /**
   * Sets up.
   */
  @BeforeAll
  void setUp() {
//    log.info("minioHost = {}, minioPort = {}, accessKey = {}, secretKey = {}",
//        minioHost, minioPort, accessKey, secretKey);
//    MinioClient minioClient = MinioClient.builder()
//        .endpoint(minioHost, minioPort, false)
//        .credentials(accessKey, secretKey)
//        .build();
//    embeddedMinio = new MinioTemplate(minioClient);
//    embeddedMinio.setErrorHandler(null);
//    embeddedMinio.setErrorHandler(new DefaultMinioErrorHandler());
//    if (!embeddedMinio.bucketExists(BucketExistsArgs.builder().bucket(DEFAULT_BUCKET).build())) {
//      embeddedMinio.makeBucket(MakeBucketArgs.builder().bucket(DEFAULT_BUCKET).build());
//    }

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
      log.error("===> play.min.io is not available", e);
    }
    playMinioEnabled = false;
  }

  /**
   * After all.
   */
  @AfterAll
  void afterAll() {
//    embeddedMinio.removeBucket(RemoveBucketArgs.builder().bucket(DEFAULT_BUCKET).build());
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

  private MinioTemplate minioTemplate(String methodName, MockMinioClientConfigurator config) {
    if (playMinioEnabled) {
      log.info("Running '{}' with play.min.io", methodName);
      return playMinio;
    }
    log.info("Running '{}' with mocked minio client", methodName);
    Optional.ofNullable(config)
        .ifPresent(c -> c.configureMock(mockClient));
    return mockMinio;
  }

  /**
   * Clone minio template.
   */
  @Order(1)
  @Test
  void cloneMinioTemplate() {
    assertNotNull(mockMinio.clone());
    assertNotNull(mockMinio.clone(new DefaultMinioErrorHandler()));
  }

  /**
   * Make and remove bucket.
   */
  @Order(10)
  @Test
  void makeAndRemoveBucket() {
    String bucketName = newBucketName();

    MinioTemplate minio = minioTemplate("makeAndRemoveBucket", mock -> {
      when(mock.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
      Bucket bucket = Mockito.mock(Bucket.class);
      when(bucket.name()).thenReturn(bucketName);
      when(bucket.creationDate()).thenReturn(ZonedDateTime.now());
      when(mock.listBuckets(any(ListBucketsArgs.class))).thenReturn(List.of(bucket));
    });

    minio.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    assertTrue(minio.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()));

    List<Bucket> buckets = minio.listBuckets(ListBucketsArgs.builder().build());
    assertNotNull(buckets);
    assertFalse(buckets.isEmpty());
    assertTrue(buckets.stream().anyMatch(bucket -> bucket.name().equals(bucketName)));

    minio.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    if (playMinioEnabled) {
      assertFalse(minio.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()));
    }
  }

  /**
   * Make and remove bucket with object lock.
   */
  @Order(12)
  @Test
  void makeAndRemoveBucketWithObjectLock() {
    String bucketName = newBucketName();

    MinioTemplate minio = minioTemplate("makeAndRemoveBucketWithObjectLock", mock -> {
      Bucket bucket = Mockito.mock(Bucket.class);
      when(bucket.name()).thenReturn(bucketName);
      when(bucket.creationDate()).thenReturn(ZonedDateTime.now());
      when(mock.listBuckets()).thenReturn(List.of(bucket));
      when(mock.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
    });

    minio.makeBucket(MakeBucketArgs.builder()
        .bucket(bucketName)
        .objectLock(true)
        .build());
    List<Bucket> buckets = minio.listBuckets();
    assertNotNull(buckets);
    assertFalse(buckets.isEmpty());
    assertTrue(buckets.stream().anyMatch(bucket -> bucket.name().equals(bucketName)));

    minio.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    assertFalse(minio.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()));
  }

  /**
   * Bucket encryption.
   */
  @Order(20)
  @Test
  void bucketEncryption() {
    final String bucket = newBucketName();

    MinioTemplate minio = minioTemplate("bucketEncryption", mock ->
        when(mock.getBucketEncryption(any(GetBucketEncryptionArgs.class)))
            .thenReturn(new SseConfiguration(new SseConfigurationRule(SseAlgorithm.AES256, null)))
    );

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
   * Bucket lifecycle.
   */
  @Order(30)
  @Test
  void bucketLifecycle() {
    final String bucket = newBucketName();
    final LifecycleRule rule0 = new LifecycleRule(
        Status.ENABLED,
        null,
        new Expiration((ZonedDateTime) null, 365, null),
        new RuleFilter("logs/"),
        null,
        null,
        null,
        null
    );
    final LifecycleConfiguration config = new LifecycleConfiguration(List.of(rule0));

    MinioTemplate minio = minioTemplate("bucketLifecycle", mock ->
        when(mock.getBucketLifecycle(any(GetBucketLifecycleArgs.class)))
            .thenReturn(config)
    );

    minio.makeBucket(MakeBucketArgs.builder()
        .bucket(bucket)
        .objectLock(true)
        .build());

    try {
      minio.setBucketLifecycle(SetBucketLifecycleArgs.builder()
          .bucket(bucket)
          .config(config)
          .build());

      Optional<LifecycleConfiguration> optionalConfig = minio.getBucketLifecycle(GetBucketLifecycleArgs.builder()
          .bucket(bucket)
          .build());
      assertTrue(optionalConfig.isPresent());
      LifecycleConfiguration readConfig = optionalConfig.get();
      assertFalse(readConfig.rules().isEmpty());
      LifecycleRule readRule0 = readConfig.rules().get(0);
      assertEquals(Status.ENABLED, readRule0.status());

      minio.deleteBucketLifecycle(DeleteBucketLifecycleArgs.builder()
          .bucket(bucket)
          .build());
      optionalConfig = minio.getBucketLifecycle(GetBucketLifecycleArgs.builder()
          .bucket(bucket)
          .build());
      if (playMinioEnabled) {
        assertFalse(optionalConfig.isPresent());
      }

    } finally {
      minio.removeBucket(RemoveBucketArgs.builder()
          .bucket(bucket)
          .build());
    }
  }

  /**
   * Bucket notification.
   *
   * @throws Exception the exception
   */
  @Order(40)
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
   * Bucket policy.
   *
   * @throws Exception the exception
   */
  @Order(50)
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
   * Bucket replication.
   *
   * @throws Exception the exception
   */
  @Order(60)
  @Test
  void bucketReplication() throws Exception {
    Map<String, String> tags = new HashMap<>();
    tags.put("key1", "value1");
    tags.put("key2", "value2");
    ReplicationRule rule =
        new ReplicationRule(
            new DeleteMarkerReplication(Status.DISABLED),
            new ReplicationDestination(
                null,
                null,
                "REPLACE-WITH-ACTUAL-DESTINATION-BUCKET-ARN",
                null,
                null,
                null,
                null),
            null,
            new RuleFilter(new AndOperator("TaxDocs", tags)),
            "rule1",
            null,
            1,
            null,
            Status.ENABLED);
    List<ReplicationRule> rules = new LinkedList<>();
    rules.add(rule);
    ReplicationConfiguration config = new ReplicationConfiguration("REPLACE-WITH-ACTUAL-ROLE", rules);

    when(mockClient.getBucketReplication(any(GetBucketReplicationArgs.class))).thenReturn(config);

    mockMinio.setBucketReplication(SetBucketReplicationArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .config(config)
        .build());
    Optional<ReplicationConfiguration> optCfg = mockMinio.getBucketReplication(GetBucketReplicationArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .build());
    assertTrue(optCfg.isPresent());
    mockMinio.deleteBucketReplication(DeleteBucketReplicationArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .build());
  }

  /**
   * Bucket tags.
   */
  @Order(70)
  @Test
  void bucketTags() {
    Map<String, String> tagMap = Collections.singletonMap("my-key", "my-value");
    MinioTemplate minio = minioTemplate("bucketTags", mock ->
        when(mock.getBucketTags(any(GetBucketTagsArgs.class)))
            .thenReturn(Tags.newBucketTags(tagMap))
    );
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
   * Bucket versioning.
   */
  @Order(80)
  @Test
  void bucketVersioning() {

    final String bucketName = newBucketName();
    final VersioningConfiguration config = new VersioningConfiguration(VersioningConfiguration.Status.ENABLED, false);
    MinioTemplate minio = minioTemplate("bucketVersioning", mock ->
        when(mock.getBucketVersioning(any(GetBucketVersioningArgs.class))).thenReturn(config)
    );

    minio.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    try {
      minio.setBucketVersioning(SetBucketVersioningArgs.builder()
          .bucket(bucketName)
          .config(config)
          .build());

      VersioningConfiguration readConfig = minio.getBucketVersioning(GetBucketVersioningArgs.builder()
          .bucket(bucketName)
          .build());

      assertNotNull(readConfig);
      assertEquals(VersioningConfiguration.Status.ENABLED, readConfig.status());

    } finally {
      minio.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    }
  }

  /**
   * Object lock configuration.
   */
  @Order(90)
  @Test
  void objectLockConfiguration() {

    final String bucketName = newBucketName();
    final ObjectLockConfiguration config = new ObjectLockConfiguration(
        RetentionMode.COMPLIANCE,
        new RetentionDurationDays(100));
    MinioTemplate minio = minioTemplate("objectLockConfiguration", mock ->
        when(mock.getObjectLockConfiguration(any(GetObjectLockConfigurationArgs.class))).thenReturn(config)
    );

    minio.makeBucket(MakeBucketArgs.builder()
        .bucket(bucketName)
        .objectLock(true)
        .build());
    try {
      minio.setObjectLockConfiguration(SetObjectLockConfigurationArgs.builder()
          .bucket(bucketName)
          .config(config)
          .build());

      ObjectLockConfiguration readConfig = minio.getObjectLockConfiguration(GetObjectLockConfigurationArgs.builder()
          .bucket(bucketName)
          .build());
      assertNotNull(readConfig);
      assertEquals(RetentionMode.COMPLIANCE, readConfig.mode());

      minio.deleteObjectLockConfiguration(DeleteObjectLockConfigurationArgs.builder()
          .bucket(bucketName)
          .build());

    } finally {
      minio.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    }
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

    MinioTemplate minio = minioTemplate("putAndRemoveObject", mock -> {
      when(mock.putObject(any(PutObjectArgs.class)))
          .thenReturn(new ObjectWriteResponse(
              Headers.of(Collections.emptyMap()),
              DEFAULT_BUCKET,
              null,
              objectName,
              null,
              null
          ));
      when(mock.listObjects(any(ListObjectsArgs.class))).thenReturn(Collections.emptyList());
    });
    byte[] value = "Hello Minio".getBytes(StandardCharsets.UTF_8);
    ObjectWriteResponse response = minio.putObject(PutObjectArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .object(objectName)
        .stream(new ByteArrayInputStream(value), value.length, -1)
        .contentType(MediaType.TEXT_PLAIN_VALUE)
        .build());
    assertNotNull(response);

    boolean contains = false;
    Iterable<Result<Item>> results;
    if (playMinioEnabled) {
      results = minio
          .listObjects(ListObjectsArgs.builder().bucket(DEFAULT_BUCKET).build());
      for (Result<Item> result : results) {
        Item item = result.get();
        if (objectName.equals(item.objectName())) {
          contains = true;
          break;
        }
      }
      assertTrue(contains);
    }

    minio.removeObject(RemoveObjectArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .object(objectName)
        .build());

    results = minio
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
   * Upload object.
   *
   * @throws Exception the exception
   */
  @Order(1020)
  @Test
  void uploadObject() throws Exception {

    String objectName = UUID.randomUUID().toString() + ".txt";
    MinioTemplate minio = minioTemplate("uploadObject", mock -> {
      when(mock.uploadObject(any(UploadObjectArgs.class)))
          .thenReturn(new ObjectWriteResponse(
              Headers.of(Collections.emptyMap()),
              DEFAULT_BUCKET,
              null,
              objectName,
              null,
              null
          ));
      when(mock.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
          .thenReturn("http://localhost/somewhere");
    });
    byte[] value = "Hello Minio".getBytes(StandardCharsets.UTF_8);
    try {
      File file = File.createTempFile(
          "junit", ".tmp", new File(System.getProperty("java.io.tmpdir")));
      FileCopyUtils.copy(
          value,
          Files.newOutputStream(
              file.toPath(),
              StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING));
      ObjectWriteResponse response = minio.uploadObject(
          UploadObjectArgs.builder()
              .bucket(DEFAULT_BUCKET)
              .object(objectName)
              .filename(file.getAbsolutePath())
              .contentType(MediaType.TEXT_PLAIN_VALUE)
              .build(),
          DeleteMode.ALWAYS);
      assertNotNull(response);

      assertFalse(file.exists());
      String url = minio.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .method(Method.GET)
          .expiry(3600 * 24)
          .build());
      assertNotNull(url);
      if (playMinioEnabled) {
        try (InputStream in = new URL(url).openStream()) {
          byte[] readBytes = FileCopyUtils.copyToByteArray(in);
          assertArrayEquals(value, readBytes);
        }
      }

    } finally {
      minio.removeObject(RemoveObjectArgs.builder()
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
    MinioTemplate minio = minioTemplate("downloadObject", mock -> {
      when(mock.putObject(any(PutObjectArgs.class)))
          .thenReturn(new ObjectWriteResponse(
              Headers.of(Collections.emptyMap()),
              DEFAULT_BUCKET,
              null,
              objectName,
              null,
              null
          ));
      when(mock.uploadObject(any(UploadObjectArgs.class)))
          .thenReturn(new ObjectWriteResponse(
              Headers.of(Collections.emptyMap()),
              DEFAULT_BUCKET,
              null,
              objectName,
              null,
              null
          ));
    });
    byte[] value = "Hello Minio".getBytes(StandardCharsets.UTF_8);
    ObjectWriteResponse response = minio.putObject(PutObjectArgs.builder()
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
      minio.downloadObject(DownloadObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .filename(file.getAbsolutePath())
          .build());
      if (playMinioEnabled) {
        byte[] fileBytes = FileCopyUtils.copyToByteArray(file);
        assertArrayEquals(value, fileBytes);
      }

    } finally {
      if (file != null) {
        Files.delete(file.toPath());
      }
      minio.removeObject(RemoveObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .build());
    }
  }

  /**
   * Presigned object url.
   *
   * @throws Exception the exception
   */
  @Order(1040)
  @Test
  void presignedObjectUrl() throws Exception {

    String objectName = UUID.randomUUID().toString() + ".txt";
    MinioTemplate minio = minioTemplate("presignedObjectUrl", mock -> {
      when(mock.putObject(any(PutObjectArgs.class)))
          .thenReturn(new ObjectWriteResponse(
              Headers.of(Collections.emptyMap()),
              DEFAULT_BUCKET,
              null,
              objectName,
              null,
              null
          ));
      when(mock.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
          .thenReturn("http://localhost/somewhere");
    });
    byte[] value = "Hello Minio".getBytes(StandardCharsets.UTF_8);
    ObjectWriteResponse response = minio.putObject(PutObjectArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .object(objectName)
        .stream(new ByteArrayInputStream(value), value.length, -1)
        .contentType(MediaType.TEXT_PLAIN_VALUE)
        .build());
    assertNotNull(response);

    try {
      String url = minio.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .method(Method.GET)
          .expiry(3600 * 24)
          .build());
      assertNotNull(url);
      if (playMinioEnabled) {
        try (InputStream in = new URL(url).openStream()) {
          byte[] readBytes = FileCopyUtils.copyToByteArray(in);
          assertArrayEquals(value, readBytes);
        }
      }

    } finally {
      minio.removeObject(RemoveObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .build());
    }
  }

  /**
   * Presigned post form data.
   */
  @Order(1050)
  @Test
  void presignedPostFormData() {

    String objectName = UUID.randomUUID().toString() + ".txt";
    MinioTemplate minio = minioTemplate("presignedPostFormData", mock ->
        when(mock.getPresignedPostFormData(any(PostPolicy.class)))
            .thenReturn(Map.of("key", "value"))
    );

    PostPolicy policy = new PostPolicy(DEFAULT_BUCKET, ZonedDateTime.now().plusDays(7));
    policy.addEqualsCondition("key", objectName);
    policy.addStartsWithCondition("Content-Type", "text/");
    policy.addContentLengthRangeCondition(8, 8 * 1024);

    Map<String, String> formData = minio.getPresignedPostFormData(policy);
    assertNotNull(formData);
    assertFalse(formData.isEmpty());
  }

  /**
   * Stat object.
   */
  @Order(1060)
  @Test
  void statObject() {
    String objectName = UUID.randomUUID().toString() + ".txt";
    byte[] value = "Hello Minio".getBytes(StandardCharsets.UTF_8);
    MinioTemplate minio = minioTemplate("statObject", mock -> {
      when(mock.putObject(any(PutObjectArgs.class)))
          .thenReturn(new ObjectWriteResponse(
              Headers.of(Collections.emptyMap()),
              DEFAULT_BUCKET,
              null,
              objectName,
              null,
              null
          ));
      when(mock.statObject(any(StatObjectArgs.class)))
          .thenReturn(new StatObjectResponse(
              Headers.of(Map.of(
                  "Content-Length", String.valueOf(value.length),
                  "Last-Modified", ZonedDateTime.now().format(Time.HTTP_HEADER_DATE_FORMAT))),
              DEFAULT_BUCKET,
              null,
              objectName
          ));
    });
    ObjectWriteResponse response = minio.putObject(PutObjectArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .object(objectName)
        .stream(new ByteArrayInputStream(value), value.length, -1)
        .contentType(MediaType.TEXT_PLAIN_VALUE)
        .build());
    assertNotNull(response);

    try {
      StatObjectResponse statObject = minio.statObject(StatObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .build());
      assertNotNull(statObject);
      assertEquals(objectName, statObject.object());

    } finally {
      minio.removeObject(RemoveObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .build());
    }
  }

  /**
   * Object exists.
   */
  @Order(1070)
  @Test
  void objectExists() {
    String objectName = UUID.randomUUID().toString() + ".txt";
    byte[] value = "Hello Minio".getBytes(StandardCharsets.UTF_8);
    MinioTemplate minio = minioTemplate("objectExists", mock -> {
      when(mock.putObject(any(PutObjectArgs.class)))
          .thenReturn(new ObjectWriteResponse(
              Headers.of(Collections.emptyMap()),
              DEFAULT_BUCKET,
              null,
              objectName,
              null,
              null
          ));
      when(mock.statObject(any(StatObjectArgs.class)))
          .thenReturn(new StatObjectResponse(
              Headers.of(Map.of(
                  "Content-Length", String.valueOf(value.length),
                  "Last-Modified", ZonedDateTime.now().format(Time.HTTP_HEADER_DATE_FORMAT))),
              DEFAULT_BUCKET,
              null,
              objectName
          ));
    });
    ObjectWriteResponse response = minio.putObject(PutObjectArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .object(objectName)
        .stream(new ByteArrayInputStream(value), value.length, -1)
        .contentType(MediaType.TEXT_PLAIN_VALUE)
        .build());
    assertNotNull(response);

    try {
      assertTrue(minio.objectExists(StatObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .build()));

    } finally {
      minio.removeObject(RemoveObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .build());
    }
  }

  /**
   * Object not exists.
   */
  @Order(1080)
  @Test
  void objectNotExists() {
    String objectName = UUID.randomUUID().toString() + ".nil";
    MinioTemplate minio = minioTemplate("objectNotExists", mock ->
        when(mock.statObject(any(StatObjectArgs.class)))
            .thenThrow(new ErrorResponseException(
                new ErrorResponse("NoSuchKey", "message", DEFAULT_BUCKET, objectName, "123", "456", "789"),
                new Response.Builder()
                    .protocol(Protocol.HTTP_1_1)
                    .code(500)
                    .message("Internal server error")
                    .request(new Request.Builder()
                        .url("http://example.org")
                        .build())
                    .build(),
                HttpStatus.NOT_FOUND.getReasonPhrase()))
    );
    assertFalse(minio.objectExists(StatObjectArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .object(objectName)
        .build()));
  }

  /**
   * Gets object.
   *
   * @throws Exception the exception
   */
  @Order(1090)
  @Test
  void getObject() throws Exception {
    String objectName = UUID.randomUUID().toString() + ".txt";
    byte[] value = "Hello Minio".getBytes(StandardCharsets.UTF_8);
    MinioTemplate minio = minioTemplate("getObject", mock -> {
      when(mock.putObject(any(PutObjectArgs.class)))
          .thenReturn(new ObjectWriteResponse(
              Headers.of(Collections.emptyMap()),
              DEFAULT_BUCKET,
              null,
              objectName,
              null,
              null
          ));
      when(mock.getObject(any(GetObjectArgs.class)))
          .then(invocationOnMock -> new GetObjectResponse(
              Headers.of(Collections.emptyMap()),
              DEFAULT_BUCKET,
              null,
              objectName,
              new ByteArrayInputStream(value)
          ));
    });
    ObjectWriteResponse response = minio.putObject(PutObjectArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .object(objectName)
        .stream(new ByteArrayInputStream(value), value.length, -1)
        .contentType(MediaType.TEXT_PLAIN_VALUE)
        .build());
    assertNotNull(response);

    try (InputStream in = minio.getObject(GetObjectArgs.builder().bucket(DEFAULT_BUCKET).object(objectName).build())) {
      byte[] readValue = FileCopyUtils.copyToByteArray(in);
      assertArrayEquals(value, readValue);

    } finally {
      minio.removeObject(RemoveObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .build());
    }
  }

  /**
   * Select object content.
   *
   * @throws Exception the exception
   */
  @Order(1100)
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
   * Compose object.
   *
   * @throws Exception the exception
   */
  @Order(1110)
  @Test
  void composeObject() throws Exception {

    String destBucket = newBucketName();
    String destObjectName = UUID.randomUUID().toString() + ".txt";

    MinioTemplate minio = minioTemplate("composeObject", mock -> {
      when(mock.putObject(any(PutObjectArgs.class)))
          .thenReturn(new ObjectWriteResponse(
              Headers.of(Collections.emptyMap()),
              DEFAULT_BUCKET,
              null,
              "any.dat",
              null,
              null
          ));
      when(mock.composeObject(any(ComposeObjectArgs.class)))
          .thenReturn(new ObjectWriteResponse(
              Headers.of(Collections.emptyMap()),
              destBucket,
              null,
              destObjectName,
              null,
              null
          ));
      when(mock.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
          .thenReturn("http://localhost/somewhere");
    });

    Random random = new Random();
    final int size = 5242882;
    String objectName0 = UUID.randomUUID().toString() + ".dat";
    byte[] value0 = new byte[size];
    random.nextBytes(value0);
    ObjectWriteResponse response = minio.putObject(PutObjectArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .object(objectName0)
        .stream(new ByteArrayInputStream(value0), value0.length, -1)
        .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
        .build());
    assertNotNull(response);

    String objectName1 = UUID.randomUUID().toString() + ".dat";
    byte[] value1 = new byte[size];
    random.nextBytes(value1);
    response = minio.putObject(PutObjectArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .object(objectName1)
        .stream(new ByteArrayInputStream(value1), value1.length, -1)
        .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
        .build());
    assertNotNull(response);

    boolean destObjectExists = false;
    minio.makeBucket(MakeBucketArgs.builder().bucket(destBucket).build());
    try {
      response = minio.composeObject(ComposeObjectArgs.builder()
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

      String url = minio.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
          .bucket(destBucket)
          .object(destObjectName)
          .method(Method.GET)
          .expiry(3600 * 24)
          .build());
      assertNotNull(url);
      if (playMinioEnabled) {
        try (InputStream in = new URL(url).openStream()) {
          byte[] readBytes = FileCopyUtils.copyToByteArray(in);
          assertEquals(2 * size, readBytes.length);
        }
      }

    } finally {
      minio.removeObject(RemoveObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName0)
          .build());
      minio.removeObject(RemoveObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName1)
          .build());
      if (destObjectExists) {
        minio.removeObject(RemoveObjectArgs.builder()
            .bucket(destBucket)
            .object(destObjectName)
            .build());
      }
      minio.removeBucket(RemoveBucketArgs.builder().bucket(destBucket).build());
    }
  }

  /**
   * Copy object.
   */
  @Order(1120)
  @Test
  void copyObject() {

    String objectName = UUID.randomUUID().toString() + ".txt";
    String destBucket = newBucketName();
    String destObjectName = UUID.randomUUID().toString() + ".txt";
    byte[] value = "Hello Minio".getBytes(StandardCharsets.UTF_8);

    MinioTemplate minio = minioTemplate("copyObject", mock -> {
      when(mockClient.putObject(any(PutObjectArgs.class)))
          .thenReturn(new ObjectWriteResponse(
              Headers.of(Collections.emptyMap()),
              DEFAULT_BUCKET,
              null,
              objectName,
              null,
              "1234"
          ));
      when(mock.copyObject(any(CopyObjectArgs.class)))
          .thenReturn(new ObjectWriteResponse(
              Headers.of(Collections.emptyMap()),
              destBucket,
              null,
              destObjectName,
              null,
              null));
      when(mock.statObject(any(StatObjectArgs.class)))
          .thenReturn(new StatObjectResponse(
              Headers.of(Map.of(
                  "Content-Length", String.valueOf(value.length),
                  "Last-Modified", ZonedDateTime.now().format(Time.HTTP_HEADER_DATE_FORMAT))),
              destBucket,
              null,
              destObjectName
          ));
      when(mock.removeObjects(any(RemoveObjectsArgs.class)))
          .thenReturn(Collections.emptyList());
    });

    ObjectWriteResponse response = minio.putObject(PutObjectArgs.builder()
        .bucket(DEFAULT_BUCKET)
        .object(objectName)
        .stream(new ByteArrayInputStream(value), value.length, -1)
        .contentType(MediaType.TEXT_PLAIN_VALUE)
        .build());
    assertNotNull(response);

    boolean destObjectExists = false;
    minio.makeBucket(MakeBucketArgs.builder().bucket(destBucket).build());
    try {
      ObjectWriteResponse copyResponse = minio.copyObject(CopyObjectArgs.builder()
          .bucket(destBucket)
          .object(destObjectName)
          .source(CopySource.builder()
              .bucket(DEFAULT_BUCKET)
              .object(objectName)
              .build())
          .build());
      assertNotNull(copyResponse);

      StatObjectResponse objectStat = minio.statObject(StatObjectArgs.builder()
          .bucket(destBucket)
          .object(destObjectName)
          .build());
      assertNotNull(objectStat);
      destObjectExists = true;
      assertEquals(value.length, (int) objectStat.size());

      Iterable<Result<DeleteError>> errors = minio.removeObjects(RemoveObjectsArgs
          .builder()
          .bucket(destBucket)
          .objects(Collections.singletonList(new DeleteObject(destObjectName)))
          .build());
      boolean hasError = false;
      for (Result<DeleteError> error : errors) {
        hasError = error != null;
      }
      assertFalse(hasError);

      if (playMinioEnabled) {
        assertThrows(MinioException.class, () -> minio.statObject(StatObjectArgs.builder()
            .bucket(destBucket)
            .object(destObjectName)
            .build()));
      }
      destObjectExists = false;

    } finally {
      minio.removeObject(RemoveObjectArgs.builder()
          .bucket(DEFAULT_BUCKET)
          .object(objectName)
          .build());
      if (destObjectExists) {
        minio.removeObject(RemoveObjectArgs.builder()
            .bucket(destBucket)
            .object(destObjectName)
            .build());
      }
      minio.removeBucket(RemoveBucketArgs.builder().bucket(destBucket).build());
    }
  }

  /**
   * Object retention.
   */
  @Order(1130)
  @Test
  void objectRetention() {
    String bucket = newBucketName();
    String objectName = UUID.randomUUID().toString() + ".txt";
    ZonedDateTime until = ZonedDateTime.now().plus(10L, ChronoUnit.SECONDS);

    MinioTemplate minio = minioTemplate("objectRetention", mock -> {
      when(mock.putObject(any(PutObjectArgs.class)))
          .thenReturn(new ObjectWriteResponse(
              Headers.of(Collections.emptyMap()),
              bucket,
              null,
              objectName,
              null,
              "1234"
          ));
      when(mock.getObjectRetention(any(GetObjectRetentionArgs.class)))
          .thenReturn(new Retention(RetentionMode.COMPLIANCE, until));
    });

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
   * Object tags.
   */
  @Order(1140)
  @Test
  void objectTags() {

    String bucket = newBucketName();
    String objectName = UUID.randomUUID().toString() + ".txt";

    MinioTemplate minio = minioTemplate("objectTags", mock -> {
      when(mock.putObject(any(PutObjectArgs.class)))
          .thenReturn(new ObjectWriteResponse(
              Headers.of(Collections.emptyMap()),
              bucket,
              null,
              objectName,
              null,
              null
          ));
      when(mock.getObjectTags(any(GetObjectTagsArgs.class)))
          .thenReturn(Tags.newObjectTags(Map.of("my-key", "my-value")));
    });

    minio.makeBucket(MakeBucketArgs.builder()
        .bucket(bucket)
        .objectLock(false)
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
      minio.setObjectTags(SetObjectTagsArgs.builder()
          .bucket(bucket)
          .object(objectName)
          .tags(Map.of("my-key", "my-value"))
          .build());

      Tags tags = minio.getObjectTags(GetObjectTagsArgs.builder()
          .bucket(bucket)
          .object(objectName)
          .build());
      assertNotNull(tags);
      assertEquals("my-value", tags.get().get("my-key"));

      minio.deleteObjectTags(DeleteObjectTagsArgs.builder()
          .bucket(bucket)
          .object(objectName)
          .build());

      if (playMinioEnabled) {
        tags = minio.getObjectTags(GetObjectTagsArgs.builder()
            .bucket(bucket)
            .object(objectName)
            .build());
        assertNotNull(tags);
        assertTrue(tags.get().isEmpty());
      }

    } finally {
      minio.removeObject(RemoveObjectArgs.builder()
          .bucket(bucket)
          .object(objectName)
          .build());
      minio.removeBucket(RemoveBucketArgs.builder()
          .bucket(bucket)
          .build());
    }
  }

  /**
   * Object legal hold.
   */
  @Order(1150)
  @Test
  void objectLegalHold() {
    String bucket = newBucketName();
    String objectName = UUID.randomUUID().toString() + ".txt";

    MinioTemplate minio = minioTemplate("objectLegalHold", mock -> {
      when(mock.putObject(any(PutObjectArgs.class)))
          .thenReturn(new ObjectWriteResponse(
              Headers.of(Collections.emptyMap()),
              bucket,
              null,
              objectName,
              null,
              null
          ));
      when(mock.isObjectLegalHoldEnabled(any(IsObjectLegalHoldEnabledArgs.class)))
          .thenReturn(true);
    });

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
   * The interface Mock minio client configurator.
   */
  interface MockMinioClientConfigurator {

    /**
     * Configure.
     *
     * @param minioClient the minio client
     * @throws Exception the exception
     */
    void configure(MinioClient minioClient) throws Exception;

    /**
     * Configure mock.
     *
     * @param minioClient the minio client
     */
    default void configureMock(MinioClient minioClient) {
      try {
        configure(minioClient);

      } catch (Exception e) {
        if (e instanceof RuntimeException) {
          throw (RuntimeException) e;
        }
        throw new RuntimeException(e);
      }
    }
  }
}
