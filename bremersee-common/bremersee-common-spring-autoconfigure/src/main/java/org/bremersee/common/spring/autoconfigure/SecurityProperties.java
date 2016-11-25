/**
 * 
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
