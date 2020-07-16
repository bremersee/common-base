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

package org.bremersee.web.multipart;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * The file aware multipart file test.
 *
 * @author Christian Bremer
 */
class FileAwareMultipartFileTest {

  /**
   * Test empty multipart file.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings({"ConstantConditions", "SimplifiableJUnitAssertion", "EqualsWithItself"})
  @Test
  void empty() throws Exception {
    MultipartFile file = FileAwareMultipartFile.empty();
    assertNotNull(file);
    assertTrue(file.isEmpty());

    file = new FileAwareMultipartFile(file);
    assertNotNull(file);
    assertTrue(file.isEmpty());

    Resource resource0 = file.getResource();
    assertNotNull(resource0);
    assertNotNull(resource0.getDescription());
    assertNotNull(resource0.getInputStream());

    file = new FileAwareMultipartFile(null, System.getProperty("java.io.tmpdir"));
    assertNotNull(file);
    assertTrue(file.isEmpty());

    Resource resource1 = file.getResource();
    assertFalse(resource0.equals(null));
    assertFalse(resource0.equals(new Object()));
    assertTrue(resource0.equals(resource0));
    assertTrue(resource0.equals(resource1));
    assertEquals(resource0.hashCode(), resource1.hashCode());
    assertEquals(resource0.toString(), resource1.toString());
  }

  /**
   * Delete.
   *
   * @throws Exception the exception
   */
  @Test
  void delete() throws Exception {
    File file = File.createTempFile(
        "junit",
        ".txt",
        new File(System.getProperty("java.io.tmpdir")));
    try {
      FileCopyUtils.copy("Hello", new FileWriter(file));
      MultipartFile multipartFile = new FileAwareMultipartFile(
          file,
          "foo",
          file.getName(),
          MediaType.TEXT_PLAIN_VALUE);
      FileAwareMultipartFile.delete(multipartFile);
      assertFalse(file.exists());

    } finally {
      if (file.exists()) {
        Files.delete(file.toPath());
      }
    }
  }

  /**
   * Gets name.
   *
   * @throws Exception the exception
   */
  @Test
  void getName() throws Exception {
    MultipartFile file = new FileAwareMultipartFile(
        new ByteArrayInputStream("Hello".getBytes(StandardCharsets.UTF_8)),
        "foo",
        "bar.txt",
        MediaType.TEXT_PLAIN_VALUE);
    try {
      assertEquals("foo", file.getName());
    } finally {
      FileAwareMultipartFile.delete(file);
    }
  }

  /**
   * Gets original filename.
   *
   * @throws Exception the exception
   */
  @Test
  void getOriginalFilename() throws Exception {
    MultipartFile file = new FileAwareMultipartFile(
        new ByteArrayInputStream("Hello".getBytes(StandardCharsets.UTF_8)),
        System.getProperty("java.io.tmpdir"),
        "foo",
        "bar.txt",
        MediaType.TEXT_PLAIN_VALUE);
    try {
      assertEquals("bar.txt", file.getOriginalFilename());
    } finally {
      FileAwareMultipartFile.delete(file);
    }
  }

  /**
   * Gets content type.
   *
   * @throws Exception the exception
   */
  @Test
  void getContentType() throws Exception {
    Path file = Files
        .createTempFile(Path.of(System.getProperty("java.io.tmpdir")), "junit", ".txt");
    MultipartFile multipartFile = null;
    try {
      FileCopyUtils.copy(
          "Hello".getBytes(StandardCharsets.UTF_8),
          Files.newOutputStream(
              file,
              StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING));
      multipartFile = new FileAwareMultipartFile(
          file,
          "foo",
          "bar.txt",
          MediaType.TEXT_PLAIN_VALUE);
      assertEquals(MediaType.TEXT_PLAIN_VALUE, multipartFile.getContentType());

    } finally {
      Optional.ofNullable(multipartFile).ifPresent(FileAwareMultipartFile::delete);
    }
  }

  /**
   * Is empty.
   */
  @Test
  void isEmpty() {
    assertTrue(new FileAwareMultipartFile((File) null, null, null, null).isEmpty());
  }

  /**
   * Gets size.
   *
   * @throws Exception the exception
   */
  @Test
  void getSize() throws Exception {
    byte[] content = "Hello".getBytes(StandardCharsets.UTF_8);
    MultipartFile file = new FileAwareMultipartFile(
        new ByteArrayInputStream(content),
        "foo",
        "bar.txt",
        MediaType.TEXT_PLAIN_VALUE);
    try {
      assertEquals(content.length, (int) file.getSize());
    } finally {
      FileAwareMultipartFile.delete(file);
    }
  }

  /**
   * Gets bytes.
   *
   * @throws Exception the exception
   */
  @Test
  void getBytes() throws Exception {
    byte[] content = "Hello".getBytes(StandardCharsets.UTF_8);
    MultipartFile file = new FileAwareMultipartFile(
        new ByteArrayInputStream(content),
        "foo",
        "bar.txt",
        MediaType.TEXT_PLAIN_VALUE);
    try {
      assertArrayEquals(content, file.getBytes());
    } finally {
      FileAwareMultipartFile.delete(file);
    }
  }

  /**
   * Gets input stream.
   *
   * @throws Exception the exception
   */
  @Test
  void getInputStream() throws Exception {
    byte[] content = "Hello".getBytes(StandardCharsets.UTF_8);
    MultipartFile file = new FileAwareMultipartFile(
        new ByteArrayInputStream(content),
        "foo",
        "bar.txt",
        MediaType.TEXT_PLAIN_VALUE);
    try (InputStream in = file.getInputStream()) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      FileCopyUtils.copy(in, out);
      assertArrayEquals(content, out.toByteArray());

    } finally {
      FileAwareMultipartFile.delete(file);
    }
  }

  /**
   * Gets resource.
   *
   * @throws Exception the exception
   */
  @Test
  void getResource() throws Exception {
    byte[] content = "Hello".getBytes(StandardCharsets.UTF_8);
    MultipartFile file = new FileAwareMultipartFile(
        new ByteArrayInputStream(content),
        "foo",
        "bar.txt",
        MediaType.TEXT_PLAIN_VALUE);
    try {
      Resource resource = file.getResource();
      assertNotNull(resource);
      assertTrue(resource.isFile());

    } finally {
      FileAwareMultipartFile.delete(file);
    }
  }

  /**
   * Transfer to.
   *
   * @throws Exception the exception
   */
  @Test
  void transferTo() throws Exception {
    byte[] content = "Hello".getBytes(StandardCharsets.UTF_8);
    MultipartFile tmp = mock(MultipartFile.class);
    when(tmp.getInputStream()).thenReturn(new ByteArrayInputStream(content));
    when(tmp.getName()).thenReturn("foo");
    when(tmp.getOriginalFilename()).thenReturn("bar.txt");
    when(tmp.getContentType()).thenReturn(MediaType.TEXT_PLAIN_VALUE);
    when(tmp.getSize()).thenReturn((long) content.length);
    when(tmp.isEmpty()).thenReturn(false);
    MultipartFile file = new FileAwareMultipartFile(tmp);
    File destFile = null;
    try {
      destFile = File.createTempFile(
          "junit",
          ".txt",
          new File(System.getProperty("java.io.tmpdir")));
      file.transferTo(destFile);
      assertArrayEquals(content, FileCopyUtils.copyToByteArray(destFile));

    } finally {
      FileAwareMultipartFile.delete(file);
      if (destFile != null) {
        Files.delete(destFile.toPath());
      }
    }
  }

  /**
   * Equals and hash code.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings({"ConstantConditions", "SimplifiableJUnitAssertion", "EqualsWithItself"})
  @Test
  void equalsAndHashCode() throws Exception {
    MultipartFile file0 = new FileAwareMultipartFile(
        new ByteArrayInputStream("Hello".getBytes(StandardCharsets.UTF_8)),
        "foo",
        "bar.txt",
        MediaType.TEXT_PLAIN_VALUE);
    MultipartFile file1 = new FileAwareMultipartFile(file0);

    assertFalse(file0.equals(null));
    assertFalse(file0.equals(new Object()));
    assertTrue(file0.equals(file0));
    assertTrue(file0.equals(file1));
    assertEquals(file0.hashCode(), file1.hashCode());
    assertEquals(file0.toString(), file1.toString());
  }
}