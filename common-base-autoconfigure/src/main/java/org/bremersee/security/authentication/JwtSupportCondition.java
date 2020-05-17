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

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * The jwt support condition.
 *
 * @author Christian Bremer
 */
public class JwtSupportCondition extends SpringBootCondition {

  private static final String globalKey = "spring.security.oauth2.resourceserver.jwt.jwk-set-uri";

  private static final String actuatorKey = "bremersee.actuator.auth.jwk-set-uri";

  @Override
  public ConditionOutcome getMatchOutcome(
      ConditionContext context,
      AnnotatedTypeMetadata metadata) {

    final boolean matches = StringUtils.hasText(context.getEnvironment().getProperty(globalKey))
        || StringUtils.hasText(context.getEnvironment().getProperty(actuatorKey));
    return new ConditionOutcome(
        matches,
        "Access token cache requires at least one of these keys: '" + globalKey + "' or '"
            + actuatorKey + "'.");
  }
}
