/*
 * Copyright 2020 the original author or authors.
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

package org.bremersee.security.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ClassUtils;

/**
 * The password encoder auto configuration.
 *
 * @author Christian Bremer
 */
@Configuration
@ConditionalOnClass({
    PasswordEncoderFactories.class,
    PasswordEncoder.class
})
@ConditionalOnWebApplication(type = Type.ANY)
@Slf4j
public class PasswordEncoderAutoConfiguration {

  /**
   * Creates a password encoder bean.
   *
   * @return the password encoder
   */
  @Bean
  @ConditionalOnMissingBean
  @Lazy
  public PasswordEncoder passwordEncoder() {
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {} is creating a {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName(),
        ClassUtils.getUserClass(PasswordEncoder.class).getSimpleName());

    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

}
