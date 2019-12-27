package org.bremersee.data.ldaptive;

import org.ldaptive.provider.Provider;
import org.ldaptive.provider.unboundid.UnboundIDProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The unbound id provider auto configuration.
 */
@Configuration
@AutoConfigureBefore({
    LdaptiveAutoConfiguration.class
})
@ConditionalOnClass({
    org.ldaptive.provider.unboundid.UnboundIDProvider.class
})
@ConditionalOnProperty(
    prefix = "bremersee.ldaptive",
    name = "use-unbound-id-provider",
    havingValue = "true")
public class UnboundIdProviderAutoConfiguration {

  /**
   * Creates an unbound id provider.
   *
   * @return the unbound id provider
   */
  @Bean
  public Provider<?> unboundIdProvider() {
    return new UnboundIDProvider();
  }

}
