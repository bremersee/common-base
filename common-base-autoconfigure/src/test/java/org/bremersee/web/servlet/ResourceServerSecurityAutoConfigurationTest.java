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

package org.bremersee.web.servlet;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bremersee.security.SecurityProperties;
import org.bremersee.security.authentication.JsonPathJwtConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;

/**
 * The resource server security auto configuration test.
 *
 * @author Christian Bremer
 */
class ResourceServerSecurityAutoConfigurationTest {

  /**
   * Init.
   */
  @Test
  void init() {
    SecurityProperties properties = new SecurityProperties();
    ResourceServerSecurityAutoConfiguration rss = new ResourceServerSecurityAutoConfiguration(
        mockEnvironment(),
        properties,
        mockJwtConverterProvider());
    rss.init();
  }

  private Environment mockEnvironment() {
    Environment env = mock(Environment.class);
    when(env.getProperty(anyString(), anyString())).thenReturn("mockapp");
    return env;
  }

  private ObjectProvider<JsonPathJwtConverter> mockJwtConverterProvider() {
    JsonPathJwtConverter value = mock(JsonPathJwtConverter.class);
    //noinspection unchecked
    ObjectProvider<JsonPathJwtConverter> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(value);
    return provider;
  }

}