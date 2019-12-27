package org.bremersee.web.servlet;

import lombok.extern.slf4j.Slf4j;
import org.bremersee.web.CorsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ConditionalOnWebApplication(type = Type.SERVLET)
@EnableConfigurationProperties(CorsProperties.class)
@Configuration
@Slf4j
public class CorsAutoConfiguration implements WebMvcConfigurer {

  private final CorsProperties properties;

  /**
   * Instantiates a new cors auto configuration.
   *
   * @param properties the cors properties
   */
  public CorsAutoConfiguration(CorsProperties properties) {
    this.properties = properties;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************\n"
            + "* corsProperties = {}\n"
            + "*********************************************************************************",
        getClass().getSimpleName(), properties);
  }

  @Override
  public void addCorsMappings(CorsRegistry corsRegistry) {
    for (CorsProperties.CorsConfiguration config : properties.getConfigs()) {
      corsRegistry.addMapping(config.getPathPattern())
          .allowedOrigins(config.getAllowedOrigins().toArray(new String[0]))
          .allowedMethods(config.getAllowedMethods().toArray(new String[0]))
          .allowedHeaders(config.getAllowedHeaders().toArray(new String[0]))
          .maxAge(config.getMaxAge())
          .allowCredentials(config.isAllowCredentials());
    }
  }

}
