/*
 * Copyright 2022 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.xml.JaxbContextBuilder;
import org.bremersee.xml.JaxbContextData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * The jaxb http message converter test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class Jaxb2HttpMessageConverterTest {

  private static final JaxbContextBuilder jaxbContextBuilder = JaxbContextBuilder
      .newInstance()
      .add(new JaxbContextData(RestApiException.class));

  private Jaxb2HttpMessageConverter target;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    target = new Jaxb2HttpMessageConverter(jaxbContextBuilder);
  }

  /**
   * Can read.
   *
   * @param softly the soft assertions
   */
  @Test
  void canRead(SoftAssertions softly) {
    softly.assertThat(target.canRead(RestApiException.class, MediaType.APPLICATION_XML))
        .isTrue();
    softly.assertThat(target.canRead(RestApiException.class, MediaType.APPLICATION_JSON))
        .isFalse();
    softly.assertThat(target.canRead(Locale.class, MediaType.APPLICATION_XML))
        .isFalse();
  }

  /**
   * Can write.
   *
   * @param softly the soft assertions
   */
  @Test
  void canWrite(SoftAssertions softly) {
    softly.assertThat(target.canWrite(RestApiException.class, MediaType.APPLICATION_XML))
        .isTrue();
    softly.assertThat(target.canWrite(RestApiException.class, MediaType.APPLICATION_JSON))
        .isFalse();
    softly.assertThat(target.canWrite(Locale.class, MediaType.APPLICATION_XML))
        .isFalse();
  }

  /**
   * Supports.
   */
  @Test
  void supports() {
    assertThatExceptionOfType(UnsupportedOperationException.class)
        .isThrownBy(() -> target.supports(RestApiException.class));
  }

  /**
   * Read from source.
   *
   * @throws Exception the exception
   */
  @Test
  void readFromSource() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);
    RestApiException expected = RestApiException.builder()
        .timestamp(OffsetDateTime
            .parse("2022-02-05T10:52:37.000Z", DateTimeFormatter.ISO_ZONED_DATE_TIME))
        .application("junit")
        .message("Read rest api exception fails?")
        .build();
    Object actual = target.readFromSource(
        RestApiException.class,
        headers,
        createSource(expected));
    assertThat(actual).isEqualTo(expected);
  }

  private Source createSource(Object o) throws Exception {
    StringWriter sw = new StringWriter();
    jaxbContextBuilder.buildMarshaller(o).marshal(o, sw);
    String xml = sw.toString();
    return new StreamSource(new StringReader(xml));
  }

  /**
   * Write to result.
   *
   * @throws Exception the exception
   */
  @Test
  void writeToResult() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);
    RestApiException expected = RestApiException.builder()
        .timestamp(OffsetDateTime
            .parse("2022-02-05T10:52:37.000Z", DateTimeFormatter.ISO_ZONED_DATE_TIME))
        .application("junit")
        .message("Read rest api exception fails?")
        .build();
    StringWriter stringWriter = new StringWriter();
    target.writeToResult(expected, headers, new StreamResult(stringWriter));
    Object actual = jaxbContextBuilder.buildUnmarshaller()
        .unmarshal(new StringReader(stringWriter.toString()));
    assertThat(actual).isEqualTo(expected);
  }
}