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

import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.TimeZone;

/**
 * XML adapter of a {@link TimeZone}.
 *
 * @author Christian Bremer
 */
public class TimeZoneXmlAdapter extends XmlAdapter<String, TimeZone> {

    /**
     * Returns the time zone.
     *
     * @param xmlValue the xml element value
     * @return the time zone
     * @throws Exception when parsing failed
     */
    @Override
    public TimeZone unmarshal(String xmlValue) throws Exception {
        if (StringUtils.isNotBlank(xmlValue)) {
            return TimeZone.getTimeZone(xmlValue);
        }
        return null;
    }

    /**
     * Returns the ID of the time zone.
     *
     * @param timeZone the time zone to marshal
     * @return the XML representation of the time zone
     * @throws Exception when parsing failed
     */
    @Override
    public String marshal(TimeZone timeZone) throws Exception {
        String s = timeZone != null ? timeZone.getID() : null;
        return StringUtils.isBlank(s) ? null : s;
    }

}
