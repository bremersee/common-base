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

package org.bremersee.exception;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.web.reactive.function.client.WebClientException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Christian Bremer
 */
public class RestApiExceptionMapperImplTest {

  private static RestApiExceptionMapper mapper;

  @BeforeClass
  public static void init() {
    final RestApiExceptionMapperProperties properties = new RestApiExceptionMapperProperties();
    properties.setApiPaths(Collections.singletonList("/api/**"));
    mapper = new RestApiExceptionMapperImpl(properties, "test");
  }

  @Test
  public void testGetApiPaths() {
    Assert.assertTrue(mapper.getApiPaths().contains("/api/**"));
  }

  @Test
  public void testBuild409() {
    final ServiceException exception = new ServiceException(409, "Either a or b", "TEST:4711");
    final RestApiException model = mapper.build(exception, "/api/something", null);
    Assert.assertNotNull(model);
    Assert.assertEquals(exception.getErrorCode(), model.getErrorCode());
    Assert.assertFalse(model.isErrorCodeInherited());
    Assert.assertEquals(exception.getMessage(), model.getMessage());
    Assert.assertEquals("/api/something", model.getRequestPath());
    Assert.assertNull(model.getId());
  }

  @Test
  public void testBuild500() {
    final ServiceException exception = new ServiceException(500, "Something failed.", "TEST:4711");
    final RestApiException model = mapper.build(exception, "/api/something", null);
    Assert.assertNotNull(model);
    Assert.assertEquals(exception.getErrorCode(), model.getErrorCode());
    Assert.assertFalse(model.isErrorCodeInherited());
    Assert.assertEquals(exception.getMessage(), model.getMessage());
    Assert.assertEquals("/api/something", model.getRequestPath());
    Assert.assertNotNull(model.getId());
  }

  @Test
  public void testBuildWithCause() {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

    final RestApiException cause = new RestApiException();
    cause.setApplication("cause");
    cause.setClassName(ServiceException.class.getName());
    cause.setErrorCode("CBR:0123");
    cause.setErrorCodeInherited(false);
    cause.setId("1");
    cause.setMessage("Something failed in service 'cause'");
    cause.setRequestPath("/api/cause");
    cause.setTimestamp(OffsetDateTime.now(ZoneId.of("UTC")));

    final WebClientException exception = new WebClientException(
        HttpStatus.INTERNAL_SERVER_ERROR,
        Collections.unmodifiableMap(headers),
        cause);

    final RestApiException model = mapper.build(exception, "/api/this", null);
    Assert.assertNotNull(model);
    Assert.assertEquals(cause.getErrorCode(), model.getErrorCode());
    Assert.assertTrue(model.isErrorCodeInherited());
    Assert.assertEquals(exception.getMessage(), model.getMessage());
    Assert.assertEquals("/api/this", model.getRequestPath());
    Assert.assertNotNull(model.getId());
    Assert.assertEquals(cause, model.getCause());
  }

}
