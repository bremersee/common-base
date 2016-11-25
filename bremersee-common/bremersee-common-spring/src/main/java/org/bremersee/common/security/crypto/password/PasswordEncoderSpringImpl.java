/**
 * 
 */
package org.bremersee.common.security.crypto.password;

import org.bremersee.common.security.crypto.password.PasswordEncoderConfig;
import org.bremersee.common.security.crypto.password.PasswordEncoderImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Christian Bremer
 *
 */
public class PasswordEncoderSpringImpl extends PasswordEncoderImpl
        implements PasswordEncoder {

    /**
     * Default constructor.
     */
    public PasswordEncoderSpringImpl() {
    }

    public PasswordEncoderSpringImpl(String algorithm) {
        super(algorithm);
    }

    public PasswordEncoderSpringImpl(String algorithm, int randomSaltLength) {
        super(algorithm, randomSaltLength);
    }

    public PasswordEncoderSpringImpl(PasswordEncoderConfig config) {
        super(config);
    }

}
