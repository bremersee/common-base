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

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * The uploaded file.
 *
 * @author Christian Bremer
 */
@Slf4j
public class UploadedFile implements UploadedItem<Path> {

  private final Path file;

  private final String contentType;

  private final String filename;

  /**
   * Instantiates a new uploaded file.
   *
   * @param file the file
   * @param contentType the content type
   * @param filename the file name
   */
  public UploadedFile(
      @Nullable File file,
      @Nullable String contentType,
      @Nullable String filename) {

    this(file != null ? file.toPath() : null, contentType, filename);
  }

  /**
   * Instantiates a new uploaded file.
   *
   * @param path the path
   * @param contentType the content type
   * @param filename the file name
   */
  public UploadedFile(
      @Nullable Path path,
      @Nullable String contentType,
      @Nullable String filename) {

    if (path == null) {
      this.file = null;
    } else if (Files.isReadable(path) && Files.isRegularFile(path)) {
      this.file = path;
    } else {
      throw ServiceException.internalServerError("Uploaded file " + path + " is not readable.");
    }
    this.contentType = contentType;
    this.filename = filename;
  }

  /**
   * Instantiates a new uploaded file.
   *
   * @param content the content
   * @param contentType the content type
   * @param filename the file name
   */
  public UploadedFile(
      @Nullable byte[] content,
      @Nullable String contentType,
      @Nullable String filename) {
    this(content, contentType, filename, null);
  }

  /**
   * Instantiates a new uploaded file.
   *
   * @param content the content
   * @param contentType the content type
   * @param filename the file name
   * @param directory the directory
   */
  public UploadedFile(
      @Nullable byte[] content,
      @Nullable String contentType,
      @Nullable String filename,
      @Nullable Path directory) {
    this(content != null ? new ByteArrayInputStream(content) : null, contentType, filename,
        directory);
  }

  /**
   * Instantiates a new uploaded file.
   *
   * @param inputStream the input stream
   * @param contentType the content type
   * @param filename the file name
   */
  public UploadedFile(
      @Nullable InputStream inputStream,
      @Nullable String contentType,
      @Nullable String filename) {
    this(inputStream, contentType, filename, null);
  }

  /**
   * Instantiates a new uploaded file.
   *
   * @param inputStream the object input stream
   * @param contentType the content type
   * @param filename the file name
   * @param directory the directory
   */
  public UploadedFile(
      @Nullable InputStream inputStream,
      @Nullable String contentType,
      @Nullable String filename,
      @Nullable Path directory) {

    Path path = null;
    if (inputStream != null) {
      try (InputStream in = inputStream) {
        if (directory == null) {
          path = createTempFile(get(System.getProperty("java.io.tmpdir")), "upload-", ".tmp");
        } else {
          path = createTempFile(createDirectories(directory), "upload-", ".tmp");
        }
        try (OutputStream out = newOutputStream(path, CREATE, TRUNCATE_EXISTING, WRITE)) {
          int len;
          byte[] buf = new byte[4096];
          while ((len = in.read(buf)) > -1) {
            out.write(buf, 0, len);
          }
          out.flush();
        }

      } catch (Exception e) {
        if (path != null && !path.toFile().delete()) {
          log.warn("Uploaded tmp file [{}] was not deleted.", path);
        }
        if (e instanceof RuntimeException) {
          throw (RuntimeException) e;
        }
        throw ServiceException
            .internalServerError("Creating tmp file of uploaded item failed.", e);
      }
    }
    this.file = path;
    this.contentType = contentType;
    this.filename = filename;
  }

  @Override
  public Path getItem() {
    return file;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    Assert.notNull(file, "File must not be null.");
    return Files.newInputStream(file, StandardOpenOption.READ);
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
    if (file == null) {
      return 0L;
    }
    return file.toFile().length();
  }

  @Override
  public void delete() {
    if (file != null) {
      File f = this.file.toFile();
      if (f.exists() && !f.delete()) {
        log.warn("File {} was not deleted.", this.file);
      }
    }
  }
}
