/**
 * 
 */
package org.bremersee.common.spring.autoconfigure;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author Christian Bremer
 *
 */
@Configuration
@ConditionalOnClass(name = {"org.springframework.http.converter.xml.MarshallingHttpMessageConverter"})
@ConditionalOnWebApplication
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {
    
    @Autowired
    @Qualifier("jaxbMarshaller")
    protected Jaxb2Marshaller jaxbMarshaller;

    protected Jackson2ObjectMapperBuilderCustomizer c;

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(marshallingHttpMessageConverter());
    }
    
    @Bean
    @Primary
    public MarshallingHttpMessageConverter marshallingHttpMessageConverter() {
        return new MarshallingHttpMessageConverter(jaxbMarshaller, jaxbMarshaller);
    }

//    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter () {
//        return null;
//    }

}
