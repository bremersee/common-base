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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.lang.NonNull;

/**
 * The json string http message converter.
 *
 * @author Christian Bremer
 */
public class JsonStringHttpMessageConverter extends StringHttpMessageConverter {

  private static final MediaType APPLICATION_PLUS_JSON = new MediaType("application", "*+json");

  /**
   * Instantiates a new json string http message converter.
   *
   * @param charset the charset
   */
  public JsonStringHttpMessageConverter(Charset charset) {
    super(charset);
  }

  @NonNull
  @Override
  protected String readInternal(
      @NonNull Class<? extends String> clazz,
      @NonNull HttpInputMessage inputMessage) throws IOException {

    String value = super.readInternal(clazz, inputMessage);
    MediaType contentType = inputMessage.getHeaders().getContentType();
    // StringHttpMessageConverter is used before MappingJackson2HttpMessageConverter and that
    // causes misbehaviour when the request body is just a JSON string object, which is quoted,
    // for example "I'm a comment.".
    if (value.startsWith("\"")
        && value.endsWith("\"")
        && isJson(contentType)) {
      return value.substring(1, value.length() - 1);
    }
    return value;
  }

  private boolean isJson(MediaType contentType) {
    return Objects.nonNull(contentType)
        && contentType.isCompatibleWith(MediaType.APPLICATION_JSON)
        || contentType.isCompatibleWith(APPLICATION_PLUS_JSON);
  }

}
