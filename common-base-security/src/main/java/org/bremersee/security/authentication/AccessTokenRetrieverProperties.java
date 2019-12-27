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

package org.bremersee.security.authentication;

import org.springframework.util.MultiValueMap;

/**
 * The access token retriever properties.
 *
 * @author Christian Bremer
 */
public interface AccessTokenRetrieverProperties {

  /**
   * Gets token endpoint.
   *
   * @return the token endpoint
   */
  String getTokenEndpoint();

  /**
   * Create http request body.
   *
   * @return the multi value map
   */
  MultiValueMap<String, String> createBody();

}
