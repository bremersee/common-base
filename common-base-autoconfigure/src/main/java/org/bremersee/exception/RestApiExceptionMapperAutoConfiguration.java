package org.bremersee.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.ClassUtils;

/**
 * The rest api exception mapper auto configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.ANY)
@Configuration
@EnableConfigurationProperties({RestApiExceptionMapperProperties.class})
@Slf4j
public class RestApiExceptionMapperAutoConfiguration {

  private final String applicationName;

  private final RestApiExceptionMapperProperties properties;

  /**
   * Instantiates a new rest api exception mapper auto configuration.
   *
   * @param applicationName the application name
   * @param properties the properties
   */
  public RestApiExceptionMapperAutoConfiguration(
      @Value("${spring.application.name:application}") String applicationName,
      RestApiExceptionMapperProperties properties) {
    this.applicationName = applicationName;
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
            + "* applicationName = {}\n"
            + "* apiPaths = {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName(),
        applicationName,
        properties.getApiPaths());
  }

  /**
   * Builds the rest api exception mapper bean.
   *
   * @return the rest api exception mapper bean
   */
  @Bean
  public RestApiExceptionMapper restApiExceptionMapper() {
    return new RestApiExceptionMapperImpl(properties, applicationName);
  }

}
