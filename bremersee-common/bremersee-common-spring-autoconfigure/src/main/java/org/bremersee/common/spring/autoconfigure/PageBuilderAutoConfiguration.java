/**
 * 
 */
package org.bremersee.common.spring.autoconfigure;

import org.bremersee.comparator.ComparatorItemTransformer;
import org.bremersee.comparator.ComparatorItemTransformerImpl;
import org.bremersee.pagebuilder.PageRequestBuilder;
import org.bremersee.pagebuilder.PageRequestBuilderImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Christian Bremer
 *
 */
@Configuration
@ConditionalOnClass(name = { "org.bremersee.comparator.ComparatorItemTransformer",
        "org.bremersee.pagebuilder.PageRequestBuilder" })
public class PageBuilderAutoConfiguration {
    
    @Bean
    @Primary
    public ComparatorItemTransformer comparatorItemTransformer() {
        return new ComparatorItemTransformerImpl();
    }

    @Bean
    @Primary
    public PageRequestBuilder pageRequestBuilder() {
        PageRequestBuilderImpl impl = new PageRequestBuilderImpl();
        impl.setComparatorItemTransformer(comparatorItemTransformer());
        return impl;
    }

}
