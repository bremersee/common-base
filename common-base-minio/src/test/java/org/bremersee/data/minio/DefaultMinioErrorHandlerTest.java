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

import static org.bremersee.data.minio.DefaultMinioErrorHandler.ERROR_CODE_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.minio.errors.ErrorResponseException;
import io.minio.messages.ErrorResponse;
import java.io.IOException;
import java.util.UUID;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

/**
 * The default minio error handler test.
 *
 * @author Christian Bremer
 */
class DefaultMinioErrorHandlerTest {

  /**
   * Map.
   */
  @Test
  void map() {
    DefaultMinioErrorHandler handler = new DefaultMinioErrorHandler();

    MinioException me = handler.map(new IllegalArgumentException("Something is missing"));
    assertEquals(400, me.status());
    assertEquals(ERROR_CODE_PREFIX + "BAD_REQUEST", me.getErrorCode());
    assertEquals("Something is missing", me.getMessage());

    me = handler.map(new IllegalArgumentException(""));
    assertEquals(400, me.status());
    assertEquals(ERROR_CODE_PREFIX + "BAD_REQUEST", me.getErrorCode());
    assertEquals("Bad request.", me.getMessage());

    me = handler.map(new IOException("Ooops"));
    assertEquals(500, me.status());
    assertEquals(ERROR_CODE_PREFIX + "IO_ERROR", me.getErrorCode());
    assertEquals("Ooops", me.getMessage());

    me = handler.map(new IOException(""));
    assertEquals(500, me.status());
    assertEquals(ERROR_CODE_PREFIX + "IO_ERROR", me.getErrorCode());
    assertEquals("IO operation failed.", me.getMessage());

    me = handler.map(new Exception("Something bad happen."));
    assertEquals(500, me.status());
    assertEquals(ERROR_CODE_PREFIX + "UNSPECIFIED", me.getErrorCode());
    assertEquals("Something bad happen.", me.getMessage());

    me = handler.map(new Exception(""));
    assertEquals(500, me.status());
    assertEquals(ERROR_CODE_PREFIX + "UNSPECIFIED", me.getErrorCode());
    assertEquals("Unmapped minio error.", me.getMessage());

    me = handler.map(new io.minio.errors.MinioException());
    assertEquals(500, me.status());

    me = handler.map(new io.minio.errors.BucketPolicyTooLargeException("bucket-name"));
    assertEquals(400, me.status());

    me = handler.map(new io.minio.errors.InsufficientDataException("A message."));
    assertEquals(400, me.status());

    me = handler.map(new io.minio.errors.InternalException("A message."));
    assertEquals(500, me.status());

    me = handler.map(new io.minio.errors.InvalidResponseException(503, "application/json", "{}"));
    assertEquals(500, me.status());

    me = handler.map(new io.minio.errors.ServerException("A message."));
    assertEquals(500, me.status());

    me = handler.map(new io.minio.errors.XmlParserException(new Exception("A message.")));
    assertEquals(500, me.status());

    me = handler.map(new ErrorResponseException(
        new ErrorResponse("InvalidRequest", "message", "bucket", "object", "123", "456", "789"),
        new Response.Builder()
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Internal server error")
            .request(new Request.Builder()
                .url("http://example.org")
                .build())
            .build()));
    assertEquals(400, me.status());

    me = handler.map(new ErrorResponseException(
        new ErrorResponse("NoSuchBucket", "message", "bucket", "object", "123", "456", "789"),
        new Response.Builder()
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Internal server error")
            .request(new Request.Builder()
                .url("http://example.org")
                .build())
            .build()));
    assertEquals(404, me.status());

    me = handler.map(new ErrorResponseException(
        new ErrorResponse("BucketAlreadyExists", "message", "bucket", "object", "123", "456", "789"),
        new Response.Builder()
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Internal server error")
            .request(new Request.Builder()
                .url("http://example.org")
                .build())
            .build()));
    assertEquals(409, me.status());

    me = handler.map(new ErrorResponseException(
        new ErrorResponse(UUID.randomUUID().toString(), "message", "bucket", "object", "123", "456", "789"),
        new Response.Builder()
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Internal server error")
            .request(new Request.Builder()
                .url("http://example.org")
                .build())
            .build()));
    assertEquals(500, me.status());
  }
}