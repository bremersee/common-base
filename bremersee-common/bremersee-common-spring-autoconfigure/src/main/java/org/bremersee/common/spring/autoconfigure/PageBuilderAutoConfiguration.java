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
