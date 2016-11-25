/*
 * Copyright 2015 the original author or authors.
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

/**
 * @author Christian Bremer
 *
 */
public abstract class RestConstants {

    public static final String REST_CONTEXT_PATH = "/api";
    
    public static final String ANT_RESOURCE_PATH = REST_CONTEXT_PATH + "/**";
    
    public static final String SECURITY_SCHEMA_OAUTH2 = "oauth2";
    
    public static final String AUTHORIZATION_SCOPE = "swaggerui";
    
    public static final String AUTHORIZATION_SCOPE_DESCR ="SwaggerUI may read and write. It depends on the user and the granted authorities.";

    /**
     * Never construct.
     */
    private RestConstants() {
    }

}
