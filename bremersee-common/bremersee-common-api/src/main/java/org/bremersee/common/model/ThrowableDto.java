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

package org.bremersee.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bremersee.common.exception.StatusCodeAwareException;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A DTO of a throwable object.
 *
 * @author Christian Bremer
 */
//@formatter:off
@ApiModel("A DTO of a throwable object.")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "throwable")
@XmlType(name = "throwableType", propOrder = {
        "className",
        "message",
        "stackTrace",
        "cause",
        "statusCode"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({
        "className",
        "message",
        "stackTrace",
        "cause",
        "statusCode"
})
@Data
@NoArgsConstructor
//@formatter:on
public class ThrowableDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(name = "className")
    @JsonProperty(value = "className")
    private String className;

    @XmlElement(name = "message")
    @JsonProperty(value = "message")
    private String message;

    @XmlElementWrapper(name = "stackTrace")
    @XmlElement(name = "stackTraceElement")
    @JsonProperty(value = "stackTrace")
    private List<StackTraceElementDto> stackTrace = new ArrayList<>();

    @XmlElement(name = "cause")
    @JsonProperty(value = "cause")
    private ThrowableDto cause;

    @XmlElement(name = "statusCode")
    @JsonProperty(value = "statusCode")
    private Integer statusCode;

    /**
     * Creates the DTO of the specified throwable.
     *
     * @param throwable the source object
     */
    public ThrowableDto(Throwable throwable) {
        if (throwable != null) {
            this.className = throwable.getClass().getName();
            this.message = throwable.getMessage();
            StackTraceElement[] stackTraceElements = throwable.getStackTrace();
            if (stackTraceElements != null) {
                for (StackTraceElement elem : stackTraceElements) {
                    this.stackTrace.add(new StackTraceElementDto(
                            elem.getClassName(),
                            elem.getMethodName(),
                            elem.getFileName(),
                            elem.getLineNumber()));
                }
            }
            if (throwable.getCause() != null) {
                this.cause = new ThrowableDto(throwable.getCause());
            }
            if (throwable instanceof StatusCodeAwareException) {
                this.statusCode = ((StatusCodeAwareException) throwable).getStatusCode();
            }
        }
    }

    /**
     * Creates the throwable object of this DTO.
     *
     * @return the throwable object
     */
    public Throwable toThrowable() {
        Class<? extends Throwable> cls = findClass();
        Throwable throwable = findThrowable(cls);
        StackTraceElement[] stackTraceElements = new StackTraceElement[stackTrace.size()];
        int i = 0;
        for (StackTraceElementDto dto : stackTrace) {
            stackTraceElements[i] = dto.toStackTraceElement();
            i++;
        }
        throwable.setStackTrace(stackTraceElements);
        if (statusCode != null && throwable instanceof StatusCodeAwareException) {
            try {
                Method setter = throwable.getClass().getDeclaredMethod("setStatusCode", Integer.class);
                if (!setter.isAccessible()) {
                    setter.setAccessible(true);
                }
                setter.invoke(throwable, statusCode);
            } catch (Exception e) { // NOSONAR
                // ignored
            }
        }
        return throwable;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Throwable> findClass() {
        try {
            Class<?> cls = Class.forName(className);
            if (Throwable.class.isAssignableFrom(cls)) {
                return (Class<? extends Throwable>) cls;
            } else {
                return Throwable.class;
            }
        } catch (ClassNotFoundException | RuntimeException e) { // NOSONAR
            return Throwable.class;
        }
    }

    @SuppressWarnings("unchecked")
    private Throwable findThrowable(Class<? extends Throwable> clazz) {
        Class<? extends Throwable> cls = clazz;
        while (Modifier.isAbstract(cls.getModifiers())) {
            cls = (Class<? extends Throwable>) cls.getSuperclass();
        }

        Throwable t = findThrowableByStringAndThrowable(cls);
        if (t != null) {
            return t;
        }

        t = findThrowableByString(cls);
        if (t != null) {
            return t;
        }

        t = findThrowableByThrowable(cls);
        if (t != null) {
            return t;
        }

        t = findThrowableByNoArgs(cls);
        if (t != null) {
            return t;
        }

        try {
            Class<?> superCls = cls.getSuperclass();
            if (Throwable.class.isAssignableFrom(superCls)) {
                return findThrowable((Class<? extends Throwable>) superCls);
            } else {
                throw new RuntimeException("No suitable constructor for class [" + cls.getName() + "] was found."); // NOSONAR
            }

        } catch (Exception e) {
            throw new RuntimeException("No suitable constructor for class [" + cls.getName() + "] was found.", e); // NOSONAR
        }

        /*
        try {  // NOSONAR
            Constructor<? extends Throwable> constructor;
            try {
                constructor = cls.getDeclaredConstructor(String.class, Throwable.class);
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                return constructor.newInstance(message, cause == null ? null : cause.toThrowable());

            } catch (NoSuchMethodException ignored0) {

                try {
                    constructor = cls.getDeclaredConstructor(String.class);
                    if (!constructor.isAccessible()) {
                        constructor.setAccessible(true);
                    }
                    Throwable t = constructor.newInstance(message);
                    if (cause != null) {
                        t.initCause(cause.toThrowable());
                    }
                    return t;

                } catch (NoSuchMethodException ignored1) {

                    try {
                        constructor = cls.getDeclaredConstructor(Throwable.class);
                        if (!constructor.isAccessible()) {
                            constructor.setAccessible(true);
                        }
                        Throwable t = constructor.newInstance(cause == null ? null : cause.toThrowable());
                        putMessage(t, cls);
                        return t;

                    } catch (NoSuchMethodException ignored2) {

                        constructor = cls.getDeclaredConstructor();
                        if (!constructor.isAccessible()) {
                            constructor.setAccessible(true);
                        }
                        Throwable t = constructor.newInstance();
                        putMessage(t, cls);
                        if (cause != null) {
                            t.initCause(cause.toThrowable());
                        }
                        return t;
                    }
                }
            }

        } catch (NoSuchMethodException e) {
            Class<?> superCls = cls.getSuperclass();
            if (Throwable.class.isAssignableFrom(superCls)) {
                return findThrowable((Class<? extends Throwable>)superCls);
            } else {
                throw new RuntimeException("No suitable constructor for class [" + cls.getName() + "] was found.");
            }

        } catch (Throwable t) {
            throw new RuntimeException("No suitable constructor for class [" + cls.getName() + "] was found.", t);
        }
        */
    }

    private Throwable findThrowableByStringAndThrowable(Class<? extends Throwable> cls) {
        try {
            Constructor<? extends Throwable> constructor = cls.getDeclaredConstructor(String.class, Throwable.class);
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return constructor.newInstance(message, cause == null ? null : cause.toThrowable());
        } catch (Exception e) { // NOSONAR
            return null;
        }
    }

    private Throwable findThrowableByString(Class<? extends Throwable> cls) {
        try {
            Constructor<? extends Throwable> constructor = cls.getDeclaredConstructor(String.class);
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            Throwable t = constructor.newInstance(message);
            if (cause != null) {
                t.initCause(cause.toThrowable());
            }
            return t;
        } catch (Exception e) { // NOSONAR
            return null;
        }
    }

    private Throwable findThrowableByThrowable(Class<? extends Throwable> cls) {
        try {
            Constructor<? extends Throwable> constructor = cls.getDeclaredConstructor(Throwable.class);
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            Throwable t = constructor.newInstance(cause == null ? null : cause.toThrowable());
            putMessage(t, cls);
            return t;
        } catch (Exception e) { // NOSONAR
            return null;
        }
    }

    private Throwable findThrowableByNoArgs(Class<? extends Throwable> cls) {
        try {
            Constructor<? extends Throwable> constructor = cls.getDeclaredConstructor();
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            Throwable t = constructor.newInstance();
            putMessage(t, cls);
            if (cause != null) {
                t.initCause(cause.toThrowable());
            }
            return t;
        } catch (Exception e) { // NOSONAR
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void putMessage(Throwable t, Class<? extends Throwable> cls) {
        try {
            Field f = cls.getDeclaredField("detailMessage");
            if (!f.isAccessible()) {
                f.setAccessible(true);
            }
            f.set(t, message);

        } catch (NoSuchFieldException e) { // NOSONAR

            Class<?> superCls = cls.getSuperclass();
            if (Throwable.class.isAssignableFrom(superCls)) {
                putMessage(t, (Class<? extends Throwable>) superCls);
            } else {
                throw new RuntimeException("Field [detailMessage] was not found on class [" + cls.getName() + "]."); // NOSONAR
            }

        } catch (SecurityException | IllegalAccessException e) {
            throw new RuntimeException("Field [detailMessage] cannot be set on class [" + cls.getName() + "].", e); // NOSONAR
        }
    }

}
