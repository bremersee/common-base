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
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.web.MediaTypeHelper;
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
      return Jackson2ObjectMapperBuilder.json().build();
    }
  }

  private XmlMapper getXmlMapper() {
    if (objectMapperBuilder != null) {
      return objectMapperBuilder.createXmlMapper(true).build();
    } else {
      return Jackson2ObjectMapperBuilder.xml().build();
    }
  }

  @Override
  public RestApiException parseRestApiException(String response, String contentType) {
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
