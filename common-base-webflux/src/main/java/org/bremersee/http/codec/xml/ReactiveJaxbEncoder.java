/*
 * Copyright 2019 the original author or authors.
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

package org.bremersee.http.codec.xml;

import java.io.OutputStream;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;
import org.bremersee.xml.JaxbContextBuilder;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractSingleValueEncoder;
import org.springframework.core.codec.CodecException;
import org.springframework.core.codec.EncodingException;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;

/**
 * Encode from single value to a byte stream containing XML elements.
 *
 * <p>{@link javax.xml.bind.annotation.XmlElements @XmlElements} and
 * {@link javax.xml.bind.annotation.XmlElement @XmlElement} can be used to specify how collections
 * should be marshalled.
 *
 * <p>The encoding parts are taken from {@link org.springframework.http.codec.xml.Jaxb2XmlEncoder}.
 *
 * @author Sebastien Deleuze
 * @author Arjen Poutsma
 * @author Christian Bremer
 */
@Validated
public class ReactiveJaxbEncoder extends AbstractSingleValueEncoder<Object> {

  private final JaxbContextBuilder jaxbContextBuilder;

  /**
   * Instantiates a new reactive jaxb encoder.
   *
   * @param jaxbContextBuilder the jaxb context builder
   */
  public ReactiveJaxbEncoder(final JaxbContextBuilder jaxbContextBuilder) {
    super(MimeTypeUtils.APPLICATION_XML, MimeTypeUtils.TEXT_XML);
    this.jaxbContextBuilder = jaxbContextBuilder != null
        ? jaxbContextBuilder
        : JaxbContextBuilder.builder()
            .withCanUnmarshal(JaxbContextBuilder.CAN_UNMARSHAL_ALL);
  }

  @Override
  public boolean canEncode(final ResolvableType elementType, @Nullable final MimeType mimeType) {
    if (super.canEncode(elementType, mimeType)) {
      final Class<?> outputClass = elementType.toClass();
      return jaxbContextBuilder.canMarshal(outputClass);
    } else {
      return false;
    }
  }

  @Override
  protected Flux<DataBuffer> encode(
      final Object value,
      final DataBufferFactory dataBufferFactory,
      final ResolvableType type,
      @Nullable final MimeType mimeType,
      @Nullable final Map<String, Object> hints) {

    if (!Hints.isLoggingSuppressed(hints)) {
      LogFormatUtils.traceDebug(logger, traceOn -> {
        String formatted = LogFormatUtils.formatValue(value, !traceOn);
        return Hints.getLogPrefix(hints) + "Encoding [" + formatted + "]";
      });
    }

    boolean release = true;
    DataBuffer buffer = dataBufferFactory.allocateBuffer(1024);
    OutputStream outputStream = buffer.asOutputStream();
    try {
      Marshaller marshaller = jaxbContextBuilder.buildMarshaller(value);
      marshaller.marshal(value, outputStream);
      release = false;
      return Flux.just(buffer);
    } catch (MarshalException ex) {
      return Flux.error(new EncodingException(
          "Could not marshal " + value.getClass() + " to XML", ex));
    } catch (JAXBException ex) {
      return Flux.error(new CodecException("Invalid JAXB configuration", ex));
    } finally {
      if (release) {
        DataBufferUtils.release(buffer);
      }
    }
  }

}
