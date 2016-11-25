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
import com.googlecode.jmapper.annotations.JMap;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Christian Bremer
 *
 */
//@formatter:off
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "stackTraceElementType", propOrder = {
        "declaringClass",
        "methodName",
        "fileName",
        "lineNumber"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
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
@Data
@AllArgsConstructor
//@formatter:on
public class StackTraceElementDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(name = "declaringClass", required = false)
    @JsonProperty(value = "declaringClass", required = false)
    @JMap
    private String declaringClass;
    
    @XmlElement(name = "methodName", required = false)
    @JsonProperty(value = "methodName", required = false)
    @JMap
    private String methodName;
    
    @XmlElement(name = "fileName", required = false)
    @JsonProperty(value = "fileName", required = false)
    @JMap
    private String fileName;
    
    @XmlElement(name = "lineNumber", defaultValue = "0")
    @JsonProperty(value = "lineNumber", defaultValue = "0")
    @JMap
    private int    lineNumber;

    public StackTraceElement toStackTraceElement() {
        return new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
    }

}
