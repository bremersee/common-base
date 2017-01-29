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

package org.bremersee.common.security.acls.model;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.acls.domain.IdentityUnavailableException;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityGenerator;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bremer
 */
public class CommonObjectIdentityRetrievalStrategy implements ObjectIdentityRetrievalStrategy, ObjectIdentityGenerator {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected List<ObjectIdentityRetrievalStrategyInterceptor> interceptors = new ArrayList<>();

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [interceptors (size) = " + interceptors.size() + "]";
    }

    public void setInterceptors(final List<ObjectIdentityRetrievalStrategyInterceptor> interceptors) {
        if (interceptors != null) {
            this.interceptors = interceptors;
        }
    }

    @Override
    public ObjectIdentity createObjectIdentity(final Serializable id, final String type) {
        if (log.isDebugEnabled()) {
            log.debug("Creating object identity for id [{}] and type [{}] ...", id, type);
        }
        Validate.notNull(id, "ID must not be null.");
        Validate.notBlank(type, "Type must not be null or blank.");
        ObjectIdentity objectIdentity = null;
        ObjectIdentityRetrievalStrategyInterceptor interceptor = findInterceptorByType(type);
        if (interceptor != null) {
            objectIdentity = interceptor.createObjectIdentity(id, type);
        }
        if (objectIdentity == null) {
            objectIdentity = new ObjectIdentityImpl(type, id.toString());
        }
        if (log.isDebugEnabled()) {
            log.debug("Object identity for id [{}] and type [{}] created: {}", id, type, objectIdentity);
        }
        return objectIdentity;
    }

    protected ObjectIdentityRetrievalStrategyInterceptor findInterceptorByType(final String type) {
        final String clearType = ObjectIdentityUtils.getType(type);
        if (log.isDebugEnabled()) {
            log.debug("Looking for an object identity interceptor that supports type [{}] (original type [{}]) ...",
                    clearType, type);
        }
        for (ObjectIdentityRetrievalStrategyInterceptor interceptor : interceptors) {
            if (interceptor.supportsType(clearType)) {
                if (log.isDebugEnabled()) {
                    log.debug("Object identity interceptor that supports type [{}] (original type [{}]) was found.",
                            clearType, type);
                }
                return interceptor;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Object identity interceptor that supports type [{}] (original type [{}]) was not found.",
                    clearType, type);
        }
        return null;
    }

    @Override
    public ObjectIdentity getObjectIdentity(final Object domainObject) {
        if (domainObject == null) {
            throw new IdentityUnavailableException("Domain object must be null.");
        }
        if (log.isDebugEnabled()) {
            log.debug("Creating object identity for domain object [{}] ...", domainObject.getClass().getName());
        }
        ObjectIdentity objectIdentity = null;
        ObjectIdentityRetrievalStrategyInterceptor interceptor = findInterceptorByDomainObject(domainObject);
        if (interceptor != null) {
            objectIdentity = interceptor.getObjectIdentity(domainObject);
        }
        if (objectIdentity == null) {
            objectIdentity = new ObjectIdentityImpl(domainObject.getClass().getName(), getId(domainObject));
        }
        if (log.isDebugEnabled()) {
            log.debug("Object identity for domain object created: {}", objectIdentity);
        }
        return objectIdentity;
    }

    protected ObjectIdentityRetrievalStrategyInterceptor findInterceptorByDomainObject(final Object domainObject) {
        final String className = domainObject == null ? null : domainObject.getClass().getName();
        if (log.isDebugEnabled()) {
            log.debug("Looking for an object identity interceptor that supports domain object [{}] ...",
                    className);
        }
        for (ObjectIdentityRetrievalStrategyInterceptor interceptor : interceptors) {
            if (interceptor.supportsDomainObject(domainObject)) {
                if (log.isDebugEnabled()) {
                    log.debug("Object identity interceptor that supports domain object [{}] was found.",
                            className);
                }
                return interceptor;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Object identity interceptor that supports domain object [{}] was not found.",
                    className);
        }
        return null;
    }

    public static String getId(final Object object) {
        Validate.notNull(object, "Object cannot be null.");

        Class<?> typeClass = ClassUtils.getUserClass(object.getClass());

        Object result;

        try {
            @SuppressWarnings("RedundantArrayCreation")
            Method method = typeClass.getMethod("getId", new Class[]{});
            result = method.invoke(object);
        } catch (Exception e) {
            throw new IdentityUnavailableException(
                    "Could not extract identity from object " + object, e);
        }

        Assert.notNull(result, "getId() is required to return a non-null value");
        return result.toString();
    }

}
