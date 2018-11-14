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

package org.bremersee.exception;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.validation.constraints.NotNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * @author Christian Bremer
 */
public abstract class ExceptionConstants {

  public static final String ID_HEADER_NAME = "X-ERROR-ID";

  public static final String TIMESTAMP_HEADER_NAME = "X-ERROR-TIMESTAMP";

  public static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME;

  public static final String MESSAGE_HEADER_NAME = "X-ERROR-MESSAGE";

  public static final String CODE_HEADER_NAME = "X-ERROR-CODE";

  public static final String CLASS_HEADER_NAME = "X-ERROR-CLASS-NAME";

  public static final String NO_MESSAGE_VALUE = "No message present.";

  public static final String NO_ERROR_CODE_VALUE = "UNSPECIFIED";

  public static final String NO_ID_VALUE = "UNSPECIFIED";

  private ExceptionConstants() {
  }

  @NotNull
  public static OffsetDateTime parseHeaderValue(@Nullable String value) {
    OffsetDateTime time = null;
    if (StringUtils.hasText(value)) {
      try {
        time = OffsetDateTime.parse(value, TIMESTAMP_FORMATTER);
      } catch (Exception e) {
        time = null;
      }
    }
    return time != null ? time : OffsetDateTime.now(ZoneId.of("UTC"));
  }

}
