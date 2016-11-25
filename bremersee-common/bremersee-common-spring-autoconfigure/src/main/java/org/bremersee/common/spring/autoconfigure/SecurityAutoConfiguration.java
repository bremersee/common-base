/**
 * 
 */
package org.bremersee.common.spring.autoconfigure;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.bremersee.common.security.crypto.password.PasswordEncoderSpringImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Christian Bremer
 *
 */
@Configuration
@ConditionalOnClass(name = {
        "org.springframework.security.core.context.SecurityContextHolder" 
})
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration {
    
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    protected SecurityProperties securityProperties;

    @PostConstruct
    public void init() {
        // @formatter:off
        log.info("\n"
               + "**********************************************************************\n"
               + "*  Security Auto Configuration                                       *\n"
               + "**********************************************************************\n"
               + "securityProperties = " + securityProperties + "\n"
               + "**********************************************************************");
        // @formatter:on
        
        if (StringUtils.isNotBlank(securityProperties.getContextHolderStrategyName())) {
            SecurityContextHolder.setStrategyName(securityProperties.getContextHolderStrategyName());
        }
    }

    @Primary
    @Bean(name = {"passwordEncoder"})
    public PasswordEncoderSpringImpl passwordEncoder() {
        return new PasswordEncoderSpringImpl(securityProperties.getEncoder());
    }

}
