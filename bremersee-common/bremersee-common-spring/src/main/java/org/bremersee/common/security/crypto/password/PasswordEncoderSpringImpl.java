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

package org.bremersee.common.security.crypto.password;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * <p>
 * Extends the {@link PasswordEncoderImpl} around Spring's {@link PasswordEncoder}.
 * </p>
 *
 * @author Christian Bremer
 */
public class PasswordEncoderSpringImpl extends PasswordEncoderImpl
        implements PasswordEncoder {

    /**
     * Default constructor.
     */
    public PasswordEncoderSpringImpl() {
        super();
    }

    /**
     * Password encoder that uses the specified algorithm.
     *
     * @param algorithm the encoding algorithm
     */
    public PasswordEncoderSpringImpl(final String algorithm) {
        super(algorithm);
    }

    /**
     * Password encoder that uses the specified algorithm and random salt length.
     *
     * @param algorithm        the encoding algorithm
     * @param randomSaltLength the random salt length
     */
    public PasswordEncoderSpringImpl(final String algorithm, final int randomSaltLength) {
        super(algorithm, randomSaltLength);
    }

    /**
     * Password encoder that uses the specified configuration.
     *
     * @param config the password encoder configuration
     */
    public PasswordEncoderSpringImpl(final PasswordEncoderConfig config) {
        super(config);
    }

}
