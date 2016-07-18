/**
 * 
 */
package org.bremersee.common.spring.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Christian Bremer
 *
 */
@ConfigurationProperties("b2c.security")
public class SecurityProperties {
    
    // TODO
    private int a = 0;
    
    private String contextHolderStrategyName = "MODE_INHERITABLETHREADLOCAL";
    
    //private PasswordEncoderConfig password = new PasswordEncoderConfig();

    @Override
    public String toString() {
        return "SecurityProperties [contextHolderStrategyName=" + contextHolderStrategyName + "]";
    }

    public String getContextHolderStrategyName() {
        return contextHolderStrategyName;
    }

    public void setContextHolderStrategyName(String contextHolderStrategyName) {
        this.contextHolderStrategyName = contextHolderStrategyName;
    }

//    public PasswordEncoderConfig getPassword() {
//        return password;
//    }

}
