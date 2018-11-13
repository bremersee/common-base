/*
 * Copyright 2017 the original author or authors.
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

package org.bremersee.http;

import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

/**
 * The content type helper.
 *
 * @author Christian Bremer
 */
@SuppressWarnings("WeakerAccess")
@Validated
public abstract class MediaTypeHelper {

  private MediaTypeHelper() {
  }

  /**
   * Determine whether the given media type can be rendered as json.
   *
   * @param mediaType the media type
   * @return the boolean
   */
  public static boolean canContentTypeBeJson(@Nullable final String mediaType) {
    return isJson(mediaType) || isText(mediaType) || isAll(mediaType);
  }

  /**
   * Determine whether the given media type can be rendered as xml.
   *
   * @param mediaType the media type
   * @return the boolean
   */
  public static boolean canContentTypeBeXml(@Nullable final String mediaType) {
    return isXml(mediaType) || isText(mediaType) || isAll(mediaType);
  }

  /**
   * Determine whether the given content type is json.
   *
   * @param mediaType the media type
   * @return the boolean
   */
  public static boolean isJson(@Nullable final String mediaType) {
    return mediaType != null
        && (mediaType.toLowerCase().contains("/json")
        || mediaType.toLowerCase().contains("+json"));
  }

  /**
   * Determine whether the given content type is text.
   *
   * @param mediaType the media type
   * @return the boolean
   */
  public static boolean isText(@Nullable final String mediaType) {
    return mediaType != null
        && mediaType.toLowerCase().contains("text/");
  }

  /**
   * Determine whether the given content type is all.
   *
   * @param mediaType the media type
   * @return the boolean
   */
  public static boolean isAll(@Nullable final String mediaType) {
    return mediaType != null
        && mediaType.toLowerCase().contains("*/*");
  }

  /**
   * Determine whether the given content type is xml.
   *
   * @param mediaType the media type
   * @return the boolean
   */
  public static boolean isXml(@Nullable final String mediaType) {
    return mediaType != null
        && (mediaType.toLowerCase().contains("/xml")
        || mediaType.toLowerCase().contains("+xml"));
  }

  /**
   * Creates a header value of the given media types.
   *
   * @param mediaTypes the media types
   * @return the header value
   */
  @Nullable
  public static String toString(@Nullable final List<MediaType> mediaTypes) {
    return mediaTypes == null || mediaTypes.isEmpty() ? null : MediaType.toString(mediaTypes);
  }

}
