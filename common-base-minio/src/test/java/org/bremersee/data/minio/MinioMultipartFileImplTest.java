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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.minio.GetObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;
import javax.validation.constraints.NotEmpty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;

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

  private static final StatObjectResponse emptyObjectStat = mock(StatObjectResponse.class);

  private static final StatObjectResponse objectStat = mock(StatObjectResponse.class);

  private static final byte[] content = "Hello".getBytes(StandardCharsets.UTF_8);

  private static final MinioObjectInfo objectInfo = new MinioObjectInfo() {
    @Override
    public String getBucket() {
      return bucket;
    }

    @Override
    public String getRegion() {
      return null;
    }

    @Override
    public String getEtag() {
      return etag;
    }

    @Override
    public OffsetDateTime getLastModified() {
      return OffsetDateTime.ofInstant(time.toInstant(), ZoneOffset.UTC);
    }

    @Override
    public @NotEmpty String getName() {
      return name;
    }

    @Override
    public String getVersionId() {
      return versionId;
    }
  };

  /**
   * Sets up.
   */
  @BeforeAll
  static void setUp() {
    when(emptyObjectStat.object()).thenReturn(name);
    when(emptyObjectStat.bucket()).thenReturn(bucket);
    when(emptyObjectStat.contentType()).thenReturn(MediaType.TEXT_PLAIN_VALUE);
    when(emptyObjectStat.lastModified()).thenReturn(time);
    when(emptyObjectStat.etag()).thenReturn(etag);
    when(emptyObjectStat.size()).thenReturn(0L);
    when(emptyObjectStat.versionId()).thenReturn(versionId);

    when(objectStat.object()).thenReturn(name);
    when(objectStat.bucket()).thenReturn(bucket);
    when(objectStat.contentType()).thenReturn(MediaType.TEXT_PLAIN_VALUE);
    when(objectStat.lastModified()).thenReturn(time);
    when(objectStat.etag()).thenReturn(etag);
    when(objectStat.size()).thenReturn((long) content.length);
    when(objectStat.versionId()).thenReturn(versionId);

    when(minioOperations.statObject(any(StatObjectArgs.class))).thenReturn(objectStat);
    when(minioOperations.getObject(any(GetObjectArgs.class)))
        .then(invocationOnMock -> new ByteArrayInputStream(content));
  }

  /**
   * Illegal constructors.
   */
  @Test
  void illegalConstructors() {
    assertThrows(IllegalArgumentException.class,
        () -> new MinioMultipartFileImpl(null, objectInfo));
    assertThrows(IllegalArgumentException.class,
        () -> new MinioMultipartFileImpl(minioOperations, null));
    assertThrows(IllegalArgumentException.class,
        () -> new MinioMultipartFileImpl(null, null, emptyObjectStat));
    assertThrows(IllegalArgumentException.class,
        () -> new MinioMultipartFileImpl(minioOperations, null, null));
    assertThrows(IllegalArgumentException.class,
        () -> new MinioMultipartFileImpl(null, null, bucket, mock(Item.class)));
    assertThrows(IllegalArgumentException.class,
        () -> new MinioMultipartFileImpl(minioOperations, null, bucket, null));
  }

  /**
   * Gets object status.
   */
  @Test
  void getObjectStatus() {
    MinioMultipartFileImpl file = new MinioMultipartFileImpl(
        minioOperations,
        objectInfo);
    assertNotNull(file.getObjectStatus());
  }

  /**
   * Gets name.
   */
  @Test
  void getName() {
    MinioMultipartFileImpl file = new MinioMultipartFileImpl(
        minioOperations,
        null,
        objectStat);
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
        objectStat);
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
        emptyObjectStat);
    assertTrue(file.isEmpty());

    file = new MinioMultipartFileImpl(
        minioOperations,
        objectInfo);
    assertFalse(file.isEmpty());
  }

  /**
   * Gets size.
   */
  @Test
  void getSize() {
    MinioMultipartFileImpl file = new MinioMultipartFileImpl(
        minioOperations,
        null,
        emptyObjectStat);
    assertEquals(0L, file.getSize());

    file = new MinioMultipartFileImpl(
        minioOperations,
        objectInfo);
    assertEquals(content.length, (int) file.getSize());
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
        emptyObjectStat);
    assertArrayEquals(new byte[0], file.getBytes());

    file = new MinioMultipartFileImpl(
        minioOperations,
        objectInfo);
    byte[] bytes = file.getBytes();
    assertArrayEquals(content, bytes);
  }

  /**
   * Gets input stream.
   *
   * @throws Exception the exception
   */
  @Test
  void getInputStream() throws Exception {
    MinioMultipartFileImpl file = new MinioMultipartFileImpl(
        minioOperations,
        null,
        emptyObjectStat);
    assertNotNull(file.getInputStream());

    file = new MinioMultipartFileImpl(
        minioOperations,
        objectInfo);
    try (InputStream in = file.getInputStream()) {
      assertArrayEquals(content, FileCopyUtils.copyToByteArray(in));
    }
  }

  /**
   * Gets etag.
   */
  @Test
  void getEtag() {
    MinioMultipartFileImpl file = new MinioMultipartFileImpl(
        minioOperations,
        null,
        emptyObjectStat);
    assertEquals(etag, file.getEtag());

    file = new MinioMultipartFileImpl(
        minioOperations,
        objectInfo);
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
        emptyObjectStat);
    assertEquals(time.toInstant(), file.getLastModified().toInstant());
  }

  /**
   * Gets region.
   */
  @Test
  void getRegion() {
    MinioMultipartFileImpl file = new MinioMultipartFileImpl(
        minioOperations,
        null,
        emptyObjectStat);
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
        emptyObjectStat);
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
        emptyObjectStat);
    assertEquals(versionId, file.getVersionId());
  }

  /**
   * Transfer to.
   *
   * @throws Exception the exception
   */
  @Test
  void transferTo() throws Exception {
    File destFile = null;
    try {
      destFile = File
          .createTempFile("junit", ".txt", new File(System.getProperty("java.io.tmpdir")));
      MinioMultipartFileImpl file = new MinioMultipartFileImpl(
          minioOperations,
          objectInfo);
      file.transferTo(destFile);
      assertArrayEquals(content, FileCopyUtils.copyToByteArray(destFile));

    } finally {
      if (destFile != null) {
        Files.delete(destFile.toPath());
      }
    }
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
        emptyObjectStat);
    assertFalse(file0.equals(null));
    assertFalse(file0.equals(new Object()));
    assertTrue(file0.equals(file0));
    MinioMultipartFileImpl file1 = new MinioMultipartFileImpl(
        minioOperations,
        file0);
    assertTrue(file0.equals(file1));
    assertEquals(file0.hashCode(), file1.hashCode());
    assertEquals(file0.toString(), file1.toString());
  }
}