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

package org.bremersee.common.business;

import java.io.Serializable;

/**
 * @author Christian Bremer
 */
public interface RoleNameService {

    /**
     * The bit mask value of a normal role.
     */
    @SuppressWarnings("PointlessBitwiseExpression")
    int NORMAL_ROLE_MASK_VALUE = 1 << 0;

    /**
     * The bit mask value of a friends role.
     */
    int FRIENDS_ROLE_MASK_VALUE = 1 << 1;

    /**
     * The bit mask value of a custom role.
     */
    int CUSTOM_ROLE_MASK_VALUE = 1 << 2;

    /**
     * The bit mask value of an owner role.
     */
    int OWNER_ROLE_MASK_VALUE = 1 << 3;

    /**
     * The bit mask value of a manager role.
     */
    int MANAGER_ROLE_MASK_VALUE = 1 << 4;


    /**
     * The role name prefix of a custom role.
     */
    String CUSTOM_ROLE_PREFIX = "ROLE_CUSTOM_OF_";

    /**
     * The role name prefix of a friends role.
     */
    String FRIENDS_ROLE_PREFIX = "ROLE_FRIENDS_OF_";

    /**
     * The role name prefix of an owner role.
     */
    String OWNER_ROLE_PREFIX = "ROLE_OWNER_OF_";

    /**
     * The role name prefix of a manager role.
     */
    String MANAGER_ROLE_PREFIX = "ROLE_MANAGER_OF_";


    boolean isValidNormalRoleName(String roleName);

    boolean isCustomRoleName(String roleName, String userName);

    String createCustomRoleNamePrefix(String userName);

    String generateCustomRoleName(String userName);

    String getUserNameFromCustomRoleName(String customRoleName);


    boolean isFriendsRoleName(String roleName);

    String createFriendsRoleName(String userName);

    String getUserNameFromFriendsRoleName(String friendsRoleName);


    boolean isOwnerRoleName(String roleName, String entityType);

    String createOwnerRoleNamePrefix(String entityType);

    String createOwnerRoleName(String entityType, Serializable entityId);

    String getEntityIdFromOwnerRoleName(String roleName, String entityType);


    boolean isManagerRoleName(String roleName, String entityType);

    String createManagerRoleNamePrefix(String entityType);

    String createManagerRoleName(String entityType, Serializable entityId);

    String getEntityIdFromManagerRoleName(String roleName, String entityType);

}
