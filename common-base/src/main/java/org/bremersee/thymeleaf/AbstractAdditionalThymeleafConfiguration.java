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

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.thymeleaf.AdditionalThymeleafProperties.ResolverProperties;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;
import org.thymeleaf.templateresolver.ITemplateResolver;

/**
 * The abstract additional thymeleaf configuration.
 *
 * @author Christian Bremer
 */
@Slf4j
public abstract class AbstractAdditionalThymeleafConfiguration {

  private ApplicationContext applicationContext;

  private AdditionalThymeleafProperties properties;

  /**
   * Instantiates a new additional thymeleaf configuration.
   *
   * @param applicationContext the application context
   * @param properties         the properties
   */
  public AbstractAdditionalThymeleafConfiguration(
      final ApplicationContext applicationContext,
      final AdditionalThymeleafProperties properties) {
    this.applicationContext = applicationContext;
    this.properties = properties;
  }

  /**
   * The resource loader.
   *
   * @return the resource loader
   */
  @SuppressWarnings("WeakerAccess")
  protected ResourceLoader resourceLoader() {
    return new DefaultResourceLoader();
  }

  /**
   * Register template resolver.
   */
  @PostConstruct
  public void registerTemplateResolver() {
    if (applicationContext instanceof ConfigurableApplicationContext) {
      final ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) applicationContext;
      final ConfigurableListableBeanFactory beanFactory = ctx.getBeanFactory();
      int index = properties.getResolverStartIndex() != null
          ? properties.getResolverStartIndex()
          : 2;
      for (final ResolverProperties resolverProperties : properties.getResolvers()) {
        final ITemplateResolver resolver = buildTemplateResolver(resolverProperties, index);
        beanFactory.registerSingleton("additionalTemplateResolver" + index, resolver);
        index++;
      }
    } else {
      log.warn("Application context is not an instance of 'ConfigurableApplicationContext', "
          + "no additional thymeleaf template resolver beans will be added to the context.");
    }
  }

  /**
   * Build template resolver.
   *
   * @param resolverProperties the resolver properties
   * @param index              the index
   * @return the template resolver
   */
  @SuppressWarnings("WeakerAccess")
  protected ITemplateResolver buildTemplateResolver(
      final ResolverProperties resolverProperties,
      final int index) {

    final TemplateResolver templateResolver = new TemplateResolver(resourceLoader());
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
    }
    if (!resolverProperties.getNonCacheablePatterns().isEmpty()) {
      templateResolver.setNonCacheablePatterns(resolverProperties.getNonCacheablePatterns());
    }
    if (resolverProperties.getOrder() != null) {
      templateResolver.setOrder(resolverProperties.getOrder());
    } else {
      templateResolver.setOrder(index);
    }
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
