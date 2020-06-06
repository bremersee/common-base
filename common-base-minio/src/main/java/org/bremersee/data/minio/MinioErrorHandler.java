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

import javax.validation.constraints.NotNull;
import org.springframework.util.ErrorHandler;
import org.springframework.validation.annotation.Validated;

/**
 * The inio error handler.
 *
 * @author Christian Bremer
 */
@Validated
public interface MinioErrorHandler extends ErrorHandler {

  /**
   * Map minio exception.
   *
   * @param t the t
   * @return the minio exception
   */
  @NotNull
  MinioException map(@NotNull Throwable t);

}