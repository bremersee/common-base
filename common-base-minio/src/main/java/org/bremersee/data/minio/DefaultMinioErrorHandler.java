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

import io.minio.errors.ErrorResponseException;

/**
 * The default minio error handler.
 *
 * @author Christian Bremer
 */
public class DefaultMinioErrorHandler extends AbstractMinioErrorHandler {

  @Override
  public MinioException map(Throwable t) {
    // TODO not found etc.
    if (t instanceof IllegalArgumentException) {
      return new MinioException(400, t.getMessage(), t);
    }
    if (t instanceof ErrorResponseException) {
      ErrorResponseException ere = (ErrorResponseException)t;

    }
    return new MinioException(500, "Unmapped minio error", t);
  }
}
