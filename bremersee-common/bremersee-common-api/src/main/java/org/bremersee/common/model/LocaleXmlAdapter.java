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
 * XMl adapter of a {@link Locale}.
 *
 * @author Christian Bremer
 */

public class LocaleXmlAdapter extends XmlAdapter<String, Locale> {

    /**
     * Returns the locale.
     *
     * @param xmlValue the XMl value of the locale
     * @return the locale
     * @throws Exception when parsing failed.
     */
    @Override
    public Locale unmarshal(String xmlValue) throws Exception {
        if (StringUtils.isNotBlank(xmlValue)) {
            return LocaleUtils.toLocale(xmlValue);
        }
        return null;
    }

    /**
     * Returns the string representation of the locale.
     *
     * @param locale the locale
     * @return the string representation
     * @throws Exception when parsing failed
     */
    @Override
    public String marshal(Locale locale) throws Exception {
        String s = locale != null ? locale.toString() : null;
        return StringUtils.isBlank(s) ? null : s;
    }

}