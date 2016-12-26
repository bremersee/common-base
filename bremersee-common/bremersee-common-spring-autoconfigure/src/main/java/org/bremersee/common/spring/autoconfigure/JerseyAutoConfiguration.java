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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jersey.ResourceConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConditionalOnClass(name = { "org.glassfish.jersey.server.spring.SpringComponentProvider",
        "javax.servlet.ServletRegistration" })
@ConditionalOnBean(type = "org.glassfish.jersey.server.ResourceConfig")
@ConditionalOnWebApplication
@AutoConfigureBefore(org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration.class)
public class JerseyAutoConfiguration {
    
    protected Jaxb2Marshaller jaxb2Marshaller;
    
    private List<JerseyComponentsProvider> componentProviders = new ArrayList<>();

    @Autowired
    @Qualifier("jaxbMarshaller")
    public void setJaxb2Marshaller(Jaxb2Marshaller jaxb2Marshaller) {
        this.jaxb2Marshaller = jaxb2Marshaller;
    }

    @Autowired(required = false)
    public void setComponentProviders(List<JerseyComponentsProvider> componentProviders) {
        if (componentProviders != null) {
            this.componentProviders = componentProviders;
        }
    }

    @Bean
    public ResourceConfigCustomizer jaxbContextResourceConfig() {
        return config -> config.register(new JAXBContextProvider(jaxb2Marshaller));
    }

    @Bean
    public ResourceConfigCustomizer componentsProviderResourceConfig() {
        return new JerseyComponentsCustomizer(componentProviders);
    }

    @Provider
    @Produces({ MediaType.APPLICATION_XML, "text/xml", MediaType.WILDCARD })
    @Consumes({ MediaType.APPLICATION_XML, "text/xml" })
    public static class JAXBContextProvider implements ContextResolver<JAXBContext> {
        
        protected final Jaxb2Marshaller jaxb2Marshaller;
        
        public JAXBContextProvider(Jaxb2Marshaller jaxb2Marshaller) {
            this.jaxb2Marshaller = jaxb2Marshaller;
        }

        @Override
        public JAXBContext getContext(Class<?> type) {
            if (jaxb2Marshaller != null && type != null && jaxb2Marshaller.supports(type)) {
                return jaxb2Marshaller.getJaxbContext();
            }
            return null;
        }
    }
    
}