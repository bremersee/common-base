/*
 * Copyright 2019 the original author or authors.
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

package org.bremersee.http.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Helper to create an {@link ObjectMapper} for JSON and XML.
 *
 * @author Christian Bremer
 */
public abstract class ObjectMapperHelper {

  private ObjectMapperHelper() {
  }

  /**
   * Gets an object mapper for JSON.
   *
   * @return an object mapper for JSON
   */
  public static ObjectMapper getJsonMapper() {
    return Jackson2ObjectMapperBuilder
        .json()
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .featuresToEnable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
        .build();
  }

  /**
   * Gets an object mapper for XML.
   *
   * @return an object mapper for XML
   */
  public static XmlMapper getXmlMapper() {
    return Jackson2ObjectMapperBuilder
        .xml()
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .featuresToEnable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
        .build();
  }

}
