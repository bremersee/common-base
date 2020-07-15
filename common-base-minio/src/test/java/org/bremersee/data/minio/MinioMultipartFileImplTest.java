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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.minio.ObjectStat;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/**
 * The minio multipart file implementation test.
 *
 * @author Christian Bremer
 */
class MinioMultipartFileImplTest {

  private static final String bucket = UUID.randomUUID().toString();

  private static final String name = UUID.randomUUID().toString();

  private static final String versionId = UUID.randomUUID().toString();

  private static final ZonedDateTime time = ZonedDateTime.now();

  private static final String etag = UUID.randomUUID().toString();

  private static final MinioOperations minioOperations = mock(MinioOperations.class);

  private static final ObjectStat objectStat = mock(ObjectStat.class);

  /**
   * Sets up.
   */
  @BeforeAll
  static void setUp() {
    when(objectStat.name()).thenReturn(name);
    when(objectStat.bucketName()).thenReturn(bucket);
    when(objectStat.contentType()).thenReturn(MediaType.TEXT_PLAIN_VALUE);
    when(objectStat.createdTime()).thenReturn(time);
    when(objectStat.etag()).thenReturn(etag);
    when(objectStat.length()).thenReturn(0L);
  }

  /**
   * Gets name.
   */
  @Test
  void getName() {
    MinioMultipartFileImpl file = new MinioMultipartFileImpl(
        minioOperations,
        null,
        objectStat,
        versionId);
    assertEquals(name, file.getName());
    assertEquals(name, file.getOriginalFilename());
  }

  /**
   * Gets content type.
   */
  @Test
  void getContentType() {
    MinioMultipartFileImpl file = new MinioMultipartFileImpl(
        minioOperations,
        null,
        objectStat,
        versionId);
    assertEquals(MediaType.TEXT_PLAIN_VALUE, file.getContentType());
  }

  /**
   * Is empty.
   */
  @Test
  void isEmpty() {
    MinioMultipartFileImpl file = new MinioMultipartFileImpl(
        minioOperations,
        null,
        objectStat,
        versionId);
    assertTrue(file.isEmpty());
  }

  /**
   * Gets size.
   */
  @Test
  void getSize() {
    MinioMultipartFileImpl file = new MinioMultipartFileImpl(
        minioOperations,
        null,
        objectStat,
        versionId);
    assertEquals(0L, file.getSize());
  }

  /**
   * Gets bytes.
   *
   * @throws Exception the exception
   */
  @Test
  void getBytes() throws Exception {
    MinioMultipartFileImpl file = new MinioMultipartFileImpl(
        minioOperations,
        null,
        objectStat,
        versionId);
    assertArrayEquals(new byte[0], file.getBytes());
  }

  /**
   * Gets input stream.
   */
  @Test
  void getInputStream() {
    MinioMultipartFileImpl file = new MinioMultipartFileImpl(
        minioOperations,
        null,
        objectStat,
        versionId);
    assertNotNull(file.getInputStream());
  }

  /**
   * Gets etag.
   */
  @Test
  void getEtag() {
    MinioMultipartFileImpl file = new MinioMultipartFileImpl(
        minioOperations,
        null,
        objectStat,
        versionId);
    assertEquals(etag, file.getEtag());
  }

  /**
   * Gets created time.
   */
  @Test
  void getCreatedTime() {
    MinioMultipartFileImpl file = new MinioMultipartFileImpl(
        minioOperations,
        null,
        objectStat,
        versionId);
    assertEquals(time.toInstant(), file.getCreatedTime().toInstant());
  }

  /**
   * Gets region.
   */
  @Test
  void getRegion() {
    MinioMultipartFileImpl file = new MinioMultipartFileImpl(
        minioOperations,
        null,
        objectStat,
        versionId);
    assertNull(file.getRegion());
  }

  /**
   * Gets bucket.
   */
  @Test
  void getBucket() {
    MinioMultipartFileImpl file = new MinioMultipartFileImpl(
        minioOperations,
        null,
        objectStat,
        versionId);
    assertEquals(bucket, file.getBucket());
  }

  /**
   * Gets version id.
   */
  @Test
  void getVersionId() {
    MinioMultipartFileImpl file = new MinioMultipartFileImpl(
        minioOperations,
        null,
        objectStat,
        versionId);
    assertEquals(versionId, file.getVersionId());
  }

  /**
   * Equals and hash code.
   */
  @SuppressWarnings({"SimplifiableJUnitAssertion", "ConstantConditions", "EqualsWithItself"})
  @Test
  void equalsAndHashCode() {
    MinioMultipartFileImpl file0 = new MinioMultipartFileImpl(
        minioOperations,
        null,
        objectStat,
        versionId);
    MinioMultipartFileImpl file1 = new MinioMultipartFileImpl(
        minioOperations,
        file0);
    assertFalse(file0.equals(null));
    assertFalse(file0.equals(new Object()));
    assertTrue(file0.equals(file0));
    assertTrue(file0.equals(file1));
    assertEquals(file0.hashCode(), file1.hashCode());
    assertEquals(file0.toString(), file1.toString());
  }
}