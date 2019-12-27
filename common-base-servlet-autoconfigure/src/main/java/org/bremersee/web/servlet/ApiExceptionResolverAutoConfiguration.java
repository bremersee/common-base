package org.bremersee.web.servlet;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.RestApiExceptionMapperAutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.Assert;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnBean({
    RestApiExceptionMapper.class,
    Jackson2ObjectMapperBuilder.class
})
@AutoConfigureAfter({
    RestApiExceptionMapperAutoConfiguration.class
})
@Configuration
@Slf4j
public class ApiExceptionResolverAutoConfiguration implements WebMvcConfigurer {

  private final ApiExceptionResolver apiExceptionResolver;

  public ApiExceptionResolverAutoConfiguration(
      ObjectProvider<RestApiExceptionMapper> apiExceptionMapper,
      ObjectProvider<Jackson2ObjectMapperBuilder> objectMapperBuilder) {

    Assert.notNull(
        apiExceptionMapper.getIfAvailable(),
        "Api exception resolver must be present.");
    Assert.notNull(
        objectMapperBuilder.getIfAvailable(),
        "Object mapper builder must be present.");
    apiExceptionResolver = new ApiExceptionResolver(apiExceptionMapper.getIfAvailable());
    apiExceptionResolver.setObjectMapperBuilder(objectMapperBuilder.getIfAvailable());
  }

  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************",
        getClass().getSimpleName());
  }

  @Override
  public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
    log.info("Adding exception resolver [{}] to registry.",
        apiExceptionResolver.getClass().getSimpleName());
    exceptionResolvers.add(0, apiExceptionResolver);
  }

}
