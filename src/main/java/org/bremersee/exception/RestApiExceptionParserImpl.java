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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.util.Collection;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.http.HttpHeadersHelper;
import org.bremersee.http.MediaTypeHelper;
import org.bremersee.http.converter.ObjectMapperHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.StringUtils;

/**
 * @author Christian Bremer
 */
@Slf4j
public class RestApiExceptionParserImpl implements RestApiExceptionParser {

  private final Jackson2ObjectMapperBuilder objectMapperBuilder;

  public RestApiExceptionParserImpl() {
    this.objectMapperBuilder = null;
  }

  public RestApiExceptionParserImpl(
      Jackson2ObjectMapperBuilder objectMapperBuilder) {
    this.objectMapperBuilder = objectMapperBuilder;
  }

  private ObjectMapper getJsonMapper() {
    if (objectMapperBuilder != null) {
      return objectMapperBuilder.build();
    } else {
      return ObjectMapperHelper.getJsonMapper();
    }
  }

  private XmlMapper getXmlMapper() {
    if (objectMapperBuilder != null) {
      return objectMapperBuilder.createXmlMapper(true).build();
    } else {
      return ObjectMapperHelper.getXmlMapper();
    }
  }

  @Override
  public RestApiException parseRestApiException(
      final String response,
      final Map<String, ? extends Collection<String>> headers) {

    final HttpHeaders httpHeaders = HttpHeadersHelper.buildHttpHeaders(headers);
    final String contentType = String.valueOf(httpHeaders.getContentType());

    if (!StringUtils.hasText(response)) {
      return null;
    }

    RestApiException restApiException = null;
    try {
      if (MediaTypeHelper.canContentTypeBeJson(contentType)) {
        restApiException = getJsonMapper().readValue(response, RestApiException.class);
      }
    } catch (Exception ignored) {
      log.debug("msg=[Response is not a 'RestApiException' as JSON.]");
    }
    try {
      if (restApiException == null && MediaTypeHelper.canContentTypeBeXml(contentType)) {
        restApiException = getXmlMapper().readValue(response, RestApiException.class);
      }
    } catch (Exception ignored) {
      log.debug("msg=[Response is not a 'RestApiException' as XML.]");
    }
    if (restApiException == null) {
      restApiException = new RestApiException();
      restApiException.setMessage(response);
    }
    return restApiException;
  }

}
