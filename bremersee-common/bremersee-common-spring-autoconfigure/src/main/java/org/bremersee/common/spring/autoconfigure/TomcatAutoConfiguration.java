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

import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @author Christian Bremer
 *
 */
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(TomcatProperties.class)
public class TomcatAutoConfiguration {
    
    @Autowired
    protected TomcatProperties properties;
    
    @Bean
    public EmbeddedServletContainerCustomizer tomcatCustomizer() {
        return new TomcatCustomizer(properties);
    }
    
    private static class TomcatCustomizer implements EmbeddedServletContainerCustomizer {
        
        private final Logger log = LoggerFactory.getLogger(getClass());
        
        private TomcatProperties properties;
        
        public TomcatCustomizer(TomcatProperties properties) {
            this.properties = properties;
        }

//        private static RemoteIpValve createRemoteIpValves() {
//            RemoteIpValve remoteIpValve = new RemoteIpValve();
//            remoteIpValve.setRemoteIpHeader("x-forwarded-for");
//            remoteIpValve.setProtocolHeader("x-forwarded-proto");
//            remoteIpValve.setInternalProxies("192\\.168\\.11\\.11|smac.cstx.de");
//            remoteIpValve.setPortHeader("x-forwarded-port");
//            remoteIpValve.setProxiesHeader("x-forwarded-by");
//            remoteIpValve.setTrustedProxies("192\\.168\\.11\\.11|smac.cstx.de");
//            return remoteIpValve;
//        }
        
        @Override
        public void customize(ConfigurableEmbeddedServletContainer container) {

            if (container instanceof TomcatEmbeddedServletContainerFactory) {
                TomcatEmbeddedServletContainerFactory factory = (TomcatEmbeddedServletContainerFactory)container;

                final TomcatProperties.ConnectorProperties spring = properties.getSpring();
                spring.setPort(factory.getPort());
                factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
                    
                    @Override
                    public void customize(Connector connector) {
                        if (connector.getPort() == spring.getPort()) {
                            if (spring.getRedirectPort() > 0) {
                                log.info("Setting redirect port [" + spring.getRedirectPort() + "] on default connector (port = " + connector.getPort() + ").");
                                connector.setRedirectPort(spring.getRedirectPort());
                            }
                            if (!StringUtils.isEmpty(spring.getProxyName())) {
                                log.info("Setting proxy name [" + spring.getProxyName() + "] on default connector (port = " + connector.getPort() + ").");
                                connector.setProxyName(spring.getProxyName());
                            }
                            if (spring.getProxyPort() > 0) {
                                log.info("Setting proxy port [" + spring.getProxyPort() + "] on default connector (port = " + connector.getPort() + ").");
                                connector.setProxyPort(spring.getProxyPort());
                            }
                            if (!StringUtils.isEmpty(spring.getScheme())) {
                                log.info("Setting scheme [" + spring.getScheme() + "] on default connector (port = " + connector.getPort() + ").");
                                connector.setScheme(spring.getScheme());
                            }
                        }
                    }
                });

                final TomcatProperties.ConnectorProperties ajp = properties.getAjp();
                if (StringUtils.hasText(ajp.getProtocol()) && ajp.getPort() > 0) {
                    
                    // @formatter:off
                    log.info("\n"
                           + "**********************************************************************\n"
                           + "*  Tomcat Customizer                                                 *\n"
                           + "**********************************************************************\n"
                           + "ajp = " + ajp + "\n"
                           + "**********************************************************************");
                    // @formatter:on
                    
                    Connector connector = new Connector(ajp.getProtocol());
                    connector.setPort(ajp.getPort());
                    if (ajp.getRedirectPort() > 0) {
                        connector.setRedirectPort(ajp.getRedirectPort());
                    } else {
                        connector.setRedirectPort(443);
                    }
                    if (StringUtils.hasText(ajp.getProxyName())) {
                        connector.setProxyName(ajp.getProxyName());
                    }
                    if (ajp.getProxyPort() > 0) {
                        connector.setProxyPort(ajp.getProxyPort());
                    }
                    connector.setSecure(ajp.isSecure());
                    if (StringUtils.hasText(ajp.getScheme())) {
                        connector.setScheme(ajp.getScheme());
                    }
                    factory.addAdditionalTomcatConnectors(connector);
                    
                } else {
                    
                    // @formatter:off
                    log.info("\n"
                           + "**********************************************************************\n"
                           + "*  Tomcat Customizer                                                 *\n"
                           + "**********************************************************************");
                    // @formatter:on
                }
                
                TomcatProperties.ConnectorProperties http = properties.getHttp();
                if (StringUtils.hasText(http.getProtocol()) && http.getPort() > 0) {
                    
                    // @formatter:off
                    log.info("\n"
                           + "**********************************************************************\n"
                           + "*  Tomcat Customizer                                                 *\n"
                           + "**********************************************************************\n"
                           + "http = " + http + "\n"
                           + "**********************************************************************");
                    // @formatter:on
                    
                    Connector connector = new Connector(http.getProtocol());
                    connector.setPort(http.getPort());
                    if (http.getRedirectPort() > 0) {
                        connector.setRedirectPort(http.getRedirectPort());
                    } else {
                        connector.setRedirectPort(443);
                    }
                    if (StringUtils.hasText(http.getProxyName())) {
                        connector.setProxyName(http.getProxyName());
                    }
                    if (http.getProxyPort() > 0) {
                        connector.setProxyPort(http.getProxyPort());
                    }
                    connector.setSecure(http.isSecure());
                    if (StringUtils.hasText(http.getScheme())) {
                        connector.setScheme(http.getScheme());
                    }
                    factory.addAdditionalTomcatConnectors(connector);
                    
                } else {
                    
                    // @formatter:off
                    log.info("\n"
                           + "**********************************************************************\n"
                           + "*  Tomcat Customizer                                                 *\n"
                           + "**********************************************************************");
                    // @formatter:on
                }
            }
        }
        
    }

}
