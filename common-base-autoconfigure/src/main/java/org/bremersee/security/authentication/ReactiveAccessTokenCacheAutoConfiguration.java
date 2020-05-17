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

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.Assert;

@ConditionalOnWebApplication(type = Type.REACTIVE)
@Configuration
@Conditional(JwtSupportCondition.class)
@Slf4j
public class ReactiveAccessTokenCacheAutoConfiguration {

  @Configuration
  //@ConditionalOnMissingBean // TODO ReactiveRedisConnectionFactory
  @ConditionalOnClass({
      CacheManager.class
  })
  static class InMemory {

    @ConditionalOnMissingBean
    @Lazy
    @Bean
    public AccessTokenCache accessTokenCache(ObjectProvider<List<CacheManager>> cacheManagers) {
      return new AccessTokenCacheAutoConfiguration().accessTokenCache(cacheManagers);
    }

    @ConditionalOnMissingBean
    @Bean
    public ReactiveAccessTokenCache reactiveAccessTokenCache(
        ObjectProvider<AccessTokenCache> accessTokenCache) {
      Assert.notNull(accessTokenCache.getIfAvailable(), "Access token cache must be present.");
      return ReactiveAccessTokenCache.from(accessTokenCache.getIfAvailable());
    }
  }

  static class WithRedis {

  }

}
