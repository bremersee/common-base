/*
 * Copyright 2020 the original author or authors.
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

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import net.minidev.json.JSONValue;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * The json path jwt parser.
 *
 * @author Christian Bremer
 */
class JsonPathJwtParser {

  private static final com.jayway.jsonpath.Configuration jsonPathConf
      = com.jayway.jsonpath.Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS).build();

  private final DocumentContext documentContext;

  /**
   * Instantiates a new json path jwt parser.
   *
   * @param jwt the jwt
   */
  JsonPathJwtParser(Jwt jwt) {
    final String jsonStr = JSONValue.toJSONString(jwt.getClaims());
    this.documentContext = JsonPath.parse(jsonStr, jsonPathConf);
  }

  /**
   * Read the value of the given json path.
   *
   * @param <T> the type of the result
   * @param jsonPath the json path
   * @param resultClass the result class
   * @return the value of the json path
   */
  <T> T read(String jsonPath, Class<T> resultClass) {
    return documentContext.read(jsonPath, resultClass);
  }

}
