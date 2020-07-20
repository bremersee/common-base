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

import io.minio.MinioClient;
import org.springframework.util.ErrorHandler;

/**
 * The minio template.
 *
 * @author Christian Bremer
 */
public class MinioTemplate implements MinioOperations, Cloneable {

  private final MinioClient client;

  private ErrorHandler errorHandler = new DefaultMinioErrorHandler();

  /**
   * Instantiates a new minio template.
   *
   * @param client the client
   */
  public MinioTemplate(MinioClient client) {
    this.client = client;
  }

  /**
   * Sets error handler.
   *
   * @param errorHandler the error handler
   */
  public void setErrorHandler(ErrorHandler errorHandler) {
    if (errorHandler != null) {
      this.errorHandler = errorHandler;
    }
  }

  @SuppressWarnings("MethodDoesntCallSuperMethod")
  @Override
  public MinioTemplate clone() {
    return clone(null);
  }

  /**
   * Clone minio template.
   *
   * @param errorHandler the error handler
   * @return the minio template
   */
  public MinioTemplate clone(ErrorHandler errorHandler) {
    MinioTemplate clone = new MinioTemplate(client);
    clone.setErrorHandler(errorHandler);
    return clone;
  }

  @Override
  public <T> T execute(MinioClientCallback<T> callback) {
    try {
      return callback.doWithMinioClient(client);

    } catch (Exception e) {
      errorHandler.handleError(e);
      return null;
    }
  }

}
