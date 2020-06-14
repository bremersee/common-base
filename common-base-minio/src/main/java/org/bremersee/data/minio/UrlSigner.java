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

import io.minio.http.Method;
import java.time.Duration;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

/**
 * The url signer.
 *
 * @author Christian Bremer
 */
@Validated
public interface UrlSigner extends Function<String, String> {

  @Override
  String apply(String objectName);

  /**
   * Created default url signer.
   *
   * @param minioOperations the minio operations
   * @param method the method
   * @param bucketName the bucket name
   * @param expires the expires
   * @param defaultUrl the default url
   * @return the url signer
   */
  static UrlSigner defaultSigner(
      @Nullable MinioOperations minioOperations,
      @NotNull Method method,
      @NotNull String bucketName,
      @Nullable Duration expires,
      @Nullable String defaultUrl) {
    return defaultSigner(minioOperations, method, bucketName, expires, null, defaultUrl);
  }

  /**
   * Created default url signer.
   *
   * @param minioOperations the minio operations
   * @param method the method
   * @param bucketName the bucket name
   * @param expires the expires
   * @param reqParams the req params
   * @param defaultUrl the default url
   * @return the presigned url provider
   */
  static UrlSigner defaultSigner(
      @Nullable MinioOperations minioOperations,
      @NotNull Method method,
      @NotNull String bucketName,
      @Nullable Duration expires,
      @Nullable Map<String, String> reqParams,
      @Nullable String defaultUrl) {

    return objectName -> {
      if (minioOperations == null || !StringUtils.hasText(objectName)) {
        return defaultUrl;
      }
      try {
        return minioOperations
            .getPresignedObjectUrl(method, bucketName, objectName, expires, reqParams);

      } catch (MinioException e) {
        if (e.status() == 404) {
          return defaultUrl;
        }
        throw e;
      }
    };
  }

}
