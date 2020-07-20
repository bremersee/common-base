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

package org.bremersee.data.minio;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.ClassUtils;

/**
 * The minio auto configuration.
 *
 * @author Christian Bremer
 */
@Configuration
@ConditionalOnClass({
    MinioClient.class,
    MinioTemplate.class
})
@ConditionalOnProperty(
    prefix = "bremersee.minio",
    name = {"url", "access-key", "secret-key"})
@EnableConfigurationProperties(MinioProperties.class)
@Slf4j
public class MinioAutoConfiguration {

  private final MinioProperties properties;

  /**
   * Instantiates a new minio auto configuration.
   *
   * @param properties the properties
   */
  public MinioAutoConfiguration(MinioProperties properties) {
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
            + "* properties = {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName(),
        properties);
  }

  /**
   * Creates minio client.
   *
   * @return the minio client
   */
  @ConditionalOnMissingBean
  @Bean
  public MinioClient minioClient() {

    log.info("Creating {} ...", MinioClient.class.getSimpleName());
    MinioClient minioClient = MinioClient.builder()
        .credentials(properties.getAccessKey(), properties.getSecretKey())
        .endpoint(HttpUrl.get(properties.getUrl()))
        .build();
    minioClient.setTimeout(
        properties.getConnectTimeout().toMillis(),
        properties.getWriteTimeout().toMillis(),
        properties.getReadTimeout().toMillis()
    );
    return minioClient;
  }

  /**
   * Creates minio template.
   *
   * @param minioClient the minio client
   * @param errorHandlerProvider the error handler provider
   * @return the minio template
   */
  @ConditionalOnMissingBean(MinioOperations.class)
  @Bean
  public MinioTemplate minioTemplate(
      MinioClient minioClient,
      ObjectProvider<MinioErrorHandler> errorHandlerProvider) {

    log.info("Creating {} ...", MinioTemplate.class.getSimpleName());
    MinioTemplate minioTemplate = new MinioTemplate(minioClient);
    minioTemplate.setErrorHandler(errorHandlerProvider.getIfAvailable());
    return minioTemplate;
  }

}
