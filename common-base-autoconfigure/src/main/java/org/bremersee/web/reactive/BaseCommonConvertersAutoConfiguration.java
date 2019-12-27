package org.bremersee.web.reactive;

import lombok.extern.slf4j.Slf4j;
import org.bremersee.converter.BaseCommonConverters;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@ConditionalOnWebApplication(type = Type.REACTIVE)
@Configuration
@Slf4j
public class BaseCommonConvertersAutoConfiguration implements WebFluxConfigurer {

  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************",
        getClass().getSimpleName());
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    BaseCommonConverters.registerAll(registry);
  }

}
