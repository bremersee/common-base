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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Bremer
 *
 */
@Configuration
@ConditionalOnProperty(
        prefix = "bremersee.swagger-ui", 
        name = "base-package")
@ConditionalOnClass(name = { 
        "springfox.documentation.spring.web.plugins.Docket",
        "springfox.documentation.swagger2.annotations.EnableSwagger2" 
})
@ConditionalOnWebApplication
@EnableConfigurationProperties({ SwaggerProperties.class })
public class SwaggerAutoConfiguration {
    
    @Configuration
    @EnableSwagger2
    public static class EnableSwagger {
    }
    
    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private Environment env;
    
    @Autowired
    private SwaggerProperties properties;
    
    private String getUserAuthorizationUri() {
        return env.getProperty("security.oauth2.client.user-authorization-uri", "");
    }
    
    private String getClientId() {
        return env.getProperty("security.oauth2.client.client-id", "");
    }
    
    @PostConstruct
    public void init() {
        // @formatter:off
        log.info("\n"
               + "**********************************************************************\n"
               + "*  Swagger Auto Configuration                                        *\n"
               + "**********************************************************************\n"
               + "properties = " + properties + "\n"
               + "userAuthorizationUri = " + getUserAuthorizationUri() + "\n"
               + "clientId = " + getClientId() + "\n"
               + "**********************************************************************");
        // @formatter:on
    }

    @Bean
    public Docket swaggerUiDocket() {
        //@formatter:off
        return new Docket(DocumentationType.SWAGGER_2)
                //.groupName("role-group")
                .apiInfo(new ApiInfoBuilder()
                        .title(properties.getTitle())
                        .description(properties.getDescription())
                        .version(properties.getVersion())
                        .build())
                .select()
                .apis(StringUtils.isBlank(properties.getBasePackage()) ? RequestHandlerSelectors.any() : RequestHandlerSelectors.basePackage(properties.getBasePackage()))
                .paths(StringUtils.isBlank(properties.getAntPath()) ? PathSelectors.any() : PathSelectors.ant(properties.getAntPath()))
                .build()
                .securitySchemes(Collections.singletonList(implicitFlow()))
                .securityContexts(Collections.singletonList(securityContext()));
        //@formatter:on
    }
    
    private AuthorizationScope getAuthorizationScope() {
        return new AuthorizationScope(RestConstants.AUTHORIZATION_SCOPE, RestConstants.AUTHORIZATION_SCOPE);
    }

    private OAuth implicitFlow() {

        return new OAuth(
                RestConstants.SECURITY_SCHEMA_OAUTH2,
                Collections.singletonList(getAuthorizationScope()),
                Collections.singletonList(
                        new ImplicitGrant(
                                new LoginEndpoint(getUserAuthorizationUri()), 
                                "access_token")
//                        new AuthorizationCodeGrant(
//                                new TokenRequestEndpoint("http://localhost:9000/oauth/authorize", "bremersee", "secret"),
//                                new TokenEndpoint("http://bremersee:secret@localhost:9000/oauth/token", "access_code"))
                ));
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.ant(RestConstants.ANT_RESOURCE_PATH))
                .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope
                = new AuthorizationScope(RestConstants.AUTHORIZATION_SCOPE, RestConstants.AUTHORIZATION_SCOPE_DESCR);
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Collections.singletonList(
                new SecurityReference(RestConstants.SECURITY_SCHEMA_OAUTH2, authorizationScopes));
    }

    @Bean
    public SecurityConfiguration swaggerUiSecurityConfiguration() {
        String clientId = getClientId();
        String clientSecret = "";
        String realm = "oauth2/client";
        String appName = "";
        String apiKeyValue = "";
        ApiKeyVehicle apiKeyVehicle = ApiKeyVehicle.HEADER;
        String apiKeyName = "";
        String scopeSeparator = ",";
        return new SecurityConfiguration(
                clientId,
                clientSecret,
                realm,
                appName,
                apiKeyValue,
                apiKeyVehicle,
                apiKeyName,
                scopeSeparator);
    }
    
}
