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

import eu.maxschuster.dataurl.DataUrl;
import eu.maxschuster.dataurl.DataUrlSerializer;
import io.minio.PutObjectOptions;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import javax.annotation.Nullable;
import org.bremersee.exception.ServiceException;
import org.springframework.util.StringUtils;

/**
 * The in memory put object.
 *
 * @author Christian Bremer
 */
public class InMemoryPutObject implements PutObject<byte[]> {

  private final byte[] object;

  private final String contentType;

  private final String name;

  private InMemoryPutObject() {
    this.object = null;
    this.contentType = null;
    this.name = null;
  }

  /**
   * Instantiates a new in memory put object.
   *
   * @param object the object
   * @param contentType the content type
   * @param name the name
   */
  public InMemoryPutObject(
      @Nullable byte[] object,
      @Nullable String contentType,
      @Nullable String name) {

    this.object = object;
    this.contentType = contentType;
    this.name = name;
  }

  /**
   * Instantiates a new in memory put object.
   *
   * @param dataUri the RFC 2397 data uri
   * @param name the name
   */
  public InMemoryPutObject(
      @Nullable String dataUri,
      @Nullable String name) {

    if (StringUtils.hasText(dataUri)) {
      try {
        DataUrl data = new DataUrlSerializer().unserialize(dataUri);
        this.object = data.getData();
        this.contentType = data.getMimeType();

      } catch (IllegalArgumentException | MalformedURLException e) {
        throw ServiceException.badRequest("Parsing data uri failed.", e);
      }
    } else {
      this.object = null;
      this.contentType = null;
    }
    this.name = name;
  }

  /**
   * Instantiates a new in memory put object.
   *
   * @param object the object
   * @param contentType the content type
   * @param name the name
   */
  public InMemoryPutObject(
      @Nullable InputStream object,
      @Nullable String contentType,
      @Nullable String name) {

    if (object == null) {
      this.object = null;
    } else {
      if (object instanceof ByteArrayInputStream) {
        this.object = ((ByteArrayInputStream) object).readAllBytes();
      } else {
        try (InputStream in = object) {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          int len;
          byte[] buf = new byte[4096];
          while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
          }
          out.flush();
          this.object = out.toByteArray();

        } catch (IOException e) {
          throw ServiceException
              .internalServerError("Reading input stream failed.", e);
        }
      }
    }
    this.contentType = contentType;
    this.name = name;
  }

  @Override
  public byte[] getObject() {
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
    return object != null ? object.length : 0;
  }

  @Override
  public void delete() {
    // nothing to do
  }

  @Override
  public boolean upload(
      MinioOperations minioOperations,
      String bucketName,
      String name,
      boolean deleteObject) {

    if (minioOperations != null && getObject() != null && getObject().length > 0) {
      try {
        PutObjectOptions options = new PutObjectOptions(getLength(), -1);
        options.setContentType(getContentType());
        minioOperations.putObject(bucketName, name, new ByteArrayInputStream(getObject()), options);
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

  /**
   * Empty in memory put object.
   *
   * @return the in memory put object
   */
  public static InMemoryPutObject empty() {
    return new InMemoryPutObject();
  }

}
