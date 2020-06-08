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

import lombok.EqualsAndHashCode;
import org.bremersee.exception.ErrorCodeAware;
import org.bremersee.exception.HttpStatusAware;
import org.bremersee.exception.ServiceException;

/**
 * The minio exception.
 *
 * @author Christian Bremer
 */
@EqualsAndHashCode(callSuper = true)
public class MinioException extends ServiceException implements HttpStatusAware, ErrorCodeAware {
  
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new minio exception.
   */
  protected MinioException() {
    super();
  }

  /**
   * Instantiates a new minio exception.
   *
   * @param httpStatus the http status
   * @param errorCode the error code
   */
  protected MinioException(final int httpStatus, final String errorCode) {
    super(httpStatus, errorCode);
  }

  /**
   * Instantiates a new minio exception.
   *
   * @param httpStatus the http status
   * @param errorCode the error code
   * @param reason the reason
   */
  protected MinioException(final int httpStatus, final String errorCode, final String reason) {
    super(httpStatus, errorCode, reason);
  }

  /**
   * Instantiates a new minio exception.
   *
   * @param httpStatus the http status
   * @param errorCode the error code
   * @param cause the cause
   */
  protected MinioException(final int httpStatus, final String errorCode, final Throwable cause) {
    super(httpStatus, errorCode, cause);
  }

  /**
   * Instantiates a new minio exception.
   *
   * @param httpStatus the http status
   * @param errorCode the error code
   * @param reason the reason
   * @param cause the cause
   */
  protected MinioException(
      final int httpStatus,
      final String errorCode,
      final String reason,
      final Throwable cause) {
    super(httpStatus, errorCode, reason, cause);
  }

}
