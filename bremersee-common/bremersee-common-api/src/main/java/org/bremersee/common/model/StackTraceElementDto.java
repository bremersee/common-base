/**
 * 
 */
package org.bremersee.common.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Christian Bremer
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "stackTraceElementType", propOrder = {
        "declaringClass",
        "methodName",
        "fileName",
        "lineNumber"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.ALWAYS)
@JsonAutoDetect(
        fieldVisibility = Visibility.ANY, 
        getterVisibility = Visibility.NONE, 
        creatorVisibility = Visibility.NONE, 
        isGetterVisibility = Visibility.NONE, 
        setterVisibility = Visibility.NONE
)
@JsonPropertyOrder({
        "declaringClass",
        "methodName",
        "fileName",
        "lineNumber"
})
public class StackTraceElementDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(name = "declaringClass", required = false)
    @JsonProperty(value = "declaringClass", required = false)
    private String declaringClass;
    
    @XmlElement(name = "methodName", required = false)
    @JsonProperty(value = "methodName", required = false)
    private String methodName;
    
    @XmlElement(name = "fileName", required = false)
    @JsonProperty(value = "fileName", required = false)
    private String fileName;
    
    @XmlElement(name = "lineNumber", defaultValue = "0")
    @JsonProperty(value = "lineNumber", defaultValue = "0")
    private int    lineNumber;

    /**
     * Default constructor.
     */
    public StackTraceElementDto() {
    }

    public StackTraceElementDto(String declaringClass, String methodName,
            String fileName, int lineNumber) {
        this.declaringClass = declaringClass;
        this.methodName = methodName;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return "StackTraceElementDto [declaringClass=" + declaringClass
                + ", methodName=" + methodName + ", fileName=" + fileName
                + ", lineNumber=" + lineNumber + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((declaringClass == null) ? 0 : declaringClass.hashCode());
        result = prime * result
                + ((fileName == null) ? 0 : fileName.hashCode());
        result = prime * result + lineNumber;
        result = prime * result
                + ((methodName == null) ? 0 : methodName.hashCode());
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
        StackTraceElementDto other = (StackTraceElementDto) obj;
        if (declaringClass == null) {
            if (other.declaringClass != null)
                return false;
        } else if (!declaringClass.equals(other.declaringClass))
            return false;
        if (fileName == null) {
            if (other.fileName != null)
                return false;
        } else if (!fileName.equals(other.fileName))
            return false;
        if (lineNumber != other.lineNumber)
            return false;
        if (methodName == null) {
            if (other.methodName != null)
                return false;
        } else if (!methodName.equals(other.methodName))
            return false;
        return true;
    }

    public StackTraceElement toStackTraceElement() {
        return new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
    }

    public String getDeclaringClass() {
        return declaringClass;
    }

    public void setDeclaringClass(String declaringClass) {
        this.declaringClass = declaringClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
}
