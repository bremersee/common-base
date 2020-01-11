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

package org.bremersee.thymeleaf;

import lombok.extern.slf4j.Slf4j;
import org.bremersee.thymeleaf.AdditionalThymeleafProperties.ResolverProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ITemplateResolver;

/**
 * The additional thymeleaf auto configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnClass({
    org.bremersee.thymeleaf.TemplateResolver.class,
    org.thymeleaf.TemplateEngine.class
})
@ConditionalOnBean({
    TemplateEngine.class
})
@AutoConfigureAfter(ThymeleafAutoConfiguration.class)
@Configuration
@EnableConfigurationProperties({
    AdditionalThymeleafProperties.class
})
@Slf4j
public class AdditionalThymeleafAutoConfiguration {

  private ResourceLoader resourceLoader = new DefaultResourceLoader();

  private final AdditionalThymeleafProperties properties;

  private TemplateEngine templateEngine;

  /**
   * Instantiates a new additional thymeleaf auto configuration.
   *
   * @param properties the properties
   * @param templateEngine the template engine
   */
  public AdditionalThymeleafAutoConfiguration(
      AdditionalThymeleafProperties properties,
      ObjectProvider<TemplateEngine> templateEngine) {
    this.properties = properties;
    this.templateEngine = templateEngine.getIfAvailable();
  }

  /**
   * Sets resource loader.
   *
   * @param resourceLoader the resource loader
   */
  @SuppressWarnings("unused")
  public void setResourceLoader(ResourceLoader resourceLoader) {
    if (resourceLoader != null) {
      this.resourceLoader = resourceLoader;
    }
  }

  /**
   * Register template resolver.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void registerTemplateResolver() {
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************\n"
            + "* resolvers size = {}\n"
            + "*********************************************************************************",
        getClass().getSimpleName(),
        properties.getResolvers().size());

    Assert.notNull(templateEngine, "Template engine must be present.");
    int index = templateEngine.getTemplateResolvers() != null
        ? templateEngine.getTemplateResolvers().size()
        : 0;
    for (ResolverProperties resolverProperties : properties.getResolvers()) {
      ITemplateResolver resolver = buildTemplateResolver(resolverProperties, index);
      templateEngine.addTemplateResolver(resolver);
      index++;
    }
  }

  /**
   * Build template resolver.
   *
   * @param resolverProperties the resolver properties
   * @param index the index
   * @return the template resolver
   */
  protected ITemplateResolver buildTemplateResolver(
      final ResolverProperties resolverProperties,
      final int index) {

    log.info("Building thymeleaf resolver template with index {} and properties {}",
        index, properties);
    final TemplateResolver templateResolver = new TemplateResolver(resourceLoader);
    templateResolver.setCacheable(resolverProperties.isCacheable());
    if (!resolverProperties.getCacheablePatterns().isEmpty()) {
      templateResolver.setCacheablePatterns(resolverProperties.getCacheablePatterns());
    }
    if (resolverProperties.getCacheTtlms() != null) {
      templateResolver.setCacheTTLMs(resolverProperties.getCacheTtlms());
    }
    templateResolver.setCharacterEncoding(resolverProperties.getCharacterEncoding());
    templateResolver.setCheckExistence(resolverProperties.isCheckExistence());
    if (!resolverProperties.getCssTemplateModePatterns().isEmpty()) {
      templateResolver.setCSSTemplateModePatterns(resolverProperties.getCssTemplateModePatterns());
    }
    templateResolver.setForceSuffix(resolverProperties.isForceSuffix());
    templateResolver.setForceTemplateMode(resolverProperties.isForceTemplateMode());
    if (!resolverProperties.getHtmlTemplateModePatterns().isEmpty()) {
      templateResolver.setHtmlTemplateModePatterns(
          resolverProperties.getHtmlTemplateModePatterns());
    }
    if (!resolverProperties.getJavaScriptTemplateModePatterns().isEmpty()) {
      templateResolver.setJavaScriptTemplateModePatterns(
          resolverProperties.getJavaScriptTemplateModePatterns());
    }
    if (StringUtils.hasText(resolverProperties.getName())) {
      templateResolver.setName(resolverProperties.getName());
    } else {
      templateResolver.setName("AdditionalThymeleafTemplateResolverNo" + index);
    }
    if (!resolverProperties.getNonCacheablePatterns().isEmpty()) {
      templateResolver.setNonCacheablePatterns(resolverProperties.getNonCacheablePatterns());
    }
    templateResolver.setOrder(index);
    if (StringUtils.hasText(resolverProperties.getPrefix())) {
      templateResolver.setPrefix(resolverProperties.getPrefix());
    }
    if (!resolverProperties.getRawTemplateModePatterns().isEmpty()) {
      templateResolver.setRawTemplateModePatterns(resolverProperties.getRawTemplateModePatterns());
    }
    templateResolver.setResolvablePatterns(resolverProperties.resolvablePatternsOrDefault());
    if (StringUtils.hasText(resolverProperties.getSuffix())) {
      templateResolver.setSuffix(resolverProperties.getSuffix());
    }
    if (!resolverProperties.getTemplateAliases().isEmpty()) {
      templateResolver.setTemplateAliases(resolverProperties.getTemplateAliases());
    }
    if (resolverProperties.getTemplateMode() != null) {
      templateResolver.setTemplateMode(resolverProperties.getTemplateMode());
    }
    if (!resolverProperties.getTextTemplateModePatterns().isEmpty()) {
      templateResolver.setTextTemplateModePatterns(
          resolverProperties.getTextTemplateModePatterns());
    }
    templateResolver.setUseDecoupledLogic(resolverProperties.isUseDecoupledLogic());
    if (!resolverProperties.getXmlTemplateModePatterns().isEmpty()) {
      templateResolver.setXmlTemplateModePatterns(resolverProperties.getXmlTemplateModePatterns());
    }
    return templateResolver;
  }

}
