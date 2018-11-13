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

package org.bremersee.web.reactive.function.client;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionParser;
import org.bremersee.exception.RestApiExceptionParserImpl;
import org.bremersee.exception.model.RestApiException;
import org.springframework.web.reactive.function.client.ClientResponse;

/**
 * @author Christian Bremer
 */
@Slf4j
public class DefaultWebClientErrorDecoder
    extends AbstractWebClientErrorDecoder<WebClientException> {

  private final RestApiExceptionParser parser;

  public DefaultWebClientErrorDecoder() {
    this.parser = new RestApiExceptionParserImpl();
  }

  public DefaultWebClientErrorDecoder(
      RestApiExceptionParser parser) {
    this.parser = parser != null ? parser : new RestApiExceptionParserImpl();
  }

  @Override
  public WebClientException buildException(ClientResponse clientResponse, String response) {

    final Map<String, Collection<String>> headers = Collections
        .unmodifiableMap(clientResponse.headers().asHttpHeaders());
    final RestApiException restApiException = parser.parseRestApiException(
        response,
        clientResponse.headers().asHttpHeaders());
    if (log.isDebugEnabled() && response != null) {
      log.debug("msg=[Is error formatted as rest api exception? {}]",
          restApiException != null && !response.equals(restApiException.getMessage()));
    }
    return new WebClientException(clientResponse.statusCode(), headers, restApiException);
  }

}
