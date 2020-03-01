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

import java.io.Serializable;

/**
 * The service exception builder.
 *
 * @param <T> the type parameter
 * @author Christian Bremer
 */
public interface ServiceExceptionBuilder<T extends ServiceException> extends Serializable {

  /**
   * Sets http status on service exception builder.
   *
   * @param httpStatus the http status
   * @return the service exception builder
   */
  ServiceExceptionBuilder<T> httpStatus(int httpStatus);

  /**
   * Sets error code on service exception builder.
   *
   * @param errorCode the error code
   * @return the service exception builder
   */
  ServiceExceptionBuilder<T> errorCode(String errorCode);

  /**
   * Sets reason on service exception builder.
   *
   * @param reason the reason
   * @return the service exception builder
   */
  ServiceExceptionBuilder<T> reason(String reason);

  /**
   * sets cause on service exception builder.
   *
   * @param cause the cause
   * @return the service exception builder
   */
  ServiceExceptionBuilder<T> cause(Throwable cause);

  /**
   * Build the service exception.
   *
   * @return the service exception
   */
  T build();

}
