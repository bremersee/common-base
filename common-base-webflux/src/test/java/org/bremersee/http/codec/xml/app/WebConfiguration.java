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

package org.bremersee.http.codec.xml.app;

import java.util.ServiceLoader;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.http.codec.xml.ReactiveJaxbDecoder;
import org.bremersee.xml.JaxbContextBuilder;
import org.bremersee.xml.JaxbContextDataProvider;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * The web configuration.
 *
 * @author Christian Bremer
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {ReactiveSecurityAutoConfiguration.class})
@EnableWebFlux
@ComponentScan(basePackageClasses = {WebConfiguration.class})
@Slf4j
public class WebConfiguration implements WebFluxConfigurer {

  private final JaxbContextBuilder jaxbContextBuilder;

  /**
   * Instantiates a new web configuration.
   */
  public WebConfiguration() {
    jaxbContextBuilder = JaxbContextBuilder
        .builder()
        .processAll(ServiceLoader.load(JaxbContextDataProvider.class));
  }

  @Override
  public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
    configurer
        .customCodecs()
        .registerWithDefaultConfig(new ReactiveJaxbDecoder(jaxbContextBuilder));
  }

}
