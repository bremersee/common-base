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

package org.bremersee.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.common.model.JavaLocale;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.AbstractResourceBasedMessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * The message source configuration.
 *
 * @author Christian Bremer
 */
@AutoConfigureBefore({
    org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration.class
})
@Configuration
@EnableConfigurationProperties({
    MessageSourceProperties.class
})
@Slf4j
public class MessageSourceAutoConfiguration {

  private static final String DEFAULT_MESSAGES_BASE_NAME = "messages";

  private final MessageSourceProperties properties;

  /**
   * Instantiates a new message source configuration.
   *
   * @param properties the message source properties
   */
  public MessageSourceAutoConfiguration(MessageSourceProperties properties) {
    this.properties = properties;
  }

  /**
   * Init.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************\n"
            + "* baseNames = {}\n"
            + "* defaultLocale = {}\n"
            + "* fallbackToSystemLocale = {}\n"
            + "* defaultEncoding = {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName(),
        getBaseNames(),
        properties.getDefaultLocale(),
        properties.isFallbackToSystemLocale(),
        properties.getDefaultEncoding());
  }

  /**
   * Creates message source bean.
   *
   * @return the message source bean
   */
  @Bean
  public MessageSource messageSource() {
    AbstractResourceBasedMessageSource messageSource = properties.isUseReloadableMessageSource()
        ? createReloadableMessageSource()
        : createMessageSource();
    configure(messageSource);
    return messageSource;
  }

  private AbstractResourceBasedMessageSource createReloadableMessageSource() {
    final ReloadableResourceBundleMessageSource messageSource
        = new ReloadableResourceBundleMessageSource();
    if (!properties.getFileEncodings().isEmpty()) {
      final Properties fileEncodings = new Properties();
      fileEncodings.putAll(properties.getFileEncodings());
      messageSource.setFileEncodings(fileEncodings);
    }
    return messageSource;
  }

  private AbstractResourceBasedMessageSource createMessageSource() {
    return new ResourceBundleMessageSource();
  }

  private void configure(AbstractResourceBasedMessageSource messageSource) {
    if (StringUtils.hasText(properties.getDefaultLocale())) {
      JavaLocale javaLocale = JavaLocale.fromValue(properties.getDefaultLocale());
      messageSource.setDefaultLocale(javaLocale.toLocale());
    }
    messageSource.setFallbackToSystemLocale(properties.isFallbackToSystemLocale());
    messageSource.setDefaultEncoding(properties.getDefaultEncoding());
    final List<String> baseNames = getBaseNames();
    messageSource.setBasenames(baseNames.toArray(new String[0]));
    messageSource.setCacheSeconds(properties.getCacheSeconds());
    messageSource.setAlwaysUseMessageFormat(properties.isAlwaysUseMessageFormat());
    messageSource.setUseCodeAsDefaultMessage(properties.isUseCodeAsDefaultMessage());
  }

  private List<String> getBaseNames() {
    final List<String> baseNames = new ArrayList<>(properties.getBaseNames());
    if (baseNames.isEmpty()) {
      baseNames.add(DEFAULT_MESSAGES_BASE_NAME);
    }
    return baseNames;
  }
}
