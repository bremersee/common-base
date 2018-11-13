/*
 * Copyright 2017 the original author or authors.
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

package org.bremersee.exception.feign;

import static org.bremersee.http.converter.ObjectMapperHelper.getJsonMapper;
import static org.bremersee.http.converter.ObjectMapperHelper.getXmlMapper;

import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.bremersee.TestHelper;
import org.bremersee.exception.model.RestApiException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Christian Bremer
 */
public class FeignClientExceptionErrorDecoderTest {

  private static final FeignClientExceptionErrorDecoder decoder
      = new FeignClientExceptionErrorDecoder();

  @Test
  public void testDecodeJson() throws Exception {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    final RestApiException expected = TestHelper.restApiException();
    //noinspection unchecked
    final Response response = Response
        .builder()
        .body(getJsonMapper().writeValueAsBytes(expected))
        .headers((Map) headers)
        .reason("Something bad")
        .status(500)
        .build();
    final Exception actual = decoder.decode("getSomething", response);
    Assert.assertNotNull(actual);
    Assert.assertTrue(actual instanceof FeignClientException);
    Assert.assertEquals(500, ((FeignClientException) actual).status());
    Assert.assertEquals(expected, ((FeignClientException) actual).getRestApiException());
  }

  @Test
  public void testDecodeXml() throws Exception {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
    final RestApiException expected = TestHelper.restApiException();
    //noinspection unchecked
    final Response response = Response
        .builder()
        .body(getXmlMapper().writeValueAsBytes(expected))
        .headers((Map) headers)
        .reason("Nothing found")
        .status(404)
        .build();
    final Exception actual = decoder.decode("getSomethingThatNotNotExists", response);
    Assert.assertNotNull(actual);
    Assert.assertTrue(actual instanceof FeignClientException);
    Assert.assertEquals(404, ((FeignClientException) actual).status());
    Assert.assertEquals(expected, ((FeignClientException) actual).getRestApiException());
  }

  @Test
  public void testDecodeSomethingElse() throws Exception {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
    final String body = getXmlMapper().writeValueAsString(TestHelper.otherResponse());
    //noinspection unchecked
    final Response response = Response
        .builder()
        .body(body.getBytes(StandardCharsets.UTF_8))
        .headers((Map) headers)
        .reason("Something bad")
        .status(500)
        .build();
    final Exception actual = decoder.decode("getSomething", response);
    Assert.assertNotNull(actual);
    Assert.assertTrue(actual instanceof FeignClientException);
    Assert.assertEquals(500, ((FeignClientException) actual).status());
    Assert.assertNotNull(((FeignClientException) actual).getRestApiException());
    //noinspection ConstantConditions
    Assert.assertEquals(body, ((FeignClientException) actual).getRestApiException().getMessage());
  }

  @Test
  public void testDecodeEmptyResponse() {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
    final String body = "";
    //noinspection unchecked
    final Response response = Response
        .builder()
        .body(body.getBytes(StandardCharsets.UTF_8))
        .headers((Map) headers)
        .reason("Something bad")
        .status(500)
        .build();
    final Exception actual = decoder.decode("getNothing", response);
    Assert.assertNotNull(actual);
    Assert.assertTrue(actual instanceof FeignClientException);
    Assert.assertEquals(500, ((FeignClientException) actual).status());

    // TODO wollen wir hier trotzdem ein cause haben?
    Assert.assertNull(((FeignClientException) actual).getRestApiException());
  }

}
