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

package org.bremersee.web.servlet;

import static org.assertj.core.api.Assertions.assertThat;

import org.bremersee.http.converter.Jaxb2HttpMessageConverter;
import org.bremersee.xml.JaxbContextBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.mock.env.MockEnvironment;

/**
 * The http message converter autoconfiguration test.
 *
 * @author Christian Bremer
 */
class HttpMessageConverterAutoConfigurationTest {

  private static final HttpMessageConverterAutoConfiguration target
      = new HttpMessageConverterAutoConfiguration();

  /**
   * Init.
   */
  @Test
  void init() {
    target.init();
  }

  /**
   * String http message converter.
   */
  @Test
  void stringHttpMessageConverter() {
    Environment environment = new MockEnvironment();
    StringHttpMessageConverter actual = target.stringHttpMessageConverter(environment);
    assertThat(actual).isNotNull();
  }

  /**
   * Jaxb http message converter.
   */
  @Test
  void jaxb2HttpMessageConverter() {
    Jaxb2HttpMessageConverter actual = target
        .jaxb2HttpMessageConverter(JaxbContextBuilder.newInstance());
    assertThat(actual).isNotNull();
  }
}