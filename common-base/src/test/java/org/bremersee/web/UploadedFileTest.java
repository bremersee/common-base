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

package org.bremersee.web;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.bremersee.exception.ServiceException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * The uploaded file test.
 *
 * @author Christian Bremer
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UploadedFileTest {

  private static final byte[] content = "UploadedFileTest".getBytes(StandardCharsets.UTF_8);

  private static File source;

  /**
   * Create source.
   *
   * @throws IOException the io exception
   */
  @BeforeAll
  static void createSource() throws IOException {
    source = File.createTempFile("test", ".tmp", new File(System.getProperty("java.io.tmpdir")));
    source.deleteOnExit();
    try (OutputStream out = new FileOutputStream(source)) {
      out.write(content);
      out.flush();
    }
  }

  /**
   * Delete source.
   */
  @AfterAll
  static void deleteSource() {
    if (source != null && source.exists() && !source.delete()) {
      System.out.println("Could not delete test file '" + source.getAbsolutePath() + "'.");
    }
  }

  /**
   * Delete.
   */
  @Order(10)
  @Test
  void delete() {
    UploadedFile uploaded = new UploadedFile(content, "text/plain", source.getName());
    uploaded.delete();
    Path deleted = uploaded.getItem();
    assertNotNull(deleted);
    assertFalse(Files.exists(deleted));
  }

  /**
   * Gets item.
   */
  @Order(20)
  @Test
  void getItem() {
    UploadedFile uploaded = new UploadedFile(source, "text/plain", source.getName());
    assertNotNull(uploaded.getItem());
    assertEquals(source.getAbsolutePath(), uploaded.getItem().toFile().getAbsolutePath());
  }

  /**
   * Gets input stream.
   *
   * @throws IOException the io exception
   */
  @Order(30)
  @Test
  void getInputStream() throws IOException {
    UploadedFile uploaded = null;
    try {
      uploaded = new UploadedFile(content, "text/plain", source.getName());
      try (InputStream in = uploaded.getInputStream()) {
        byte[] actual = in.readAllBytes();
        assertArrayEquals(content, actual);
      }

    } finally {
      if (uploaded != null) {
        uploaded.delete();
      }
    }
    try {
      uploaded = new UploadedFile(new ByteArrayInputStream(content), "text/plain",
          source.getName());
      try (InputStream in = uploaded.getInputStream()) {
        byte[] actual = in.readAllBytes();
        assertArrayEquals(content, actual);
      }

    } finally {
      uploaded.delete();
    }
  }

  /**
   * Gets content type.
   */
  @Order(40)
  @Test
  void getContentType() {
    UploadedFile uploaded = new UploadedFile(source, "text/plain", source.getName());
    assertEquals("text/plain", uploaded.getContentType());
  }

  /**
   * Gets filename.
   */
  @Order(50)
  @Test
  void getFilename() {
    UploadedFile uploaded = new UploadedFile(source, "text/plain", source.getName());
    assertEquals(source.getName(), uploaded.getFilename());
  }

  /**
   * Gets length.
   */
  @Order(60)
  @Test
  void getLength() {
    UploadedFile uploaded = new UploadedFile(content, "text/plain", source.getName(),
        Paths.get(System.getProperty("java.io.tmpdir")));
    assertEquals(source.length(), uploaded.getLength());
  }

  /**
   * Equals and to string.
   */
  @Order(70)
  @Test
  void equalsAndToString() {
    UploadedFile a = new UploadedFile(source, "text/plain", source.getName());
    UploadedFile b = new UploadedFile(source, "text/plain", source.getName());
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertEquals(a.toString(), b.toString());
  }

  /**
   * With no file.
   */
  @Order(80)
  @Test
  void withNoFile() {
    UploadedFile uploaded = new UploadedFile((File) null, null, null);
    assertNull(uploaded.getItem());
    assertNull(uploaded.getFilename());
    assertNull(uploaded.getContentType());
    assertEquals(0L, uploaded.getLength());
    assertThrows(IllegalArgumentException.class, uploaded::getInputStream);
  }

  /**
   * With no content.
   */
  @Order(81)
  @Test
  void withNoContent() {
    UploadedFile uploaded = new UploadedFile((byte[]) null, null, null);
    assertNull(uploaded.getItem());
    assertNull(uploaded.getFilename());
    assertNull(uploaded.getContentType());
    assertEquals(0L, uploaded.getLength());
    assertThrows(IllegalArgumentException.class, uploaded::getInputStream);
  }

  /**
   * Illegal uploaded file.
   */
  @Order(90)
  @Test
  void illegalUploadedFile() {
    assertThrows(
        ServiceException.class,
        () -> new UploadedFile(new File(UUID.randomUUID().toString()), null, null));
  }
}