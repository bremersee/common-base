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

import java.util.Locale;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Christian Bremer
 *
 */

public class LocaleXmlAdapter extends XmlAdapter<String, Locale> {

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public Locale unmarshal(String v) throws Exception {
        if (StringUtils.isNotBlank(v)) {
            return LocaleUtils.toLocale(v);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(Locale v) throws Exception {
        String s = v != null ? v.toString() : null;
        return StringUtils.isBlank(s) ? null : s;
    }

}
