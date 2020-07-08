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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * The uploaded byte array test.
 *
 * @author Christian Bremer
 */
class UploadedByteArrayTest {

  private static final byte[] content = "UploadedByteArrayTest".getBytes(StandardCharsets.UTF_8);

  /**
   * Gets item.
   */
  @Test
  void getItem() {
    UploadedByteArray uploaded = new UploadedByteArray(content, null, null);
    assertEquals(content, uploaded.getItem());
  }

  /**
   * Gets input stream.
   *
   * @throws IOException the io exception
   */
  @Test
  void getInputStream() throws IOException {
    UploadedByteArray uploaded = new UploadedByteArray(
        new ByteArrayInputStream(content), null, null);
    try (InputStream in = uploaded.getInputStream()) {
      byte[] actual = in.readAllBytes();
      assertArrayEquals(content, actual);
    }
  }

  /**
   * Gets content type.
   */
  @Test
  void getContentType() {
    UploadedByteArray uploaded = new UploadedByteArray(content, "text/plain", null);
    assertEquals("text/plain", uploaded.getContentType());
  }

  /**
   * Gets filename.
   */
  @Test
  void getFilename() {
    UploadedByteArray uploaded = new UploadedByteArray(content, null, "a.txt");
    assertEquals("a.txt", uploaded.getFilename());
  }

  /**
   * Gets length.
   */
  @Test
  void getLength() {
    UploadedByteArray uploaded = new UploadedByteArray(content, null, null);
    assertEquals(content.length, (int) uploaded.getLength());
  }

  /**
   * Delete.
   */
  @Test
  void delete() {
    UploadedByteArray uploaded = new UploadedByteArray(content, null, null);
    uploaded.delete();
  }

  /**
   * Equals and to string.
   */
  @Test
  void equalsAndToString() {
    UploadedByteArray a = new UploadedByteArray(content, "text/plain", "a.txt");
    UploadedByteArray b = new UploadedByteArray(content, "text/plain", "a.txt");
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertEquals(a.toString(), b.toString());
  }
}