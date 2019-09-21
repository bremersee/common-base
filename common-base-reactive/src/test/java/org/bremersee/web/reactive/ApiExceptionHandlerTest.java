/*
 * Copyright 2018 the original author or authors.
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

package org.bremersee.web.reactive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.RestApiExceptionMapperImpl;
import org.bremersee.exception.RestApiExceptionMapperProperties;
import org.bremersee.exception.RestApiExceptionUtils;
import org.bremersee.exception.ServiceException;
import org.bremersee.web.reactive.ApiExceptionHandler;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.test.StepVerifier;

/**
 * The api exception handler test.
 *
 * @author Christian Bremer
 */
public class ApiExceptionHandlerTest {

  private static ServiceException exception;

  private static ApiExceptionHandler exceptionHandler;

  /**
   * Setup test.
   */
  @BeforeClass
  public static void setup() {
    final RestApiExceptionMapperProperties properties = new RestApiExceptionMapperProperties();
    properties.setApiPaths(Collections.singletonList("/api/**"));
    properties.getDefaultExceptionMappingConfig().setIncludeHandler(true);
    properties.getDefaultExceptionMappingConfig().setIncludeStackTrace(false);

    final RestApiExceptionMapper mapper = new RestApiExceptionMapperImpl(
        properties, "testapp");

    exception = new ServiceException(500, "Oops, a conflict", "TEST:4711");
    ErrorAttributes errorAttributes = mock(ErrorAttributes.class);
    when(errorAttributes.getError(any(ServerRequest.class))).thenReturn(exception);

    final ResourceProperties resourceProperties = new ResourceProperties();

    ApplicationContext applicationContext = mock(ApplicationContext.class);
    when(applicationContext.getClassLoader())
        .thenReturn(ApplicationContext.class.getClassLoader());

    final DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();

    exceptionHandler = new ApiExceptionHandler(
        errorAttributes,
        resourceProperties,
        applicationContext,
        codecConfigurer,
        mapper);
  }

  /**
   * Test responsible exception handler.
   */
  @Test
  public void testResponsibleExceptionHandler() {
    ServerRequest serverRequest = mock(ServerRequest.class);
    when(serverRequest.path()).thenReturn("/api/resource");
    assertTrue(exceptionHandler.isResponsibleExceptionHandler(serverRequest));
  }

  /**
   * Test render error response as json.
   */
  @Test
  public void testRenderErrorResponseAsJson() {
    doTestingRenderErrorResponse(MediaType.APPLICATION_JSON);
  }

  /**
   * Test render error response as xml.
   */
  @Test
  public void testRenderErrorResponseAsXml() {
    doTestingRenderErrorResponse(MediaType.APPLICATION_XML);
  }

  /**
   * Test render error response as something else.
   */
  @Test
  public void testRenderErrorResponseAsSomethingElse() {
    doTestingRenderErrorResponse(MediaType.IMAGE_JPEG);
  }

  private void doTestingRenderErrorResponse(MediaType mediaType) {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.put(HttpHeaders.ACCEPT, Collections.singletonList(String.valueOf(mediaType)));
    ServerRequest.Headers headers = mock(ServerRequest.Headers.class);
    when(headers.asHttpHeaders()).thenReturn(httpHeaders);
    when(headers.accept()).thenReturn(httpHeaders.getAccept());

    ServerRequest serverRequest = mock(ServerRequest.class);
    when(serverRequest.path()).thenReturn("/api/resource");
    when(serverRequest.headers()).thenReturn(headers);

    StepVerifier.create(exceptionHandler.renderErrorResponse(serverRequest))
        .assertNext(response -> {
          assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode());
          assertTrue(mediaType.isCompatibleWith(response.headers().getContentType()));

          if (MediaType.IMAGE_JPEG.isCompatibleWith(mediaType)) {
            assertEquals(
                exception.getMessage(),
                response.headers().getFirst(RestApiExceptionUtils.MESSAGE_HEADER_NAME));
            assertEquals(
                exception.getErrorCode(),
                response.headers().getFirst(RestApiExceptionUtils.CODE_HEADER_NAME));
            assertEquals(
                exception.getClass().getName(),
                response.headers().getFirst(RestApiExceptionUtils.CLASS_HEADER_NAME));
            assertNotNull(response.headers().getFirst(RestApiExceptionUtils.ID_HEADER_NAME));
            assertNotEquals(
                RestApiExceptionUtils.NO_ID_VALUE,
                response.headers().getFirst(RestApiExceptionUtils.ID_HEADER_NAME));
          }
        })
        .expectNextCount(0)
        .verifyComplete();
  }

}
