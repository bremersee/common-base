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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Christian Bremer
 *
 */
@ConfigurationProperties("bremersee.tomcat.connector")
public class TomcatProperties {
    
    private ConnectorProperties spring = new ConnectorProperties();
    
    private ConnectorProperties ajp = new ConnectorProperties();

    private ConnectorProperties http = new ConnectorProperties();
    
    public TomcatProperties() {
        ajp.setProtocol("org.apache.coyote.ajp.AjpNio2Protocol");
        http.setProtocol("org.apache.coyote.http11.Http11NioProtocol");
    }

    @Override
    public String toString() {
        return "TomcatProperties [\nspring=" + spring + "\najp=" + ajp + ",\nhttp=" + http + "\n]";
    }

    public ConnectorProperties getSpring() {
        return spring;
    }

    public void setSpring(ConnectorProperties spring) {
        this.spring = spring;
    }

    public ConnectorProperties getAjp() {
        return ajp;
    }

    public void setAjp(ConnectorProperties ajp) {
        this.ajp = ajp;
    }

    public ConnectorProperties getHttp() {
        return http;
    }

    public void setHttp(ConnectorProperties http) {
        this.http = http;
    }

    public static class ConnectorProperties {
        
        private String protocol;
        
        private int port;

        private int redirectPort = 0;
        
        private String proxyName;
        
        private int proxyPort;
        
        private String scheme;
        
        private boolean secure = true;

        @Override
        public String toString() {
            // @formatter:off
            return "ConnectorProperties ["
                    + "\n- protocol=" + protocol 
                    + ", \n- port=" + port 
                    + ", \n- redirectPort=" + redirectPort 
                    + ", \n- proxyName=" + proxyName 
                    + ", \n- proxyPort=" + proxyPort 
                    + ", \n- scheme=" + scheme 
                    + ", \n- secure=" + secure + "]";
            // @formatter:on
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getRedirectPort() {
            return redirectPort;
        }

        public void setRedirectPort(int redirectPort) {
            this.redirectPort = redirectPort;
        }

        public String getProxyName() {
            return proxyName;
        }

        public void setProxyName(String proxyName) {
            this.proxyName = proxyName;
        }

        public int getProxyPort() {
            return proxyPort;
        }

        public void setProxyPort(int proxyPort) {
            this.proxyPort = proxyPort;
        }

        public String getScheme() {
            return scheme;
        }

        public void setScheme(String scheme) {
            this.scheme = scheme;
        }

        public boolean isSecure() {
            return secure;
        }

        public void setSecure(boolean secure) {
            this.secure = secure;
        }
    }
}
