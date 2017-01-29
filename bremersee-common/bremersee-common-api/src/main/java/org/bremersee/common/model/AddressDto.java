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

package org.bremersee.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.xml.bind.annotation.*;

/**
 * An address model object.
 *
 * @author Christian Bremer
 */
//@formatter:off
@ApiModel(parent = AbstractBaseDto.class)
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
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
//@formatter:on
public class AddressDto extends AbstractBaseDto {

    private static final long serialVersionUID = 1L;

    @XmlElement(name = "streetAddress")
    @JsonProperty(value = "streetAddress")
    @ApiModelProperty(value = "The name of the street.")
    private String streetAddress; // google: route, street_number (long_name / short_name)

    @XmlElement(name = "streetNumber")
    @JsonProperty(value = "streetNumber")
    @ApiModelProperty(value = "The number of the building.")
    private String streetNumber;

    @XmlElement(name = "postalCode")
    @JsonProperty(value = "postalCode")
    @ApiModelProperty(value = "The postal code of the city.")
    private String postalCode; // google: postal_code (long_name / short_name)

    @XmlElement(name = "city")
    @JsonProperty(value = "city")
    @ApiModelProperty(value = "The name of the city.")
    private String city; // google: locality (long_name / short_name)

    @XmlElement(name = "cityCode")
    @JsonProperty(value = "cityCode")
    @ApiModelProperty(value = "The short name of the city.")
    private String cityCode; // google: locality (long_name / short_name)

    @XmlElement(name = "sublocality")
    @JsonProperty(value = "sublocality")
    @ApiModelProperty(value = "The name of the sublocality.")
    private String sublocality; // (eg Eixe) (long_name / short_name)

    @XmlElement(name = "state")
    @JsonProperty(value = "state")
    @ApiModelProperty(value = "The name of the state.")
    private String state; // google: administrative_area_level_1 (long_name / short_name)

    @XmlElement(name = "stateCode")
    @JsonProperty(value = "stateCode")
    @ApiModelProperty(value = "The short name of the state.")
    private String stateCode; // google: administrative_area_level_1 (long_name / short_name)

    @XmlElement(name = "country")
    @JsonProperty(value = "country")
    @ApiModelProperty(value = "The name of the country.")
    private String country; // google: country (long_name / short_name)

    @XmlElement(name = "countryCode")
    @JsonProperty(value = "countryCode")
    @ApiModelProperty(value = "The short name of the country.")
    private String countryCode; // google: country (long_name / short_name)

    // google: "formatted_address" : "Hauptstraße 26, 31228 Peine, Deutschland"

}
