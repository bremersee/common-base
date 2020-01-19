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

package org.bremersee.thymeleaf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.bremersee.thymeleaf.AdditionalThymeleafProperties.ResolverProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.DefaultResourceLoader;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * The additional thymeleaf auto configuration test.
 *
 * @author Christian Bremer
 */
class AdditionalThymeleafAutoConfigurationTest {

  /**
   * Register template resolver.
   */
  @Test
  void registerTemplateResolver() {
    // Mockito cannot mock TemplateEngine because getTemplateResolvers() is final.
    TemplateEngine templateEngine = new TemplateEngine();
    //noinspection unchecked
    ObjectProvider<TemplateEngine> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(templateEngine);

    AdditionalThymeleafProperties properties = new AdditionalThymeleafProperties();
    AdditionalThymeleafAutoConfiguration configuration
        = new AdditionalThymeleafAutoConfiguration(properties, provider);
    configuration.setResourceLoader(new DefaultResourceLoader());
    configuration.registerTemplateResolver();

    ResolverProperties resolver0 = new ResolverProperties();
    resolver0.setName("resolver0");
    resolver0.setCacheablePatterns(Collections.singleton("1234"));
    resolver0.setCacheTtlms(123456789L);
    resolver0.setCssTemplateModePatterns(Collections.singleton("2345"));
    resolver0.setHtmlTemplateModePatterns(Collections.singleton("3456"));
    resolver0.setJavaScriptTemplateModePatterns(Collections.singleton("4567"));
    resolver0.setNonCacheablePatterns(Collections.singleton("5678"));
    resolver0.setPrefix("6789");
    resolver0.setRawTemplateModePatterns(Collections.singleton("7890"));
    resolver0.setSuffix("12345");
    resolver0.setTemplateAliases(Collections.singletonMap("key", "23456"));
    resolver0.setTemplateMode(TemplateMode.HTML);
    resolver0.setTextTemplateModePatterns(Collections.singleton("34567"));
    resolver0.setXmlTemplateModePatterns(Collections.singleton("45678"));

    ResolverProperties resolver1 = new ResolverProperties();
    resolver1.setName("");
    resolver1.setPrefix("");
    resolver1.setSuffix("");
    resolver0.setTemplateMode(null);

    properties.getResolvers().add(resolver0);
    properties.getResolvers().add(resolver1);
    configuration.registerTemplateResolver();

    assertEquals(2, templateEngine.getTemplateResolvers().size());
  }

}