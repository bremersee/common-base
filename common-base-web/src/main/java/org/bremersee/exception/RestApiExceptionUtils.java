/*
 * Copyright 2019 the original author or authors.
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

package org.bremersee.exception;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

/**
 * Some utilities.
 *
 * @author Christian Bremer
 */
@Validated
@Slf4j
public abstract class RestApiExceptionUtils {

  /**
   * The header name for the 'id' attribute.
   */
  public static final String ID_HEADER_NAME = "X-ERROR-ID";

  /**
   * The header name for the 'timestamp' attribute.
   */
  public static final String TIMESTAMP_HEADER_NAME = "X-ERROR-TIMESTAMP";

  /**
   * The formatter of the timestamp value.
   */
  public static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME;

  /**
   * The header name for the 'message' attribute.
   */
  public static final String MESSAGE_HEADER_NAME = "X-ERROR-MESSAGE";

  /**
   * The header name for the 'code' attribute.
   */
  public static final String CODE_HEADER_NAME = "X-ERROR-CODE";

  /**
   * The header name for the 'class name' attribute.
   */
  public static final String CLASS_HEADER_NAME = "X-ERROR-CLASS-NAME";

  /**
   * The default value of the 'id' attribute.
   */
  public static final String NO_ID_VALUE = "UNSPECIFIED";

  /**
   * The default value of the 'code' attribute.
   */
  public static final String NO_ERROR_CODE_VALUE = ErrorCodeAware.NO_ERROR_CODE_VALUE;

  /**
   * The default value of the 'message' attribute.
   */
  public static final String NO_MESSAGE_VALUE = "No message present.";

  /**
   * The default value of the 'class name attribute.
   */
  public static final String NO_CLASS_VALUE = "UNSPECIFIED";

  private RestApiExceptionUtils() {
  }

  /**
   * Parse the 'timestamp' header value.
   *
   * @param value the 'timestamp' header value
   * @return the timestamp
   */
  @SuppressWarnings("WeakerAccess")
  @Nullable
  public static OffsetDateTime parseHeaderValue(@Nullable String value) {
    OffsetDateTime time = null;
    if (StringUtils.hasText(value)) {
      try {
        time = OffsetDateTime.parse(value, TIMESTAMP_FORMATTER);
      } catch (final Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("msg=[Parsing timestamp failed.] timestamp=[{}]", value);
        }
      }
    }
    return time;
  }

}
