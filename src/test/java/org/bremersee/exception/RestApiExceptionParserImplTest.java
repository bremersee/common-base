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

package org.bremersee.exception;

import static org.bremersee.http.converter.ObjectMapperHelper.getJsonMapper;
import static org.bremersee.http.converter.ObjectMapperHelper.getXmlMapper;

import lombok.extern.slf4j.Slf4j;
import org.bremersee.TestHelper;
import org.bremersee.exception.model.RestApiException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;

/**
 * @author Christian Bremer
 */
@Slf4j
public class RestApiExceptionParserImplTest {

  @Test
  public void testResponseIsNull() {
    Assert.assertNull(new RestApiExceptionParserImpl().parseRestApiException(null, null));
  }

  @Test
  public void testResponseIsJson() throws Exception {
    final RestApiException expected = TestHelper.restApiException();
    log.info("Expected: {}", expected);
    final RestApiException actual = new RestApiExceptionParserImpl()
        .parseRestApiException(
            getJsonMapper().writeValueAsString(expected),
            MediaType.APPLICATION_JSON_UTF8_VALUE);
    log.info("Actual:   {}", actual);
    Assert.assertNotNull(actual);
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testResponseIsXml() throws Exception {
    final RestApiException expected = TestHelper.restApiException();
    log.info("Expected: {}", expected);
    final RestApiException actual = new RestApiExceptionParserImpl()
        .parseRestApiException(
            getXmlMapper().writeValueAsString(expected),
            MediaType.APPLICATION_XML_VALUE);
    log.info("Actual:   {}", actual);
    Assert.assertNotNull(actual);
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testResponseIsSomethingElse() throws Exception {
    final String response = getJsonMapper()
        .writeValueAsString(TestHelper.otherResponse());
    final RestApiException expected = new RestApiException();
    expected.setMessage(response);
    log.info("Expected: {}", expected);
    final RestApiException actual = new RestApiExceptionParserImpl()
        .parseRestApiException(
            response,
            MediaType.APPLICATION_JSON_VALUE);
    log.info("Actual:   {}", actual);
    Assert.assertNotNull(actual);
    Assert.assertEquals(expected, actual);
  }

}
