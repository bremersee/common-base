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
@ConfigurationProperties("bremersee.sms")
public class SmsServiceProperties {

    private String defaultSender;

    private String defaultReceiver;

    private String defaultMessage;

    private String charset = "ISO-8859-1";

    private int maxLengthOfOneSms = 153;
    

    private String url = "https://gate1.goyyamobile.com/sms/sendsms.asp";

    private String username;

    private String password;

    private String proxyHost;

    private Integer proxyPort;

    private String proxyUsername;

    private String proxyPassword;

    private String sendTimePattern = "HHmmddMMyyyy";

    /**
     * t|f|b|c – t=Text-SMS, f=Flash-SMS, b=Blink-SMS*, c=long SMS
     */
    private String defaultMessageType = "t";

    @Override
    public String toString() {
        //@formatter:off
        return "SmsServiceProperties ["
                + "\n  url=" + url + ", "
                + "\n  username=" + username + ", password=****, "
                + "\n  proxyHost=" + proxyHost + ", proxyPort=" + proxyPort + ", proxyUsername=" + proxyUsername + ", proxyPassword=****, "
                + "\n  sendTimePattern=" + sendTimePattern + ", "
                + "\n  defaultMessageType=" + defaultMessageType 
                + "\n  defaultSender=" + defaultSender + ", "
                + "\n  defaultReceiver=" + defaultReceiver + ", "
                + "\n  defaultMessage=" + defaultMessage + ", "
                + "\n  charset=" + charset + ", "
                + "\n  maxLengthOfOneSms=" + maxLengthOfOneSms
                + "\n]";
        //@formatter:on
    }

    public String getDefaultSender() {
        return defaultSender;
    }

    public void setDefaultSender(String defaultSender) {
        this.defaultSender = defaultSender;
    }

    public String getDefaultReceiver() {
        return defaultReceiver;
    }

    public void setDefaultReceiver(String defaultReceiver) {
        this.defaultReceiver = defaultReceiver;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public void setDefaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public int getMaxLengthOfOneSms() {
        return maxLengthOfOneSms;
    }

    public void setMaxLengthOfOneSms(int maxLengthOfOneSms) {
        this.maxLengthOfOneSms = maxLengthOfOneSms;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public String getSendTimePattern() {
        return sendTimePattern;
    }

    public void setSendTimePattern(String sendTimePattern) {
        this.sendTimePattern = sendTimePattern;
    }

    public String getDefaultMessageType() {
        return defaultMessageType;
    }

    public void setDefaultMessageType(String defaultMessageType) {
        this.defaultMessageType = defaultMessageType;
    }

}
