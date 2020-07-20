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

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

/**
 * The abstract minio error handler.
 *
 * @author Christian Bremer
 */
@Slf4j
public abstract class AbstractMinioErrorHandler implements MinioErrorHandler {

  @Override
  public void handleError(@NonNull Throwable t) {
    MinioException minioException = map(t);
    log.error("Minio operation failed.", minioException);
    throw minioException;
  }
}
