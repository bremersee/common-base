/*
 * Copyright 2019 the original author or authors.
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
    UnboundIDProvider.class
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
