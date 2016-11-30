/*
 * Copyright 2016 the original author or authors.
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

package org.bremersee.common.spring.autoconfigure;

import org.bremersee.common.security.crypto.password.PasswordEncoderConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Christian Bremer
 *
 */
@ConfigurationProperties("bremersee.security")
public class SecurityProperties {

    private String contextHolderStrategyName = "MODE_INHERITABLETHREADLOCAL";

    private PasswordEncoderConfig encoder = new PasswordEncoderConfig();

    @Override
    public String toString() {
        //@formatter:off
        return "SecurityProperties [contextHolderStrategyName=" + contextHolderStrategyName 
                + ", encoder=" + encoder
                + "]";
        //@formatter:on
    }

    public String getContextHolderStrategyName() {
        return contextHolderStrategyName;
    }

    public void setContextHolderStrategyName(String contextHolderStrategyName) {
        this.contextHolderStrategyName = contextHolderStrategyName;
    }

    public PasswordEncoderConfig getEncoder() {
        return encoder;
    }

}
