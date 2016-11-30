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

import java.util.regex.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * @author Christian Bremer
 */
@ConfigurationProperties(prefix = "bremersee.unboundid.ldap.server")
public class LdaptiveInMemoryDirectoryServerProperties {

    private boolean embedded = true;

    private String rootDn = "dc=example,dc=org";

    private String managerDn = "cn=admin,dc=example,dc=org";

    private String managerPassword = "changeit";

    private String trustStoreLocation;
    private String trustStorePassword;
    private String trustStoreFormat;

    private String keyStoreLocation;
    private String keyStorePassword;
    private String keyStoreFormat;
    private String keyAlias;

    private String ldapListenerName = "LDAP";
    private String ldapAddress = null;
    private int ldapPort = 1389;

    private String ldapsListenerName = "LDAPS";
    private String ldapsAddress = null;
    private int ldapsPort = 1636;

    private String schemaLocations;

    private String ldifLocations;

    @Override
    public String toString() {
        //@formatter:off
        return "LdaptiveInMemoryDirectoryServerProperties [\n"
                + "embedded=" + embedded + ",\n"
                + "rootDn=" + rootDn + ",\n"
                + "managerDn=" + managerDn + ",\n"
                + "managerPassword=" + "****" + ",\n"
                + "trustStoreLocation=" + trustStoreLocation + ",\n"
                + "trustStorePassword=" + "****" + ",\n"
                + "trustStoreFormat=" + trustStoreFormat + ",\n"
                + "keyStoreLocation=" + keyStoreLocation + ",\n"
                + "keyStorePassword=" + "****" + ",\n"
                + "keyStoreFormat=" + keyStoreFormat + ",\n"
                + "keyAlias=" + keyAlias + ",\n"
                + "ldapListenerName=" + ldapListenerName + ",\n"
                + "ldapAddress=" + ldapAddress + ",\n"
                + "ldapPort=" + ldapPort + ",\n"
                + "ldapsListenerName=" + ldapsListenerName + ",\n"
                + "ldapsAddress=" + ldapsAddress + ",\n"
                + "ldapsPort=" + ldapsPort + ",\n"
                + "schemaLocations=" + schemaLocations + ",\n"
                + "ldifLocations=" + ldifLocations + "\n"
                + "]";
        //@formatter:on
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public void setEmbedded(boolean embedded) {
        this.embedded = embedded;
    }

    public String getRootDn() {
        return rootDn;
    }

    public void setRootDn(String rootDn) {
        this.rootDn = rootDn;
    }

    public String getManagerDn() {
        return managerDn;
    }

    public void setManagerDn(String managerDn) {
        this.managerDn = managerDn;
    }

    public String getManagerPassword() {
        return managerPassword;
    }

    public void setManagerPassword(String managerPassword) {
        this.managerPassword = managerPassword;
    }

    public String getTrustStoreLocation() {
        return trustStoreLocation;
    }

    public void setTrustStoreLocation(String trustStoreLocation) {
        this.trustStoreLocation = trustStoreLocation;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public String getTrustStoreFormat() {
        return trustStoreFormat;
    }

    public void setTrustStoreFormat(String trustStoreFormat) {
        this.trustStoreFormat = trustStoreFormat;
    }

    public String getKeyStoreLocation() {
        return keyStoreLocation;
    }

    public void setKeyStoreLocation(String keyStoreLocation) {
        this.keyStoreLocation = keyStoreLocation;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getKeyStoreFormat() {
        return keyStoreFormat;
    }

    public void setKeyStoreFormat(String keyStoreFormat) {
        this.keyStoreFormat = keyStoreFormat;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public String getLdapListenerName() {
        if (!StringUtils.hasText(ldapListenerName)) {
            return "LDAP";
        }
        return ldapListenerName;
    }

    public void setLdapListenerName(String ldapListenerName) {
        this.ldapListenerName = ldapListenerName;
    }

    public String getLdapAddress() {
        return ldapAddress;
    }

    public void setLdapAddress(String ldapAddress) {
        this.ldapAddress = ldapAddress;
    }

    public int getLdapPort() {
        return ldapPort;
    }

    public void setLdapPort(int ldapPort) {
        this.ldapPort = ldapPort;
    }

    public String getLdapsListenerName() {
        if (!StringUtils.hasText(ldapsListenerName)) {
            return "LDAPS";
        }
        return ldapsListenerName;
    }

    public void setLdapsListenerName(String ldapsListenerName) {
        this.ldapsListenerName = ldapsListenerName;
    }

    public String getLdapsAddress() {
        if (!StringUtils.hasText(ldapsAddress)) {
            return ldapAddress;
        }
        return ldapsAddress;
    }

    public void setLdapsAddress(String ldapsAddress) {
        this.ldapsAddress = ldapsAddress;
    }

    public int getLdapsPort() {
        return ldapsPort;
    }

    public void setLdapsPort(int ldapsPort) {
        this.ldapsPort = ldapsPort;
    }

    public String getSchemaLocations() {
        return schemaLocations;
    }

    public void setSchemaLocations(String schemaLocations) {
        this.schemaLocations = schemaLocations;
    }

    public String[] getSchemaLocationsAsArray() {
        if (StringUtils.hasText(schemaLocations)) {
            return schemaLocations.split(Pattern.quote(","));
        }
        return new String[0];
    }

    public String getLdifLocations() {
        return ldifLocations;
    }

    public void setLdifLocations(String ldifLocations) {
        this.ldifLocations = ldifLocations;
    }

    public String[] getLdifLocationsAsArray() {
        if (StringUtils.hasText(ldifLocations)) {
            return ldifLocations.split(Pattern.quote(","));
        }
        return new String[0];
    }

}
