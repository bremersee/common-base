/**
 * 
 */
package org.bremersee.common.model;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Christian Bremer
 *
 */
//@formatter:off
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "throwable")
@XmlType(name = "throwableType", propOrder = {
        "className",
        "message",
        "stackTrace",
        "cause"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonAutoDetect(
        fieldVisibility = Visibility.NONE, 
        getterVisibility = Visibility.PROTECTED_AND_PUBLIC, 
        creatorVisibility = Visibility.NONE, 
        isGetterVisibility = Visibility.PROTECTED_AND_PUBLIC, 
        setterVisibility = Visibility.PROTECTED_AND_PUBLIC
)
@JsonPropertyOrder({
        "className",
        "message",
        "stackTrace",
        "cause"
})
//@formatter:on
public class ThrowableDto implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @XmlElement(name = "className", required = false)
    private String className;
    
    @XmlElement(name = "message", required = false)
    private String message;
    
    @XmlElementWrapper(name = "stackTrace", required = false)
    @XmlElement(name = "stackTraceElement", required = false)
    private List<StackTraceElementDto> stackTrace = new ArrayList<>();

    @XmlElement(name = "cause", required = false)
    private ThrowableDto cause;
    
    /**
     * Default constructor. 
     */
    public ThrowableDto() {
    }

    public ThrowableDto(Throwable t) {
        if (t != null) {
            this.className = t.getClass().getName();
            this.message = t.getMessage();
            StackTraceElement[] stackTrace = t.getStackTrace();
            if (stackTrace != null) {
                for (StackTraceElement elem : stackTrace) {
                    this.stackTrace.add(new StackTraceElementDto(elem.getClassName(), elem.getMethodName(), elem.getFileName(), elem.getLineNumber()));
                }
            }
            if (t.getCause() != null) {
                this.cause = new ThrowableDto(t.getCause());
            }
        }
    }
    
    @Override
    public String toString() {
        return "ThrowableDto [className=" + className + ", message=" + message
                + ", stackTrace=" + stackTrace + ", cause=" + cause + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cause == null) ? 0 : cause.hashCode());
        result = prime * result
                + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result
                + ((stackTrace == null) ? 0 : stackTrace.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ThrowableDto other = (ThrowableDto) obj;
        if (cause == null) {
            if (other.cause != null)
                return false;
        } else if (!cause.equals(other.cause))
            return false;
        if (className == null) {
            if (other.className != null)
                return false;
        } else if (!className.equals(other.className))
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (stackTrace == null) {
            if (other.stackTrace != null)
                return false;
        } else if (!stackTrace.equals(other.stackTrace))
            return false;
        return true;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Throwable> findClass()  {
        try {
            Class<?> cls = Class.forName(className);
            if (Throwable.class.isAssignableFrom(cls)) {
                return (Class<? extends Throwable>) cls;
            } else {
                return Throwable.class;
            }
        } catch (ClassNotFoundException | RuntimeException e) {
            return Throwable.class;
        }
    }
    
    @SuppressWarnings("unchecked")
    private Throwable findThrowable(Class<? extends Throwable> cls) {
        while (Modifier.isAbstract(cls.getModifiers())) {
            cls = (Class<? extends Throwable>) cls.getSuperclass();
        }
        try {
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
    }
    
    @SuppressWarnings("unchecked")
    private void putMessage(Throwable t, Class<? extends Throwable> cls) {
        try {
            Field f = cls.getDeclaredField("detailMessage");
            if (!f.isAccessible()) {
                f.setAccessible(true);
            }
            f.set(t, message);
            
        } catch (NoSuchFieldException e) {
            
            Class<?> superCls = cls.getSuperclass();
            if (Throwable.class.isAssignableFrom(superCls)) {
                putMessage(t, (Class<? extends Throwable>)superCls);
            } else {
                throw new RuntimeException("Field [detailMessage] was not found on class [" + cls.getName() + "].");
            }

        } catch (SecurityException | IllegalAccessException e) {
            throw new RuntimeException("Field [detailMessage] cannot be set on class [" + cls.getName() + "].", e);
        }
    }
    
    public Throwable toThrowable() {
        Class<? extends Throwable> cls = findClass();
        Throwable t = findThrowable(cls);
        StackTraceElement[] elems = new StackTraceElement[stackTrace.size()];
        int i = 0;
        for (StackTraceElementDto dto : stackTrace) {
            elems[i] = dto.toStackTraceElement();
            i++;
        }
        t.setStackTrace(elems);
        return t;
    }
    
    @JsonProperty(value = "className", required = false)
    public String getClassName() {
        return className;
    }

    @JsonProperty(value = "className", required = false)
    public void setClassName(String className) {
        this.className = className;
    }

    @JsonProperty(value = "message", required = false)
    public String getMessage() {
        return message;
    }

    @JsonProperty(value = "message", required = false)
    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty(value = "stackTrace", required = false)
    public List<StackTraceElementDto> getStackTrace() {
        return stackTrace;
    }

    @JsonProperty(value = "stackTrace", required = false)
    public void setStackTrace(List<StackTraceElementDto> stackTrace) {
        if (stackTrace == null) {
            stackTrace = new ArrayList<>();
        }
        this.stackTrace = stackTrace;
    }

    @JsonProperty(value = "cause", required = false)
    public ThrowableDto getCause() {
        return cause;
    }

    @JsonProperty(value = "cause", required = false)
    public void setCause(ThrowableDto cause) {
        this.cause = cause;
    }
    
}
