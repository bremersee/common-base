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

package org.bremersee.common.jms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bremersee.utils.CodingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Christian Bremer
 *
 */
public class DefaultJmsConverter implements MessageConverter {
    
    private static final byte[] JSON_START = CodingUtils.toBytesSilently("{", StandardCharsets.UTF_8);

    private static final byte[] JSON_END = CodingUtils.toBytesSilently("}", StandardCharsets.UTF_8);

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected Jaxb2Marshaller marshaller;

    protected ObjectMapper objectMapper;

    /**
     * Default constructor.
     */
    public DefaultJmsConverter() {
        objectMapper = new ObjectMapper();
        AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector secondary = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        AnnotationIntrospectorPair pair = new AnnotationIntrospectorPair(primary, secondary);
        objectMapper.setAnnotationIntrospector(pair);
    }

    public DefaultJmsConverter(Jaxb2Marshaller marshaller) {
        this();
        setMarshaller(marshaller);
    }

    public DefaultJmsConverter(Jaxb2Marshaller marshaller, ObjectMapper objectMapper) {
        this();
        setMarshaller(marshaller);
        setObjectMapper(objectMapper);
    }

    public void setMarshaller(Jaxb2Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        if (objectMapper != null) {
            this.objectMapper = objectMapper;
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.jms.support.converter.MessageConverter#fromMessage(javax.jms.Message)
     */
    @Override
    public Object fromMessage(Message message) throws JMSException {

        if (message == null) {
            return null;
        }

        Class<?> payloadClass;
        try {
            payloadClass = Class.forName(message.getJMSType());
        } catch (Throwable t) { // NOSONAR
            payloadClass = null;
        }

        Object payload;

        if (message instanceof TextMessage) {

            TextMessage msg = (TextMessage)message;
            payload = tryToGetPayload(msg, payloadClass);

        } else if (message instanceof BytesMessage) {

            BytesMessage msg = (BytesMessage)message;
            payload = tryToGetPayload(msg, payloadClass);

        } else {

            MessageConversionException e = new MessageConversionException("Unsupported message type [" + message.getClass().getName() + "].");
            log.error("Could not convert message:", e);
            throw e;
        }

        return payload;
    }

    private Object tryToGetPayload(TextMessage msg, Class<?> payloadClass) throws JMSException {
        String text = msg.getText();
        if (StringUtils.isBlank(text)) {
            return null;
        }
        String trimmedText = text.trim();
        Object payload = null;
        if (payloadClass != null && trimmedText.startsWith("{") && trimmedText.endsWith("}")) {
            try {
                payload = objectMapper.readValue(trimmedText, payloadClass);

            } catch (Throwable t0) { // NOSONAR
                log.info("Reading JSON from message with JMSType [" + msg.getJMSType() + "] failed."); // NOSONAR
            }
        }

        if (payload == null) {
            try {
                payload = marshaller.unmarshal(new StreamSource(new StringReader(text)));

            } catch (Throwable t0) { // NOSONAR
                log.info("Reading XML from message with JMSType [" + msg.getJMSType() + "] failed.");
            }
        }

        if (payload == null) {
            MessageConversionException e = new MessageConversionException("Converting TextMessage failed.");
            log.error("Could not convert text:\n" + text + "\n", e);
            throw e;
        }

        return payload;
    }

    private Object tryToGetPayload(BytesMessage msg, Class<?> payloadClass) throws JMSException {
        int len;
        byte[] buf = new byte[1024];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while ((len = msg.readBytes(buf)) != -1) {
            out.write(buf, 0, len);
        }
        byte[] payloadBytes = out.toByteArray();
        if (payloadBytes == null || payloadBytes.length == 0) {
            return null;
        }

        Object payload = null;
        if (shouldTryJson(payloadClass, payloadBytes)) {
            try {
                payload = objectMapper.readValue(payloadBytes, payloadClass);

            } catch (Throwable t0) { // NOSONAR
                log.info("Reading JSON from message with JMSType [" + msg.getJMSType() + "] failed.");
            }
        }

        if (shouldTryUnmarshal(payload, payloadClass)) {
            try {
                payload = marshaller.unmarshal(new StreamSource(new ByteArrayInputStream(payloadBytes)));

            } catch (Throwable t0) { // NOSONAR
                log.info("Reading XML from message with JMSType [" + msg.getJMSType() + "] failed.");
            }
        }

        if (shouldTryDeserialize(payload, payloadClass)) {
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(payloadBytes));
                payload = objectInputStream.readObject();

            } catch (Throwable t0) { // NOSONAR
                log.info("Reading serialized object of JMSType [" + msg.getJMSType() + "] failed.");
            }
        }

        if (payload == null) {
            MessageConversionException e = new MessageConversionException("Converting BytesMessage failed.");
            log.error("Could not convert bytes:\n" + Base64.encodeBase64String(payloadBytes) + "\n", e);
            throw e;
        }

        return payload;
    }

    private boolean isJson(byte[] payloadBytes) {

        if (payloadBytes == null || payloadBytes.length < (JSON_START.length + JSON_END.length)) {
            return false;
        }
        int len = payloadBytes.length;
        byte[] start = ArrayUtils.subarray(payloadBytes, 0, JSON_START.length);
        byte[] end = ArrayUtils.subarray(payloadBytes, len - JSON_END.length, len);
        return Objects.deepEquals(JSON_START, start) && Objects.deepEquals(JSON_END, end);
    }

    private boolean shouldTryJson(Class<?> payloadClass, byte[] payloadBytes) {
        return payloadClass != null && isJson(payloadBytes);
    }

    private boolean shouldTryUnmarshal(Object payload, Class<?> payloadClass) {
        return payload == null && (payloadClass == null || marshaller.supports(payloadClass));
    }

    private boolean shouldTryDeserialize(Object payload, Class<?> payloadClass) {
        return payload == null && (payloadClass == null || Serializable.class.isAssignableFrom(payloadClass));
    }

    /* (non-Javadoc)
     * @see org.springframework.jms.support.converter.MessageConverter#toMessage(java.lang.Object, javax.jms.Session)
     */
    @Override
    public Message toMessage(Object object, Session session) throws JMSException {
        
        Message message = null;
        
        if (object != null) {
            
            message = createJsonMessage(object, session);
            
            if (message == null && marshaller != null && marshaller.supports(object.getClass())) {
                message = createXmlMessage(object, session);
            }
            
            if (message == null && object instanceof Serializable) {
                message = createSerializedMessage((Serializable)object, session);
            }
        }
        
        if (message == null) {
            MessageConversionException e = new MessageConversionException("No message creator found.");
            log.error("Creating JMS from object of type [" + (object == null ? "null" : object.getClass().getName()) + "] failed.", e);
            throw e;
        }
        
        return message;
    }
    
    private Message createJsonMessage(Object object, Session session) throws JMSException {
        try {
            TextMessage msg = session.createTextMessage();
            String payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            msg.setText(payload);
            msg.setJMSType(object.getClass().getName());
            return msg;
            
        } catch (Throwable t) { // NOSONAR
            log.info("Creating JSON JMS from object of type [" + (object == null ? "null" : object.getClass().getName()) + "] failed.");
            return null;
        }
    }
    
    private Message createXmlMessage(Object object, Session session) throws JMSException {
        try {
            TextMessage msg = session.createTextMessage();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            StreamResult streamResult = new StreamResult(outputStream);
            marshaller.marshal(object, streamResult);
            String payload = CodingUtils.toStringSilently(outputStream.toByteArray(), StandardCharsets.UTF_8);
            msg.setText(payload);
            msg.setJMSType(object.getClass().getName());
            return msg;
            
        } catch (Throwable t) { // NOSONAR
            log.info("Creating XML JMS from object of type [" + (object == null ? "null" : object.getClass().getName()) + "] failed.");
            return null;
        }
    }
    
    private Message createSerializedMessage(Serializable object, Session session) throws JMSException {
        try {
            BytesMessage msg = session.createBytesMessage();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            msg.writeBytes(outputStream.toByteArray());
            msg.setJMSType(object.getClass().getName());
            return msg;
            
        } catch (Throwable t) { // NOSONAR
            log.info("Creating Serialized JMS from object of type [" + (object == null ? "null" : object.getClass().getName()) + "] failed.");
            return null;
        }
    }

}
