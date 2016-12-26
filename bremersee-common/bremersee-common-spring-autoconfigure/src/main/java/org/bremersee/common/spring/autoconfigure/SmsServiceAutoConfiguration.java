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

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.bremersee.sms.AbstractSmsService;
import org.bremersee.sms.DummySmsService;
import org.bremersee.sms.GoyyaSmsService;
import org.bremersee.sms.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christian Bremer
 *
 */
@Configuration
@ConditionalOnClass(name = { 
        "org.bremersee.sms.DummySmsService",
        "org.bremersee.sms.GoyyaSmsService" 
})
@EnableConfigurationProperties({ SmsServiceProperties.class })
public class SmsServiceAutoConfiguration {
    
    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    @Autowired
    protected SmsServiceProperties properties;

    @PostConstruct
    public void init() {
        // @formatter:off
        log.info("\n"
               + "**********************************************************************\n"
               + "*  SMS Service Auto Configuration                                    *\n"
               + "**********************************************************************\n"
               + "properties = " + properties + "\n"
               + "**********************************************************************");
        // @formatter:on
    }
    
    @Bean
    public SmsService smsService() {
        AbstractSmsService smsService;
        if (StringUtils.isAnyBlank(properties.getUrl(), properties.getUsername(), properties.getPassword())) {
            smsService = new DummySmsService();
        } else {
            GoyyaSmsService goyya = new GoyyaSmsService(properties.getUsername(), properties.getPassword(), properties.getUrl());
            goyya.setProxyHost(properties.getProxyHost());
            goyya.setProxyPort(properties.getProxyPort());
            goyya.setProxyUsername(properties.getProxyUsername());
            goyya.setProxyPassword(properties.getProxyPassword());
            goyya.setSendTimePattern(properties.getSendTimePattern());
            smsService = goyya;
        }
        smsService.setCharset(properties.getCharset());
        smsService.setDefaultMessage(properties.getDefaultMessage());
        smsService.setDefaultReceiver(properties.getDefaultReceiver());
        smsService.setDefaultSender(properties.getDefaultSender());
        smsService.setMaxLengthOfOneSms(properties.getMaxLengthOfOneSms());
        return smsService;
    }

}
