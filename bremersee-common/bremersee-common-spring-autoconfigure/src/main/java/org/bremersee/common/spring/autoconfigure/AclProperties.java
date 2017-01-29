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

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Christian Bremer
 */
@ConfigurationProperties("bremersee.acl")
@Data
@NoArgsConstructor
public class AclProperties {

    public static final String ROLE_ACL_ADMIN = "ROLE_ACL_ADMIN";

    private String auditLoggerName = "org.bremersee.acl.AuditLogger";

    private String defaultRolePrefix = "ROLE_";

    private String takeOwnershipRole = ROLE_ACL_ADMIN;

    private String modifyAuditingRole = ROLE_ACL_ADMIN;

    private String generalChangesRole = ROLE_ACL_ADMIN;

    private String classIdentityQuery = "select currval(pg_get_serial_sequence('acl_class', 'id'))";

    private String sidIdentityQuery = "select currval(pg_get_serial_sequence('acl_sid', 'id'))";

    private String aclCacheName = "aclCache";

}
