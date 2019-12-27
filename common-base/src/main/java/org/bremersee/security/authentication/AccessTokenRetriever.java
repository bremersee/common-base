/*
 * Copyright 2016 the original author or authors.
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

import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * Retrieve the access token from the identity provider.
 *
 * @author Christian Bremer
 */
@Validated
public interface AccessTokenRetriever<T> {

  /**
   * Retrieve the access token from the identity provider.
   *
   * @param properties the request properties
   * @return the access token
   */
  @NotNull
  T retrieveAccessToken(@NotNull AccessTokenRetrieverProperties properties);

}
