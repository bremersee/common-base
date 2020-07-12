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

import io.minio.ErrorCode;
import io.minio.errors.BucketPolicyTooLargeException;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidExpiresRangeException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.RegionConflictException;
import io.minio.errors.XmlParserException;
import io.minio.messages.ErrorResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.util.StringUtils;

/**
 * The default minio error handler.
 *
 * @author Christian Bremer
 */
public class DefaultMinioErrorHandler extends AbstractMinioErrorHandler {

  static final String ERROR_CODE_PREFIX = "MINIO_";

  @Override
  public MinioException map(Throwable t) {
    if (t instanceof IllegalArgumentException) {
      return new MinioException(
          400,
          ERROR_CODE_PREFIX + "BAD_REQUEST",
          StringUtils.hasText(t.getMessage()) ? t.getMessage() : "Bad request.",
          t);
    }
    if (t instanceof IOException) {
      return new MinioException(
          500,
          ERROR_CODE_PREFIX + "IO_ERROR",
          StringUtils.hasText(t.getMessage()) ? t.getMessage() : "IO operation failed.",
          t);
    }
    if (t instanceof io.minio.errors.MinioException) {
      return mapMinioException((io.minio.errors.MinioException) t);
    }
    return new MinioException(
        500,
        ERROR_CODE_PREFIX + "UNSPECIFIED",
        StringUtils.hasText(t.getMessage()) ? t.getMessage() : "Unmapped minio error",
        t);
  }

  private MinioException mapMinioException(io.minio.errors.MinioException e) {
    int status = 500;
    String errorCode = ERROR_CODE_PREFIX + "UNSPECIFIED";
    String message = StringUtils.hasText(e.getMessage())
        ? e.getMessage()
        : "Unspecified minio error.";

    if (e instanceof BucketPolicyTooLargeException) {
      status = 400;
      errorCode = ERROR_CODE_PREFIX + "BUCKET_POLICY_TOO_LARGE";
      message = e.toString();
    } else if (e instanceof ErrorResponseException) {
      ErrorResponseException ere = (ErrorResponseException) e;
      status = getStatus(ere);
      errorCode = getErrorCode(ere);
      message = getMessage(ere);
    } else if (e instanceof InsufficientDataException) {
      status = 400;
      errorCode = ERROR_CODE_PREFIX + "INSUFFICIENT_DATA";
    } else if (e instanceof InternalException) {
      errorCode = ERROR_CODE_PREFIX + "INTERNAL_ERROR";
    } else if (e instanceof InvalidBucketNameException) {
      status = 400;
      errorCode = ERROR_CODE_PREFIX + "INVALID_BUCKET_NAME";
    } else if (e instanceof InvalidEndpointException) {
      errorCode = ERROR_CODE_PREFIX + "INVALID_ENDPOINT";
    } else if (e instanceof InvalidExpiresRangeException) {
      status = 400;
      errorCode = ERROR_CODE_PREFIX + "INVALID_EXPIRES_RANGE";
    } else if (e instanceof InvalidPortException) {
      errorCode = ERROR_CODE_PREFIX + "INVALID_PORT";
    } else if (e instanceof InvalidResponseException) {
      errorCode = ERROR_CODE_PREFIX + "INVALID_RESPONSE";
    } else if (e instanceof RegionConflictException) {
      status = 400;
      errorCode = ERROR_CODE_PREFIX + "REGION_CONFLICT";
    } else if (e instanceof XmlParserException) {
      errorCode = ERROR_CODE_PREFIX + "XML_PARSER_ERROR";
    }
    return new MinioException(status, errorCode, message, e);
  }

  private int getStatus(ErrorResponseException e) {
    return Optional.of(e)
        .map(ErrorResponseException::errorResponse)
        .map(ErrorResponse::errorCode)
        .map(this::getStatus)
        .orElse(500);
  }

  private int getStatus(ErrorCode errorCode) {
    switch (errorCode) {
      case NO_SUCH_OBJECT:
      case RESOURCE_NOT_FOUND:
      case NO_SUCH_BUCKET:
      case NO_SUCH_KEY:
      case NO_SUCH_UPLOAD:
      case NO_SUCH_VERSION:
      case NO_SUCH_LIFECYCLE_CONFIGURATION:
      case NO_SUCH_BUCKET_POLICY:
      case NO_SUCH_OBJECT_LOCK_CONFIGURATION:
        return 404;
      case RESOURCE_CONFLICT:
      case BUCKET_ALREADY_EXISTS:
      case BUCKET_ALREADY_OWNED_BY_YOU:
      case BUCKET_NOT_EMPTY:
        return 409;
      case INTERNAL_ERROR:
      case NOT_IMPLEMENTED:
      case OPERATION_ABORTED:
      case RESTORE_ALREADY_IN_PROGRESS:
      case SERVICE_UNAVAILABLE:
      case SLOW_DOWN:
        return 500;
      default:
        return 400;
    }
  }

  private String getErrorCode(ErrorResponseException e) {
    return Optional.of(e)
        .map(ErrorResponseException::errorResponse)
        .map(ErrorResponse::errorCode)
        .map(ErrorCode::name)
        .orElse(ERROR_CODE_PREFIX + "UNSPECIFIED_ERROR_RESPONSE");
  }

  private String getMessage(ErrorResponseException e) {
    return Optional.of(e)
        .map(ErrorResponseException::errorResponse)
        .map(ErrorResponse::errorCode)
        .map(ErrorCode::message)
        .orElseGet(() -> StringUtils.hasText(e.getMessage())
            ? e.getMessage()
            : "Unspecified error response.");
  }

}
