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

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import io.minio.PutObjectOptions;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;

/**
 * The file system put object.
 *
 * @author Christian Bremer
 */
@Slf4j
public class FileSystemPutObject implements PutObject<Path> {

  private final Path object;

  private final String contentType;

  private final String name;

  private FileSystemPutObject() {
    this.object = null;
    this.contentType = null;
    this.name = null;
  }

  /**
   * Instantiates a new file system put object.
   *
   * @param file the object file
   * @param contentType the content type
   * @param name the name
   */
  public FileSystemPutObject(@Nullable File file, @Nullable String contentType, @Nullable String name) {
    this(file != null ? file.toPath() : null, contentType, name);
  }

  /**
   * Instantiates a new file system put object.
   *
   * @param path the object path
   * @param contentType the content type
   * @param name the name
   */
  public FileSystemPutObject(@Nullable Path path, @Nullable String contentType, @Nullable String name) {
    if (path == null) {
      this.object = null;
    } else if (Files.isReadable(path) && Files.isRegularFile(path)) {
      this.object = path;
    } else {
      throw ServiceException.internalServerError("Put object " + path + " is not readable.");
    }
    this.contentType = contentType;
    this.name = name;
  }

  /**
   * Instantiates a new file system put object.
   *
   * @param inputStream the object input stream
   * @param contentType the content type
   * @param name the name
   */
  public FileSystemPutObject(
      @Nullable InputStream inputStream,
      @Nullable String contentType,
      @Nullable String name) {
    this(inputStream, null, contentType, name);
  }

  /**
   * Instantiates a new file system put object.
   *
   * @param inputStream the object input stream
   * @param directory the directory
   * @param contentType the content type
   * @param name the name
   */
  public FileSystemPutObject(
      @Nullable InputStream inputStream,
      @Nullable Path directory,
      @Nullable String contentType,
      @Nullable String name) {

    Path path = null;
    if (inputStream != null) {
      try (InputStream in = inputStream) {
        if (directory == null) {
          path = createTempFile(get(System.getProperty("java.io.tmpdir")), "put", "obj");
        } else {
          path = createTempFile(createDirectories(directory), "put", "obj");
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
            .internalServerError("Creating tmp file of uploaded object failed.", e);
      }
    }
    this.object = path;
    this.contentType = contentType;
    this.name = name;
  }

  @Override
  public Path getObject() {
    return object;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public long getLength() {
    return object != null ? object.toFile().length() : 0;
  }

  @Override
  public void delete() {
    if (object != null) {
      File file = object.toFile();
      if (file.exists() && !file.delete()) {
        log.warn("Path object {} was not deleted.", object);
      }
    }
  }

  @Override
  public boolean upload(
      MinioOperations minioOperations,
      String bucketName,
      String name,
      boolean deleteObject) {

    if (minioOperations != null && getObject() != null) {
      try {
        PutObjectOptions options = new PutObjectOptions(getLength(), -1);
        options.setContentType(getContentType());
        minioOperations.putObject(bucketName, name, getObject(), options);
        return true;

      } finally {
        if (deleteObject) {
          delete();
        }
      }
    } else {
      return false;
    }
  }

  public static FileSystemPutObject empty() {
    return new FileSystemPutObject();
  }

}
