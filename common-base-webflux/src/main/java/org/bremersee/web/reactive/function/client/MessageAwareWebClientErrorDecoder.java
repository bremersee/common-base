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

package org.bremersee.web.reactive.function.client;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ExceptionParser;
import org.bremersee.exception.MessageExceptionParser;
import org.bremersee.exception.model.RestApiException;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientResponse;

/**
 * This web client error decoder generates a {@link WebClientException} from the error response.
 *
 * @author Christian Bremer
 */
@Slf4j
@SuppressWarnings({"unused", "WeakerAccess"})
public class MessageAwareWebClientErrorDecoder
    extends AbstractWebClientErrorDecoder<WebClientException> {

  @Getter(AccessLevel.PACKAGE)
  private final ExceptionParser<String> parser;

  @Getter(AccessLevel.PACKAGE)
  private String errorCode;

  /**
   * Instantiates a new message aware web client error decoder.
   */
  public MessageAwareWebClientErrorDecoder() {
    this(null, null);
  }

  /**
   * Instantiates a new message aware web client error decoder.
   *
   * @param errorCode the error code
   */
  public MessageAwareWebClientErrorDecoder(String errorCode) {
    this(new MessageExceptionParser(), errorCode);
  }

  /**
   * Instantiates a new message aware web client error decoder.
   *
   * @param parser the parser
   */
  public MessageAwareWebClientErrorDecoder(
      ExceptionParser<String> parser) {
    this(parser, null);
  }

  /**
   * Instantiates a new message aware web client error decoder.
   *
   * @param parser    the parser
   * @param errorCode the error code
   */
  public MessageAwareWebClientErrorDecoder(
      ExceptionParser<String> parser, String errorCode) {
    this.parser = parser != null ? parser : new MessageExceptionParser();
    this.errorCode = errorCode;
  }

  @Override
  public WebClientException buildException(
      final ClientResponse clientResponse,
      final String response) {

    final Map<String, ? extends Collection<String>> headers = Collections
        .unmodifiableMap(clientResponse.headers().asHttpHeaders());
    final RestApiException restApiException = new RestApiException();
    restApiException.setMessage(parser.parseException(response, headers));
    if (StringUtils.hasText(errorCode)) {
      restApiException.setErrorCode(errorCode);
    }
    return new WebClientException(clientResponse.statusCode(), headers, restApiException);
  }

}
