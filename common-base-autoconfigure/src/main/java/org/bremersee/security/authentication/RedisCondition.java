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

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * The redis condition.
 *
 * @author Christian Bremer
 */
public class RedisCondition extends AnyNestedCondition {

  /**
   * Instantiates a new redis condition.
   */
  public RedisCondition() {
    super(ConfigurationPhase.REGISTER_BEAN);
  }

  /**
   * The lettuce connection factory condition.
   */
  @ConditionalOnBean(LettuceConnectionFactory.class)
  @SuppressWarnings("unused")
  static class LettuceConnectionFactoryCondition {

  }

  /**
   * The jedis connection factory condition.
   */
  @ConditionalOnBean(JedisConnectionFactory.class)
  @SuppressWarnings("unused")
  static class JedisConnectionFactoryCondition {

  }

}
