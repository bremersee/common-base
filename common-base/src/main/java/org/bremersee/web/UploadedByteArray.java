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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * The uploaded byte array.
 *
 * @author Christian Bremer
 */
@Slf4j
public class UploadedByteArray implements UploadedItem<byte[]> {

  private final byte[] bytes;

  private final String contentType;

  private final String filename;

  /**
   * Instantiates a new uploaded byte array.
   *
   * @param bytes the bytes
   * @param contentType the content type
   * @param filename the file name
   */
  public UploadedByteArray(
      @Nullable byte[] bytes,
      @Nullable String contentType,
      @Nullable String filename) {

    this.bytes = bytes;
    this.contentType = contentType;
    this.filename = filename;
  }

  /**
   * Instantiates a new uploaded byte array.
   *
   * @param inputStream the input stream
   * @param contentType the content type
   * @param filename the file name
   */
  public UploadedByteArray(
      @Nullable InputStream inputStream,
      @Nullable String contentType,
      @Nullable String filename) {

    if (inputStream == null) {
      this.bytes = null;
    } else {
      if (inputStream instanceof ByteArrayInputStream) {
        this.bytes = ((ByteArrayInputStream) inputStream).readAllBytes();
      } else {
        try (InputStream in = inputStream) {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          int len;
          byte[] buf = new byte[4096];
          while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
          }
          out.flush();
          this.bytes = out.toByteArray();

        } catch (IOException e) {
          throw ServiceException
              .internalServerError("Reading input stream failed.", e);
        }
      }
    }
    this.contentType = contentType;
    this.filename = filename;
  }

  @Override
  public byte[] getItem() {
    return bytes;
  }

  @Override
  public InputStream getInputStream() {
    Assert.notNull(bytes, "Byte array must not be null.");
    return new ByteArrayInputStream(bytes);
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public String getFilename() {
    return filename;
  }

  @Override
  public long getLength() {
    if (bytes == null) {
      return 0L;
    }
    return bytes.length;
  }

  @Override
  public void delete() {
    // nothing to do
  }
}
