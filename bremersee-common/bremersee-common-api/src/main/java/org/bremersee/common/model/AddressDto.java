/**
 * 
 */
package org.bremersee.common.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.googlecode.jmapper.annotations.JMap;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Christian Bremer
 *
 */
//@formatter:off
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "address")
@XmlType(name = "addressType", propOrder = {
        "streetAddress",
        "streetNumber",
        "postalCode",
        "city",
        "cityCode",
        "sublocality",
        "state",
        "stateCode",
        "country",
        "countryCode"
})
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
@Getter @Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
//@formatter:on
public class AddressDto extends AbstractBaseDto {

    private static final long serialVersionUID = 1L;
    
    @JsonProperty(required = false)
    @JMap
    private String streetAddress; // google: route, street_number (long_name / short_name)
    
    @JsonProperty(required = false)
    @JMap
    private String streetNumber;
    
    @JsonProperty(required = false)
    @JMap
    private String postalCode; // google: postal_code (long_name / short_name)
    
    @JsonProperty(required = false)
    @JMap
    private String city; // google: locality (long_name / short_name)
    
    @JsonProperty(required = false)
    @JMap
    private String cityCode; // google: locality (long_name / short_name)
    
    @JsonProperty(required = false)
    @JMap
    private String sublocality; // (eg Eixe) (long_name / short_name)
    
    @JsonProperty(required = false)
    @JMap
    private String state; // google: administrative_area_level_1 (long_name / short_name)
    
    @JsonProperty(required = false)
    @JMap
    private String stateCode; // google: administrative_area_level_1 (long_name / short_name)
    
    @JsonProperty(required = false)
    @JMap
    private String country; // google: country (long_name / short_name)
    
    @JsonProperty(required = false)
    @JMap
    private String countryCode; // google: country (long_name / short_name)
    
    // google: "formatted_address" : "Hauptstraße 26, 31228 Peine, Deutschland"

}
