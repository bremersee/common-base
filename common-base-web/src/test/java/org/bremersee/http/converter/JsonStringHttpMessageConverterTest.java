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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;

/**
 * The json string http message converter test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class JsonStringHttpMessageConverterTest {

  /**
   * Read internal.
   *
   * @param softly the soft assertions
   * @throws Exception the exception
   */
  @Test
  void readInternal(SoftAssertions softly) throws Exception {
    JsonStringHttpMessageConverter target = new JsonStringHttpMessageConverter(
        StandardCharsets.UTF_8);

    String expected = "Hello world!";
    HttpInputMessage inputMessage = createHttpInputMessage(
        expected, false, MediaType.APPLICATION_JSON);
    String actual = target.readInternal(String.class, inputMessage);
    softly.assertThat(actual).isEqualTo(expected);

    expected = "Hi";
    inputMessage = createHttpInputMessage(
        expected, true, MediaType.APPLICATION_JSON);
    actual = target.readInternal(String.class, inputMessage);
    softly.assertThat(actual).isEqualTo(expected);

    expected = "Bye bye";
    inputMessage = createHttpInputMessage(
        expected, true, MediaType.TEXT_PLAIN);
    actual = target.readInternal(String.class, inputMessage);
    softly.assertThat(actual).isEqualTo("\"" + expected + "\"");
  }

  private HttpInputMessage createHttpInputMessage(
      String body, boolean wrap, MediaType contentType) {

    return new HttpInputMessage() {

      @NonNull
      @Override
      public InputStream getBody() {
        String wrappedBody = wrap ? "\"" + body + "\"" : body;
        return new ByteArrayInputStream(wrappedBody.getBytes(StandardCharsets.UTF_8));
      }

      @NonNull
      @Override
      public HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        return headers;
      }
    };
  }
}