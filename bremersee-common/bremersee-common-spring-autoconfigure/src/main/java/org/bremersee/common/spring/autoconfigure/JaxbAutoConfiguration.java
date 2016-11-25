/**
 * 
 */
package org.bremersee.common.spring.autoconfigure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.util.StringUtils;

/**
 * @author Christian Bremer
 *
 */
@Configuration
public class JaxbAutoConfiguration {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired(required = false)
    protected List<JaxbContextPathsProvider> jaxbContextPathsProviders = new ArrayList<JaxbContextPathsProvider>();

    @PostConstruct
    public void init() {
        // @formatter:off
        log.info("\n"
               + "**********************************************************************\n"
               + "*  JAXB Auto Configuration                                           *\n"
               + "**********************************************************************\n"
               + "jaxbContextPaths = " + StringUtils.arrayToCommaDelimitedString(getJaxbContextPaths()) + "\n"
               + "**********************************************************************");
        // @formatter:on
    }

    protected String[] getJaxbContextPaths() {
        
        Set<String> paths = new HashSet<String>();
        
        // defaults
        paths.add(org.bremersee.common.model.ObjectFactory.class.getPackage().getName());
        paths.add(org.bremersee.comparator.model.ObjectFactory.class.getPackage().getName());
        paths.add(org.bremersee.pagebuilder.model.ObjectFactory.class.getPackage().getName());
        
        if (jaxbContextPathsProviders != null) {
            for (JaxbContextPathsProvider provider : jaxbContextPathsProviders) {
                String[] providerPaths = provider.getContextPaths();
                if (providerPaths != null) {
                    for (String providerPath : providerPaths) {
                        paths.add(providerPath);
                    }
                }
            }
        }
        return paths.toArray(new String[paths.size()]);
    }

    @Bean(name = { "jaxbMarshaller", "jaxbUnmarshaller" })
    @Primary
    public Jaxb2Marshaller jaxbMarshaller() {
        Jaxb2Marshaller m = new Jaxb2Marshaller();
        Map<String, Object> marshallerProperties = new HashMap<String, Object>();
        marshallerProperties.put(Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
        m.setMarshallerProperties(marshallerProperties);
        m.setContextPaths(getJaxbContextPaths());
        return m;
    }
    
}
