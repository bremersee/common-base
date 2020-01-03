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
    name = "enable-jwt-support",
    havingValue = "true")
@ConditionalOnClass({
    org.bremersee.security.authentication.JsonPathReactiveJwtConverter.class,
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
            + "*********************************************************************************\n"
            + "* rolesJsonPath = {}\n"
            + "* rolesValueList = {}\n"
            + "* rolesValueSeparator = {}\n"
            + "* rolePrefix = {}\n"
            + "* nameJsonPath = {}\n"
            + "*********************************************************************************",
        getClass().getSimpleName(),
        properties.getRolesJsonPath(),
        properties.isRolesValueList(),
        properties.getRolesValueSeparator(),
        properties.getRolePrefix(),
        properties.getNameJsonPath());
  }

  @ConditionalOnMissingBean
  @Bean
  public JsonPathReactiveJwtConverter jsonPathReactiveJwtConverter() {
    log.info("Creating {} ...", JsonPathReactiveJwtConverter.class.getSimpleName());
    JsonPathJwtConverter converter = new JsonPathJwtConverter();
    converter.setNameJsonPath(properties.getNameJsonPath());
    converter.setRolePrefix(properties.getRolePrefix());
    converter.setRolesJsonPath(properties.getRolesJsonPath());
    converter.setRolesValueList(properties.isRolesValueList());
    converter.setRolesValueSeparator(properties.getRolesValueSeparator());
    return new JsonPathReactiveJwtConverter(converter);
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
      JsonPathReactiveJwtConverter jwtConverter,
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
