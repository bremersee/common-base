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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.annotation.PostConstruct;
import javax.xml.bind.Marshaller;
import java.util.*;

/**
 * @author Christian Bremer
 */
@Configuration
public class JaxbAutoConfiguration {

    private static final String[] DEFAULT_JAXB_CONTEXT_PATHS = {
            org.bremersee.common.model.ObjectFactory.class.getPackage().getName(),
            org.bremersee.comparator.model.ObjectFactory.class.getPackage().getName(),
            org.bremersee.pagebuilder.model.ObjectFactory.class.getPackage().getName()
    };

    private final Logger log = LoggerFactory.getLogger(getClass());

    private List<JaxbContextPathsProvider> jaxbContextPathsProviders = new ArrayList<>();

    public static Jaxb2Marshaller createJaxbMarshaller() {
        return createJaxbMarshaller(new ArrayList<>());
    }

    public static Jaxb2Marshaller createJaxbMarshaller(final Package...packages) {
        final List<String> packageList = new ArrayList<>();
        if (packages != null) {
            for (Package p : packages) {
                if (p != null) {
                    packageList.add(p.getName());
                }
            }
        }
        return createJaxbMarshaller(packageList);
    }

    public static Jaxb2Marshaller createJaxbMarshaller(final String...packages) {
        return createJaxbMarshaller(packages == null ? new ArrayList<>() : Arrays.asList(packages));
    }

    public static Jaxb2Marshaller createJaxbMarshaller(final Collection<String> packages) {
        final Set<String> packageSet = createPackageSet(packages);
        Jaxb2Marshaller m = new Jaxb2Marshaller();
        Map<String, Object> marshallerProperties = new HashMap<>();
        marshallerProperties.put(Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
        m.setMarshallerProperties(marshallerProperties);
        m.setContextPaths(packageSet.toArray(new String[packageSet.size()]));
        return m;
    }

    private static Set<String> createPackageSet(Collection<String> packages) {
        final Set<String> packageSet = new LinkedHashSet<>(Arrays.asList(DEFAULT_JAXB_CONTEXT_PATHS));
        if (packages != null) {
            packageSet.addAll(packages);
        }
        return packageSet;
    }

    @Autowired(required = false)
    public void setJaxbContextPathsProviders(List<JaxbContextPathsProvider> jaxbContextPathsProviders) {
        if (jaxbContextPathsProviders != null) {
            this.jaxbContextPathsProviders = jaxbContextPathsProviders;
        }
    }

    @PostConstruct
    public void init() {
        // @formatter:off
        log.info("\n"
               + "**********************************************************************\n"
               + "*  JAXB Auto Configuration                                           *\n"
               + "**********************************************************************\n"
               + "jaxbContextPaths = " + createPackageSet(getJaxbContextPaths()) + "\n"
               + "**********************************************************************");
        // @formatter:on
    }

    private List<String> getJaxbContextPaths() {

        List<String> contextPaths = new ArrayList<>();
        for (JaxbContextPathsProvider provider : jaxbContextPathsProviders) {
            if (provider.getContextPaths() != null && provider.getContextPaths().length > 0) {
                final String[] providerPaths = provider.getContextPaths();
                contextPaths.addAll(Arrays.asList(providerPaths));
            }
        }
        return contextPaths;
    }

    @Bean(name = { "jaxbMarshaller", "jaxbUnmarshaller" })
    @Primary
    public Jaxb2Marshaller jaxbMarshaller() {
        List<String> contextPaths = new ArrayList<>();
        for (JaxbContextPathsProvider provider : jaxbContextPathsProviders) {
            if (provider.getContextPaths() != null && provider.getContextPaths().length > 0) {
                final String[] providerPaths = provider.getContextPaths();
                contextPaths.addAll(Arrays.asList(providerPaths));
            }
        }
        return createJaxbMarshaller(contextPaths);
    }
    
}
