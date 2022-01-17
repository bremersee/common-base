/*
 * Copyright 2019-2022 the original author or authors.
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

package org.bremersee.context;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * The message source autoconfiguration test.
 */
class MessageSourceAutoConfigurationTest {

  /**
   * Init.
   */
  @Test
  void init() {
    MessageSourceAutoConfiguration configuration = new MessageSourceAutoConfiguration(
        new MessageSourceProperties());
    configuration.init();
  }

  /**
   * Message source.
   */
  @Test
  void messageSource() {
    MessageSourceProperties properties = new MessageSourceProperties();
    properties.setUseReloadableMessageSource(false);
    MessageSourceAutoConfiguration configuration = new MessageSourceAutoConfiguration(
        properties);
    MessageSource messageSource = configuration.messageSource();
    assertNotNull(messageSource);
    assertTrue(messageSource instanceof ResourceBundleMessageSource);

    properties.setUseReloadableMessageSource(true);
    configuration = new MessageSourceAutoConfiguration(properties);
    messageSource = configuration.messageSource();
    assertNotNull(messageSource);
    assertTrue(messageSource instanceof ReloadableResourceBundleMessageSource);
  }
}