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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.IsVersioningEnabledArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.Time;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.Item;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import org.bremersee.web.multipart.FileAwareMultipartFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

/**
 * The minio repository implementation test.
 *
 * @author Christian Bremer
 */
class MinioRepositoryImplTest {

  private static MinioRepository repository;

  private static final MinioOperations minioOperations = mock(MinioOperations.class);

  private static final String bucket = "testbucket";

  private static final String name = "test.txt";

  private static final String etag = "1234";

  private static final String versionId = "5678";

  private static final long size = 15L;

  private static final ZonedDateTime time = ZonedDateTime.now();

  /**
   * Sets up.
   */
  @BeforeAll
  static void setUp() {
    repository = new MinioRepositoryImpl(
        MinioClient.builder()
            .endpoint(HttpUrl.get("https://play.min.io"))
            .credentials("Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG")
            .build(),
        null,
        "testbucket",
        false,
        false,
        Duration.ofDays(1L));

    when(minioOperations.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

    when(minioOperations.isVersioningEnabled(any(IsVersioningEnabledArgs.class))).thenReturn(false);

    when(minioOperations.putObject(any(PutObjectArgs.class)))
        .thenReturn(new ObjectWriteResponse(
            Headers.of(Collections.emptyMap()), bucket, null, name, etag, versionId));

    when(minioOperations.objectExists(any(StatObjectArgs.class))).thenReturn(true);

    Map<String, String> headers = new LinkedHashMap<>();
    headers.put("Content-Type", MediaType.TEXT_PLAIN_VALUE);
    headers.put("Last-Modified", time.format(Time.HTTP_HEADER_DATE_FORMAT));
    headers.put("Content-Length", String.valueOf(size));
    headers.put("ETag", etag);
    when(minioOperations.statObject(any(StatObjectArgs.class))).thenReturn(new ObjectStat(
        bucket, name, Headers.of(headers)
    ));

    Item item = mock(Item.class);
    when(item.etag()).thenReturn(etag);
    when(item.isDeleteMarker()).thenReturn(false);
    when(item.isDir()).thenReturn(false);
    when(item.isLatest()).thenReturn(true);
    when(item.lastModified()).thenReturn(ZonedDateTime.now());
    when(item.objectName()).thenReturn(name);
    when(item.size()).thenReturn(size);
    when(item.versionId()).thenReturn(versionId);

    when(minioOperations.listObjects(any(ListObjectsArgs.class)))
        .thenReturn(Collections.singletonList(new Result<>(item)));

    when(minioOperations.removeObjects(any(RemoveObjectsArgs.class)))
        .thenReturn(Collections.singletonList(new Result<>(new DeleteError())));

    when(minioOperations.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
        .thenReturn("https://example.org/somewhere");

    repository = new MinioRepositoryImpl(
        minioOperations,
        null,
        bucket,
        true,
        true,
        Duration.ofDays(1L));
  }

  /**
   * Gets minio operations.
   */
  @Test
  void getMinioOperations() {
    assertNotNull(repository.getMinioOperations());
  }

  /**
   * Gets region.
   */
  @Test
  void getRegion() {
    assertNull(repository.getRegion());
  }

  /**
   * Gets bucket.
   */
  @Test
  void getBucket() {
    assertEquals("testbucket", repository.getBucket());
  }

  /**
   * Versioning.
   */
  @Test
  void versioning() {
    assertTrue(repository.isVersioningEnabled());
  }

  /**
   * Save.
   *
   * @throws Exception the exception
   */
  @Test
  void save() throws Exception {
    Optional<ObjectWriteResponse> response = repository.save(
        MinioObjectId.from("test"),
        FileAwareMultipartFile.empty(),
        DeleteMode.NEVER);
    assertFalse(response.isPresent());

    MultipartFile multipartFile = new FileAwareMultipartFile(
        new ByteArrayInputStream("Hello".getBytes(StandardCharsets.UTF_8)),
        "file",
        "a.txt",
        MediaType.TEXT_PLAIN_VALUE);
    response = repository.save(MinioObjectId.from(name), multipartFile, DeleteMode.ALWAYS);
    assertTrue(response.isPresent());
    ObjectWriteResponse obj = response.get();
    assertEquals(name, obj.object());
    assertEquals(versionId, obj.versionId());
    assertEquals(etag, obj.etag());
    verify(minioOperations).putObject(any(PutObjectArgs.class));
  }

  /**
   * Exists.
   */
  @Test
  void exists() {
    assertTrue(repository.exists(MinioObjectId.from(name, versionId)));
    verify(minioOperations).objectExists(any(StatObjectArgs.class));
  }

  /**
   * Find one.
   */
  @Test
  void findOne() {
    Optional<MinioMultipartFile> response = repository.findOne(MinioObjectId.from(name));
    assertTrue(response.isPresent());
    MinioMultipartFile file = response.get();
    assertEquals(name, file.getName());
    assertEquals(bucket, file.getBucket());
    assertEquals(etag, file.getEtag());
    assertEquals(size, file.getSize());
    verify(minioOperations).statObject(any(StatObjectArgs.class));
  }

  /**
   * Find all.
   */
  @Test
  void findAll() {
    List<MinioMultipartFile> files = repository.findAll();
    assertNotNull(files);
    verify(minioOperations).listObjects(any(ListObjectsArgs.class));
  }

  /**
   * Delete.
   */
  @Test
  void delete() {
    repository.delete(MinioObjectId.from(name, versionId));
    verify(minioOperations).removeObject(any(RemoveObjectArgs.class));
  }

  /**
   * Delete all.
   */
  @Test
  void deleteAll() {
    List<DeleteError> results = repository
        .deleteAll(Collections.singletonList(MinioObjectId.from(name)));
    assertNotNull(results);
    verify(minioOperations).removeObjects(any(RemoveObjectsArgs.class));
  }

  /**
   * Gets presigned object url.
   */
  @Test
  void getPresignedObjectUrl() {
    String url = repository.getPresignedObjectUrl(MinioObjectId.from(name), Method.GET);
    assertNotNull(url);
    verify(minioOperations).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
  }
}