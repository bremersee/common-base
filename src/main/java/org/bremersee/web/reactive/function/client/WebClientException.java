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
import java.util.Map;
import org.bremersee.exception.model.RestApiException;
import org.springframework.http.HttpStatus;

/**
 * @author Christian Bremer
 */
public class WebClientException extends AbstractWebClientException {

  public WebClientException(
      final HttpStatus status,
      final Map<String, Collection<String>> headers,
      final RestApiException restApiException) {
    super(status, headers, restApiException);
  }

}
