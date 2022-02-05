/*
 * Copyright 2022 the original author or authors.
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

package org.bremersee.http.converter;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import org.bremersee.xml.JaxbContextBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.xml.AbstractXmlHttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * The jaxb http message converter.
 *
 * @author Arjen Poutsma, Juergen Hoeller, Rossen Stoyanchev, Christian Bremer
 * @see org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter
 */
public class Jaxb2HttpMessageConverter extends AbstractXmlHttpMessageConverter<Object> {

  private final JaxbContextBuilder jaxbContextBuilder;

  /**
   * Instantiates a new jaxb http message converter.
   *
   * @param jaxbContextBuilder the jaxb context builder
   */
  public Jaxb2HttpMessageConverter(JaxbContextBuilder jaxbContextBuilder) {
    Assert.notNull(jaxbContextBuilder, "JaxbContextBuilder must be present.");
    this.jaxbContextBuilder = jaxbContextBuilder;
  }

  @Override
  public boolean canRead(@NonNull Class<?> clazz, @Nullable MediaType mediaType) {
    return jaxbContextBuilder.canUnmarshal(clazz) && this.canRead(mediaType);
  }

  @Override
  public boolean canWrite(@NonNull Class<?> clazz, @Nullable MediaType mediaType) {
    return jaxbContextBuilder.canMarshal(clazz) && this.canWrite(mediaType);
  }

  @Override
  protected boolean supports(@NonNull Class<?> clazz) {
    throw new UnsupportedOperationException();
  }

  @NonNull
  @Override
  protected Object readFromSource(
      @NonNull Class<?> clazz,
      @NonNull HttpHeaders headers,
      @NonNull Source source) throws Exception {

    try {
      // processSource of Jaxb2RootElementHttpMessageConverter causes MalformedUrlException
      Unmarshaller unmarshaller = jaxbContextBuilder.buildUnmarshaller(clazz);
      if (clazz.isAnnotationPresent(XmlRootElement.class)) {
        return unmarshaller.unmarshal(source);
      } else {
        JAXBElement<?> jaxbElement = unmarshaller.unmarshal(source, clazz);
        return jaxbElement.getValue();
      }
    } catch (UnmarshalException unmarshalException) {
      throw unmarshalException;
    } catch (JAXBException jaxbException) {
      throw new HttpMessageConversionException("Invalid JAXB setup: "
          + jaxbException.getMessage(), jaxbException);
    }
  }

  @Override
  protected void writeToResult(
      @NonNull Object o,
      HttpHeaders headers,
      @NonNull Result result) throws Exception {

    try {
      Marshaller marshaller = jaxbContextBuilder.buildMarshaller(o);
      this.setCharset(headers.getContentType(), marshaller);
      marshaller.marshal(o, result);
    } catch (MarshalException marshalException) {
      throw marshalException;
    } catch (JAXBException jaxbException) {
      throw new HttpMessageConversionException("Invalid JAXB setup: "
          + jaxbException.getMessage(), jaxbException);
    }
  }

  private void setCharset(
      @Nullable MediaType contentType,
      Marshaller marshaller) throws PropertyException {

    if (contentType != null && contentType.getCharset() != null) {
      marshaller.setProperty("jaxb.encoding", contentType.getCharset().name());
    }
  }

}
