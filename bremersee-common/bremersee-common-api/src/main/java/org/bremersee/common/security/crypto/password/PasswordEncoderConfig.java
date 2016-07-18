/**
 * 
 */
package org.bremersee.common.security.crypto.password;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Christian Bremer
 *
 */
public class PasswordEncoderConfig {

    private int randomSaltLength = 4;
    
    private String algorithm = "SSHA";

    @Override
    public String toString() {
        return "PasswordEncoderConfig [randomSaltLength=" + randomSaltLength + ", algorithm=" + algorithm + "]";
    }

    public int getRandomSaltLength() {
        return randomSaltLength;
    }

    public void setRandomSaltLength(int randomSaltLength) {
        if (randomSaltLength > 0) {
            this.randomSaltLength = randomSaltLength;
        }
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        if (StringUtils.isNotBlank(algorithm)) {
            this.algorithm = algorithm;
        }
    }
    
}