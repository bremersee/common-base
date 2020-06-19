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

import java.io.IOException;
import java.io.InputStream;
import javax.validation.constraints.NotNull;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

/**
 * The uploaded item.
 *
 * @param <S> the item type (can be {@link java.nio.file.Path} or {@code byte[]})
 * @author Christian Bremer
 */
@Validated
public interface UploadedItem<S> {

  /**
   * The constant EMPTY.
   */
  UploadedItem<?> EMPTY = new UploadedByteArray((byte[]) null, null, null);

  /**
   * Gets item.
   *
   * @return the item
   */
  @Nullable
  S getItem();

  /**
   * Gets input stream.
   *
   * @return the input stream
   * @throws IOException the io exception
   */
  @NotNull
  InputStream getInputStream() throws IOException;

  /**
   * Checks whether this item is empty.
   *
   * @return {@code true} if it is empty, otherwise {@code false}
   */
  default boolean isEmpty() {
    return getLength() == 0;
  }

  /**
   * Gets content type.
   *
   * @return the content type
   */
  @Nullable
  String getContentType();

  /**
   * Gets file name.
   *
   * @return the file name
   */
  @Nullable
  String getFilename();

  /**
   * Gets length of this item.
   *
   * @return the length
   */
  long getLength();

  /**
   * Delete this item.
   */
  void delete();

  /**
   * The delete mode.
   */
  enum DeleteMode {

    /**
     * Never delete mode.
     */
    NEVER,

    /**
     * On success delete mode.
     */
    ON_SUCCESS,

    /**
     * Always delete mode.
     */
    ALWAYS
  }

}
