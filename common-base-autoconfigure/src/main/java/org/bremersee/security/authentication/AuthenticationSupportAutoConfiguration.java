package org.bremersee.security.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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

  @Bean
  public KeycloakJwtConverter keycloakJwtConverter() {
    return new KeycloakJwtConverter();
  }

  @Bean
  public RestTemplateAccessTokenRetriever restTemplateAccessTokenRetriever(
      ObjectProvider<RestTemplateBuilder> restTemplateBuilder) {

    Assert.notNull(
        restTemplateBuilder.getIfAvailable(),
        "Rest template builder must be present.");
    return new RestTemplateAccessTokenRetriever(restTemplateBuilder.getIfAvailable().build());
  }

  @Bean
  public PasswordFlowAuthenticationManager passwordFlowAuthenticationManager(
      ObjectProvider<JwtDecoder> jwtDecoder,
      KeycloakJwtConverter jwtConverter,
      RestTemplateAccessTokenRetriever tokenRetriever) {

    Assert.notNull(
        jwtDecoder.getIfAvailable(),
        "Jwt decoder must be present.");

    return new PasswordFlowAuthenticationManager(
        properties,
        jwtDecoder.getIfAvailable(),
        jwtConverter,
        tokenRetriever);
  }

}
