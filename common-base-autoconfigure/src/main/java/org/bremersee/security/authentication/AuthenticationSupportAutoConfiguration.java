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
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.Assert;

@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnProperty(
    prefix = "bremersee.security.authentication",
    name = "enable-keycloak-support",
    havingValue = "true")
@ConditionalOnClass({
    org.springframework.boot.web.client.RestTemplateBuilder.class,
    org.bremersee.security.authentication.KeycloakJwtConverter.class,
    org.bremersee.security.authentication.RestTemplateAccessTokenRetriever.class
})
@EnableConfigurationProperties(AuthenticationProperties.class)
@Configuration
@Slf4j
public class AuthenticationSupportAutoConfiguration {

  private AuthenticationProperties properties;

  public AuthenticationSupportAutoConfiguration(
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
  public KeycloakJwtConverter keycloakJwtConverter() {
    log.info("Creating {} ...", KeycloakJwtConverter.class.getSimpleName());
    return new KeycloakJwtConverter();
  }

  @ConditionalOnMissingBean
  @Bean
  public RestTemplateAccessTokenRetriever restTemplateAccessTokenRetriever(
      ObjectProvider<RestTemplateBuilder> restTemplateBuilder) {

    Assert.notNull(
        restTemplateBuilder.getIfAvailable(),
        "Rest template builder must be present.");
    log.info("Creating {} ...", RestTemplateAccessTokenRetriever.class.getSimpleName());
    return new RestTemplateAccessTokenRetriever(restTemplateBuilder.getIfAvailable().build());
  }

  @ConditionalOnMissingBean
  @Bean
  public PasswordFlowAuthenticationManager passwordFlowAuthenticationManager(
      ObjectProvider<JwtDecoder> jwtDecoder,
      KeycloakJwtConverter jwtConverter,
      RestTemplateAccessTokenRetriever tokenRetriever) {

    Assert.notNull(
        jwtDecoder.getIfAvailable(),
        "Jwt decoder must be present.");

    log.info("Creating {} ...", PasswordFlowAuthenticationManager.class.getSimpleName());
    return new PasswordFlowAuthenticationManager(
        properties,
        jwtDecoder.getIfAvailable(),
        jwtConverter,
        tokenRetriever);
  }

}
