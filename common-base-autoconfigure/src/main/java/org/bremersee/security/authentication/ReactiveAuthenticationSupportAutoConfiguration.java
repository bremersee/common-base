package org.bremersee.security.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.util.Assert;

@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnProperty(
    prefix = "bremersee.security.authentication",
    name = "enable-keycloak-support",
    havingValue = "true")
@ConditionalOnClass({
    org.bremersee.security.authentication.KeycloakReactiveJwtConverter.class,
    org.bremersee.security.authentication.WebClientAccessTokenRetriever.class
})
@EnableConfigurationProperties(AuthenticationProperties.class)
@Configuration
@Slf4j
public class ReactiveAuthenticationSupportAutoConfiguration {

  private AuthenticationProperties properties;

  public ReactiveAuthenticationSupportAutoConfiguration(
      AuthenticationProperties properties) {
    this.properties = properties;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************",
        getClass().getSimpleName());
  }

  @ConditionalOnMissingBean
  @Bean
  public KeycloakReactiveJwtConverter keycloakReactiveJwtConverter() {
    log.info("Creating {} ...", KeycloakReactiveJwtConverter.class.getSimpleName());
    return new KeycloakReactiveJwtConverter();
  }

  @ConditionalOnMissingBean
  @Bean
  public WebClientAccessTokenRetriever webClientAccessTokenRetriever() {
    log.info("Creating {} ...", WebClientAccessTokenRetriever.class.getSimpleName());
    return new WebClientAccessTokenRetriever();
  }

  @ConditionalOnMissingBean
  @Bean
  public PasswordFlowReactiveAuthenticationManager passwordFlowReactiveAuthenticationManager(
      ObjectProvider<ReactiveJwtDecoder> jwtDecoder,
      KeycloakReactiveJwtConverter jwtConverter,
      WebClientAccessTokenRetriever tokenRetriever) {

    Assert.notNull(jwtDecoder.getIfAvailable(), "Jwt decoder must be present.");
    log.info("Creating {} ...", PasswordFlowReactiveAuthenticationManager.class.getSimpleName());
    return new PasswordFlowReactiveAuthenticationManager(
        properties,
        jwtDecoder.getIfAvailable(),
        jwtConverter,
        tokenRetriever);
  }

}
