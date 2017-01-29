/*
 * Copyright 2017 the original author or authors.
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

import org.apache.commons.lang3.StringUtils;
import org.bremersee.common.exception.BadRequestException;
import org.bremersee.utils.PasswordUtils;

import java.io.Serializable;

/**
 * @author Christian Bremer
 */
public class RoleNameServiceImpl implements RoleNameService {

    @Override
    public boolean isCustomRoleName(final String roleName, final String userName) {
        if (StringUtils.isBlank(userName)) {
            return roleName != null && roleName.startsWith(CUSTOM_ROLE_PREFIX);
        }
        return roleName != null && roleName.startsWith(createCustomRoleNamePrefix(userName));
    }

    @Override
    public String createCustomRoleNamePrefix(String userName) {
        BadRequestException.validateNotBlank(userName, "User name must be present.");
        return CUSTOM_ROLE_PREFIX_TEMPLATE.replace(USER_PLACE_HOLDER, userName);
    }

    @Override
    public String generateCustomRoleName(final String userName) {
        int randomStrLength = 130 - (CUSTOM_ROLE_PREFIX.length() + userName.length() + 1);
        // ROLE_CUSTOM_OF_username_XYZ
        String randomStr = PasswordUtils.createRandomClearPassword(randomStrLength,
                false, false).replace('_', '8');
        return createCustomRoleNamePrefix(userName) + randomStr;
    }

    @Override
    public String getUserNameFromCustomRoleName(final String customRoleName) {
        final int index = customRoleName.lastIndexOf('_');
        if (isCustomRoleName(customRoleName, null) && index > CUSTOM_ROLE_PREFIX.length()) {
            // ROLE_CUSTOM_OF_username_XYZ
            return customRoleName.substring(CUSTOM_ROLE_PREFIX.length(), index);
        }
        return null;
    }

    @Override
    public boolean isFriendsRoleName(final String roleName) {
        return roleName != null && roleName.startsWith(FRIENDS_ROLE_PREFIX);
    }

    @Override
    public String createFriendsRoleName(final String userName) {
        BadRequestException.validateNotBlank(userName, "User name must be present.");
        return FRIENDS_ROLE_PREFIX + userName;
    }

    @Override
    public String getUserNameFromFriendsRoleName(final String friendsRoleName) {
        if (isFriendsRoleName(friendsRoleName)) {
            return friendsRoleName.substring(FRIENDS_ROLE_PREFIX.length());
        }
        return null;
    }

    @Override
    public String createOwnerRoleNamePrefix(String entityType) {
        BadRequestException.validateNotBlank(entityType, "Entity type must be present."); // NOSONAR
        return OWNER_ROLE_PREFIX_TEMPLATE.replace(ENTITY_TYPE_PLACE_HOLDER, entityType.toUpperCase());
    }

    @Override
    public boolean isOwnerRoleName(final String roleName, final String entityType) {
        if (StringUtils.isBlank(entityType)) {
            return roleName != null && roleName.startsWith(OWNER_ROLE_PREFIX);
        }
        return roleName != null && roleName.startsWith(createOwnerRoleNamePrefix(entityType));
    }

    @Override
    public String createOwnerRoleName(final String entityType, final Serializable entityId) {
        BadRequestException.validateNotNull(entityId, "Entity ID must not be null.");
        return createOwnerRoleNamePrefix(entityType) + entityId;
    }

    @Override
    public String getEntityIdFromOwnerRoleName(final String roleName, final String entityType) {
        if (isOwnerRoleName(roleName, entityType)) {
            return roleName.substring(createOwnerRoleNamePrefix(entityType).length());
        }
        return null;
    }

    @Override
    public String createManagerRoleNamePrefix(final String entityType) {
        BadRequestException.validateNotBlank(entityType, "Entity type must be present.");
        return MANAGER_ROLE_PREFIX_TEMPLATE.replace(ENTITY_TYPE_PLACE_HOLDER, entityType.toUpperCase());
    }

    @Override
    public boolean isManagerRoleName(final String roleName, final String entityType) {
        if (StringUtils.isBlank(entityType)) {
            return roleName != null && roleName.startsWith(MANAGER_ROLE_PREFIX);
        }
        return roleName != null && roleName.startsWith(createManagerRoleNamePrefix(entityType));
    }

    @Override
    public String createManagerRoleName(final String entityType, final Serializable entityId) {
        BadRequestException.validateNotBlank(entityType, "Entity type must be present.");
        BadRequestException.validateNotNull(entityId, "Entity ID must not be null.");
        return createManagerRoleNamePrefix(entityType) + entityId;
    }

    @Override
    public String getEntityIdFromManagerRoleName(final String roleName, final String entityType) {
        if (isManagerRoleName(roleName, entityType)) {
            return roleName.substring(createManagerRoleNamePrefix(entityType).length());
        }
        return null;
    }

}
