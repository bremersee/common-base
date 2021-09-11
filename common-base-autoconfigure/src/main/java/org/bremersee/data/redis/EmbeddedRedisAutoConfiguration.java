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

package org.bremersee.data.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.ClassUtils;
import org.springframework.util.SocketUtils;
import redis.embedded.RedisServer;

/**
 * The embedded redis auto configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnClass(name = {
    "redis.embedded.RedisServer",
    "org.springframework.data.redis.connection.RedisConnectionFactory"
})
@ConditionalOnProperty(name = "bremersee.redis.embedded", havingValue = "true")
@Configuration
@AutoConfigureBefore({RedisAutoConfiguration.class})
@Slf4j
public class EmbeddedRedisAutoConfiguration {

  /**
   * Overwrites the redis properties and sets host to 'localhost' and port to a random one.
   *
   * @return the redis properties
   */
  @Bean
  @Primary
  public RedisProperties embeddedRedisProperties() {
    RedisProperties redisProperties = new RedisProperties();
    redisProperties.setHost("localhost");
    redisProperties.setPort(SocketUtils.findAvailableTcpPort(12000));
    return redisProperties;
  }

  /**
   * Creates embedded redis server.
   *
   * @param redisProperties the redis properties
   * @return the redis server
   * @throws Exception the exception
   */
  @Bean(initMethod = "start", destroyMethod = "stop")
  public RedisServer redisServer(RedisProperties redisProperties) throws Exception {
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************\n"
            + "* host = {}\n"
            + "* port = {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName(),
        redisProperties.getHost(),
        redisProperties.getPort());
    return new RedisServer(redisProperties.getPort());
  }

}
