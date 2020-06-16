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

package org.bremersee.data.minio.http;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * The multipart names.
 *
 * @author Christian Bremer
 */
@Getter
@ToString
@EqualsAndHashCode
@SuppressWarnings("FieldMayBeFinal")
public class MultipartNames {

  private String contentPart;

  private String contentTypePart;

  private String filenamePart;

  private boolean required;

  /**
   * Instantiates new multipart names.
   *
   * @param contentPart the content part
   * @param contentTypePart the content type part
   * @param filenamePart the filename part
   */
  public MultipartNames(
      String contentPart,
      String contentTypePart,
      String filenamePart) {

    this(contentPart, contentTypePart, filenamePart, false);
  }

  /**
   * Instantiates new multipart names.
   *
   * @param contentPart the content part
   * @param contentTypePart the content type part
   * @param filenamePart the filename part
   * @param required the required
   */
  @Builder(toBuilder = true)
  public MultipartNames(
      String contentPart,
      String contentTypePart,
      String filenamePart,
      boolean required) {

    this.contentPart = contentPart;
    this.contentTypePart = contentTypePart;
    this.filenamePart = filenamePart;
    this.required = required;
  }
}
