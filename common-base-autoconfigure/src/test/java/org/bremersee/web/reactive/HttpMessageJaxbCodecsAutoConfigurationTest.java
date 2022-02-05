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

package org.bremersee.web.reactive;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bremersee.xml.JaxbContextBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.codec.CodecConfigurer.CustomCodecs;
import org.springframework.http.codec.ServerCodecConfigurer;

/**
 * The http message jaxb codecs autoconfiguration test.
 *
 * @author Christian Bremer
 */
class HttpMessageJaxbCodecsAutoConfigurationTest {

  /**
   * Init.
   */
  @Test
  void init() {
    HttpMessageJaxbCodecsAutoConfiguration target = new HttpMessageJaxbCodecsAutoConfiguration(
        objectProvider(JaxbContextBuilder.newInstance()));
    target.init();
  }

  /**
   * Configure http message codecs.
   */
  @Test
  void configureHttpMessageCodecs() {
    HttpMessageJaxbCodecsAutoConfiguration target = new HttpMessageJaxbCodecsAutoConfiguration(
        objectProvider(JaxbContextBuilder.newInstance()));

    ServerCodecConfigurer configurer = mock(ServerCodecConfigurer.class);
    CustomCodecs customCodecs = mock(CustomCodecs.class);
    when(configurer.customCodecs()).thenReturn(customCodecs);

    target.configureHttpMessageCodecs(configurer);
    verify(customCodecs, times(2)).registerWithDefaultConfig(any());
  }

  /**
   * Do not configure http message codecs because of missing jaxb context builder.
   */
  @Test
  void doNotConfigureHttpMessageCodecsBecauseOfMissingJaxbContextBuilder() {
    HttpMessageJaxbCodecsAutoConfiguration target = new HttpMessageJaxbCodecsAutoConfiguration(
        objectProvider(null));

    ServerCodecConfigurer configurer = mock(ServerCodecConfigurer.class);

    target.configureHttpMessageCodecs(configurer);
    verify(configurer, never()).customCodecs();
  }

  private static ObjectProvider<JaxbContextBuilder> objectProvider(
      JaxbContextBuilder jaxbContextBuilder) {

    //noinspection unchecked
    ObjectProvider<JaxbContextBuilder> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(jaxbContextBuilder);
    return provider;
  }

}