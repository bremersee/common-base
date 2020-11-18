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

import io.minio.errors.BucketPolicyTooLargeException;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.ErrorResponse;
import java.io.IOException;
import java.util.Optional;
import okhttp3.Response;
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
        StringUtils.hasText(t.getMessage()) ? t.getMessage() : "Unmapped minio error.",
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

    } else if (e instanceof InvalidResponseException) {
      errorCode = ERROR_CODE_PREFIX + "INVALID_RESPONSE";

    } else if (e instanceof ServerException) {
      errorCode = ERROR_CODE_PREFIX + "SERVER_EXCEPTION";

    } else if (e instanceof XmlParserException) {
      errorCode = ERROR_CODE_PREFIX + "XML_PARSER_ERROR";
    }
    return new MinioException(status, errorCode, message, e);
  }

  private int getStatus(ErrorResponseException e) {
    return Optional.of(e)
        .map(ErrorResponseException::errorResponse)
        .map(ErrorResponse::code)
        .map(code -> getStatus(code, e.response()))
        .orElse(500);
  }

  private int getStatus(String errorCode, Response response) {
    // see https://docs.aws.amazon.com/AmazonS3/latest/API/ErrorResponses.html#ErrorCodeList
    switch (errorCode) {
      case "AmbiguousGrantByEmailAddress":
      case "AuthorizationHeaderMalformed":
      case "BadDigest":
      case "CredentialsNotSupported":
      case "EntityTooSmall":
      case "EntityTooLarge":
      case "ExpiredToken":
      case "IllegalLocationConstraintException":
      case "IllegalVersioningConfigurationException":
      case "IncompleteBody":
      case "IncorrectNumberOfFilesInPostRequest":
      case "InlineDataTooLarge":
      case "InvalidAccessPoint":
      case "InvalidArgument":
      case "InvalidBucketName":
      case "InvalidDigest":
      case "InvalidEncryptionAlgorithmError":
      case "InvalidLocationConstraint":
      case "InvalidPart":
      case "InvalidPartOrder":
      case "InvalidPolicyDocument":
      case "InvalidRequest":
      case "InvalidSOAPRequest":
      case "InvalidStorageClass":
      case "InvalidTargetBucketForLogging":
      case "InvalidToken":
      case "InvalidURI":
      case "KeyTooLongError":
      case "MalformedACLError":
      case "MalformedPOSTRequest":
      case "MalformedXML":
      case "MaxMessageLengthExceeded":
      case "MaxPostPreDataLengthExceededError":
      case "MetadataTooLarge":
      case "MissingRequestBodyError":
      case "MissingSecurityElement":
      case "MissingSecurityHeader":
      case "NoLoggingStatusForKey":
      case "RequestIsNotMultiPartContent":
      case "RequestTimeout":
      case "RequestTorrentOfBucketError":
      case "ServerSideEncryptionConfigurationNotFoundError":
      case "TokenRefreshRequired":
      case "TooManyAccessPoints":
      case "TooManyBuckets":
      case "UnexpectedContent":
      case "UnresolvableGrantByEmailAddress":
      case "UserKeyMustBeSpecified":
      case "InvalidTag":
      case "MalformedPolicy":
        return 400;
      case "UnauthorizedAccess":
        return 401;
      case "AccessDenied":
      case "AccountProblem":
      case "AllAccessDisabled":
      case "CrossLocationLoggingProhibited":
      case "InvalidAccessKeyId":
      case "InvalidObjectState":
      case "InvalidPayer":
      case "InvalidSecurity":
      case "NotSignedUp":
      case "RequestTimeTooSkewed":
      case "SignatureDoesNotMatch":
        return 403;
      case "ResourceNotFound": // not in list
      case "NoSuchAccessPoint":
      case "NoSuchBucket":
      case "NoSuchKey":
      case "NoSuchObject": // not in list
      case "NoSuchUpload":
      case "NoSuchVersion":
      case "NoSuchLifecycleConfiguration":
      case "NoSuchBucketPolicy":
      case "NoSuchObjectLockConfiguration": // not in list
      case "NoSuchOutpost":
      case "NoSuchTagSet":
      case "UnsupportedOperation":
        return 404;
      case "MethodNotAllowed":
        return 405;
      case "BucketAlreadyExists":
      case "BucketAlreadyOwnedByYou":
      case "BucketNotEmpty":
      case "InvalidBucketState":
      case "OperationAborted":
      case "InvalidOutpostState":
        return 409;
      case "MissingContentLength":
        return 411;
      case "PreconditionFailed":
        return 412;
      case "InvalidRange":
        return 416;
      case "InternalError":
        return 500;
      case "NotImplemented":
        return 501;
      case "ServiceUnavailable":
      case "SlowDown":
        return 503;
      default:
        return Optional.ofNullable(response)
            .filter(r -> r.code() != 200)
            .map(Response::code)
            .orElse(400);
    }
  }

  private String getErrorCode(ErrorResponseException e) {
    return Optional.of(e)
        .map(ErrorResponseException::errorResponse)
        .map(ErrorResponse::code)
        .orElse(ERROR_CODE_PREFIX + "UNSPECIFIED_ERROR_RESPONSE");
  }

  private String getMessage(ErrorResponseException e) {
    return Optional.of(e)
        .map(ErrorResponseException::errorResponse)
        .map(ErrorResponse::message)
        .orElseGet(() -> StringUtils.hasText(e.getMessage())
            ? e.getMessage()
            : "Unspecified error response.");
  }

}
