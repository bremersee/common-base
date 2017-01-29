package org.bremersee.common.jms;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * JMS utility methods.
 *
 * @author Christian Bremer
 */
public abstract class JmsUtils {

    private static final Logger log = LoggerFactory.getLogger(JmsUtils.class);

    /**
     * Timeout value indicating that a receive operation should check if a
     * message is immediately available without blocking.
     */
    public static final long RECEIVE_TIMEOUT_NO_WAIT = -1;

    /**
     * Timeout value indicating a blocking receive without timeout.
     */
    public static final long RECEIVE_TIMEOUT_INDEFINITE_WAIT = 0;

    /**
     * Character used to replace illegal characters in a message header.
     */
    public static final char REPLACEMENT_CHAR = '_';

    /**
     * Never construct.
     */
    private JmsUtils() {
    }

    /**
     * Encodes a {@link String} so that is is a valid JMS header name.
     *
     * @param name the String to encode
     * @return a valid JMS header name
     * @throws IllegalArgumentException if name is null or blank
     */
    public static String encodeHeader(String name) {

        // check against JMS 1.1 spec, sections 3.5.1 (3.8.1.1)
        boolean nonCompliant = false;

        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "Header name to encode must not be null or blank.");
        }

        int i = 0;
        int length = name.length();
        while (i < length && Character.isJavaIdentifierPart(name.charAt(i))) {
            // zip through
            i++;
        }

        if (i == length) {
            // String is already valid
            return name;

        } else {
            // make a copy, fix up remaining characters
            StringBuilder sb = new StringBuilder(name);
            for (int j = i; j < length; j++) {
                if (!Character.isJavaIdentifierPart(sb.charAt(j))) {
                    sb.setCharAt(j, REPLACEMENT_CHAR);
                    nonCompliant = true;
                }
            }

            if (nonCompliant) {
                log.warn(
                        MessageFormat.format(
                                "Header: {0} is not compliant with JMS specification (sec. 3.5.1, 3.8.1.1). " +
                                        "It will cause problems in your and other applications. Please update " +
                                        "your application code to correct this. I renamed it to {1}",
                                name, sb.toString()));
            }

            return sb.toString();
        }
    }

    /**
     * Returns the value of a message header or {@code null} if there's no
     * such message header.
     *
     * @param message      the message
     * @param key          the name of the message header property
     * @param defaultValue a default value, can be {@code null}
     * @return the value of a message header
     */
    public static Object getProperty(Message message, String key,
                                     Object defaultValue) {

        String encodedKey = JmsUtils.encodeHeader(key);
        try {
            Object value = message.getObjectProperty(encodedKey);
            if (value != null)
                return value;
            else
                return defaultValue;
        } catch (Exception e) {
            log.error("get property with key = " + encodedKey
                    + "throws an exception; return default value = "
                    + defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Returns the value of a message header or {@code null} if there's no
     * such message header.
     *
     * @param message      the message
     * @param key          the name of the message header property
     * @param defaultValue a default value, can be {@code null}
     * @return the value of a message header
     */
    public static String getPropertyAsString(Message message, String key,
                                             String defaultValue) {
        Object obj = getProperty(message, key, defaultValue);
        if (obj != null) {
            return obj.toString();
        }
        return null;
    }

    /**
     * Returns the value of a message header or {@code null} if there's no
     * such message header.
     *
     * @param message      the message
     * @param key          the name of the message header property
     * @param defaultValue a default value, can be {@code null}
     * @return the value of a message header
     */
    public static BigInteger getPropertyAsBigDecimal(Message message,
                                                     String key, BigInteger defaultValue) {
        Object obj = getProperty(message, key, defaultValue);
        if (obj != null) {
            return new BigInteger(obj.toString());
        }
        return null;
    }

    /**
     * Returns the value of a message header or {@code null} if there's no
     * such message header.
     *
     * @param message      the message
     * @param key          the name of the message header property
     * @param defaultValue a default value, can be {@code null}
     * @return the value of a message header
     */
    public static Long getPropertyAsLong(Message message, String key,
                                         Long defaultValue) {
        Object obj = getProperty(message, key, defaultValue);
        if (obj != null) {
            return Long.parseLong(obj.toString());
        }
        return null;
    }

    /**
     * Returns the value of a message header or {@code null} if there's no
     * such message header.
     *
     * @param message      the message
     * @param key          the name of the message header property
     * @param defaultValue a default value, can be {@code null}
     * @return the value of a message header
     */
    public static Integer getPropertyAsInteger(Message message, String key,
                                               Integer defaultValue) {
        Object obj = getProperty(message, key, defaultValue);
        if (obj != null) {
            return Integer.parseInt(obj.toString());
        }
        return null;
    }

    /**
     * Returns the value of a message header or {@code null} if there's no
     * such message header.
     *
     * @param message      the message
     * @param key          the name of the message header property
     * @param defaultValue a default value, can be {@code null}
     * @return the value of a message header
     */
    public static Byte getPropertyAsByte(Message message, String key,
                                         Byte defaultValue) {
        Object obj = getProperty(message, key, defaultValue);
        if (obj != null) {
            return Byte.parseByte(obj.toString());
        }
        return null;
    }

    /**
     * Returns the value of a message header or {@code null} if there's no
     * such message header.
     *
     * @param message      the message
     * @param key          the name of the message header property
     * @param defaultValue a default value, can be {@code null}
     * @return the value of a message header
     */
    public static BigDecimal getPropertyAsBigDecimal(Message message,
                                                     String key, BigDecimal defaultValue) {
        Object obj = getProperty(message, key, defaultValue);
        if (obj != null) {
            return new BigDecimal(obj.toString());
        }
        return null;
    }

    /**
     * Returns the value of a message header or {@code null} if there's no
     * such message header.
     *
     * @param message      the message
     * @param key          the name of the message header property
     * @param defaultValue a default value, can be {@code null}
     * @return the value of a message header
     */
    public static Double getPropertyAsDouble(Message message, String key,
                                             Double defaultValue) {
        Object obj = getProperty(message, key, defaultValue);
        if (obj != null) {
            return Double.parseDouble(obj.toString());
        }
        return null;
    }

    /**
     * Returns the value of a message header or {@code null} if there's no
     * such message header.
     *
     * @param message      the message
     * @param key          the name of the message header property
     * @param defaultValue a default value, can be {@code null}
     * @return the value of a message header
     */
    public static Float getPropertyAsFloat(Message message, String key,
                                           Float defaultValue) {
        Object obj = getProperty(message, key, defaultValue);
        if (obj != null) {
            return Float.parseFloat(obj.toString());
        }
        return null;
    }

    /**
     * Returns the value of a message header or {@code null} if there's no
     * such message header.
     *
     * @param message      the message
     * @param key          the name of the message header property
     * @param defaultValue a default value, can be {@code null}
     * @return the value of a message header
     */
    public static Boolean getPropertyAsBoolean(Message message, String key,
                                               Boolean defaultValue) {
        Object obj = getProperty(message, key, defaultValue);
        if (obj != null) {
            return Boolean.parseBoolean(obj.toString());
        }
        return defaultValue;
    }

    /**
     * Sets a set of properties on the message.
     *
     * @param message   the message
     * @param headerMap a map of properties
     * @throws JMSException when the property can't be set
     */
    public static void setProperties(Message message,
                                     Map<String, Object> headerMap) throws JMSException {

        for (Map.Entry<String, Object> entry : headerMap.entrySet()) {
            if (entry.getValue() != null) {
                setProperty(message, entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Sets the specified property onto the message.
     *
     * @param message the message
     * @param key     the name of property
     * @param value   the value of the property
     * @throws JMSException when property can't be set
     */
    public static void setProperty(Message message, String key, Object value) // NOSONAR
            throws JMSException {

        if (key != null && key.trim().length() > 0 && value != null) {

            // Ensures that all keys are conform with JMS spec.
            String encodedKey = JmsUtils.encodeHeader(key);

            if (value.getClass().isPrimitive()
                    || value instanceof Boolean
                    || value instanceof Number
                    || value instanceof String) {
                message.setObjectProperty(encodedKey, value);
            } else if (value.getClass().isEnum()) {
                setProperty(message, encodedKey, ((Enum<?>) value).name());
            } else if (value instanceof Date) {
                setProperty(message, encodedKey, ((Date) value).getTime());
            } else if (value instanceof Calendar) {
                setProperty(message, encodedKey, ((Calendar) value).getTime());
            } else {
                log.warn(
                        "Key = " + encodedKey + " and value = " + value + " of type = "
                                + value.getClass().getName() + " are ignored.");
            }
        }
    }

    /**
     * Creates a {@link Destination} from it's name.
     *
     * @param destinationName    the name of the destination
     * @param destinationIsTopic specifies whether the destination is a {@link Topic} or a
     *                           {@link Queue}, the argument can be {@code null} only if the
     *                           destination name ends with 'topic' or 'queue' (case
     *                           insensitive)
     * @param session            the JMS session
     * @return the JMS destination
     * @throws JMSException if resolving fails
     */
    public static Destination resolveDestination(String destinationName,
                                                 Boolean destinationIsTopic, Session session) throws JMSException {

        if (destinationName == null || destinationName.trim().length() == 0) {
            throw new JMSException(
                    "Destination name must not be null or blank.");
        }
        if (session == null) {
            throw new JMSException("Session must not be null.");
        }
        String dn = destinationName.toLowerCase();
        if (dn.endsWith("queue")) {
            return session.createQueue(destinationName);
        } else if (dn.endsWith("topic")) {
            return session.createTopic(destinationName);
        }
        if (destinationIsTopic != null) {
            if (Boolean.TRUE.equals(destinationIsTopic)) {
                return session.createTopic(destinationName);
            } else {
                return session.createQueue(destinationName);
            }
        }
        throw new JMSException(
                "Could not resolve destination because destination name does not end with 'queue' or 'topic' and 'destinationIsTopic' is NULL.");
    }

    public static String getDestinationName(Destination destination) throws JMSException {
        if (destination instanceof Queue) {
            return ((Queue) destination).getQueueName();
        }
        if (destination instanceof Topic) {
            return ((Topic) destination).getTopicName();
        }
        return null;
    }

    public static boolean isPubSubDomain(Destination destination) {
        Validate.notNull(destination, "Destination must not be null.");
        return destination instanceof Topic;
    }

    /**
     * Creates a {@link Connection}.
     *
     * @param connectionFactory the connection factory
     * @return the connection
     * @throws JMSException if creation of the connection fails
     */
    public static Connection createConnection(
            ConnectionFactory connectionFactory) throws JMSException {
        return createConnection(connectionFactory, null, null);
    }

    /**
     * Creates a {@link Connection} with the specified user anem and password.
     *
     * @param connectionFactory the connection factory
     * @param userName          the user name
     * @param password          the password
     * @return the connection
     * @throws JMSException if creation of the connection fails
     */
    @SuppressWarnings("SameParameterValue")
    public static Connection createConnection(
            ConnectionFactory connectionFactory, String userName,
            String password) throws JMSException {

        if (userName == null || userName.trim().length() == 0) {
            return connectionFactory.createConnection();
        } else {
            return connectionFactory.createConnection(userName, password);
        }
    }

    /**
     * Creates a JMS session.
     *
     * @param connection the connection
     * @return the JMS session
     * @throws JMSException if creation of the JMS session fails
     */
    public static Session createSession(Connection connection)
            throws JMSException {
        return createSession(connection, Session.AUTO_ACKNOWLEDGE, false);
    }

    /**
     * Creates a JMS session.
     *
     * @param connection      the connection
     * @param acknowledgeMode the acknowledge mode
     * @return the JMS session
     * @throws JMSException if creation of the JMS session fails
     */
    public static Session createSession(Connection connection,
                                        int acknowledgeMode) throws JMSException {
        return createSession(connection, acknowledgeMode, false);
    }

    /**
     * Creates a JMS session.
     *
     * @param connection      the connection
     * @param acknowledgeMode the acknowledge mode
     * @param startConnection {@code true} if the connection should be started otherwise
     *                        {@code false}
     * @return the JMS session
     * @throws JMSException if creation of the JMS session fails
     */
    @SuppressWarnings("SameParameterValue")
    public static Session createSession(Connection connection,
                                        int acknowledgeMode, boolean startConnection) throws JMSException {

        return createSession(connection, false, acknowledgeMode,
                startConnection);
    }

    /**
     * Creates a JMS session.
     *
     * @param connection      the connection
     * @param transacted      {@code true} if the session should be transacted otherwise
     *                        {@code false}
     * @param acknowledgeMode the acknowledge mode
     * @param startConnection {@code true} if the connection should be started otherwise
     *                        {@code false}
     * @return the JMS session
     * @throws JMSException if creation of the JMS session fails
     */
    @SuppressWarnings("SameParameterValue")
    public static Session createSession(Connection connection,
                                        boolean transacted, int acknowledgeMode, boolean startConnection)
            throws JMSException {
        if (connection == null) {
            throw new JMSException("Connection must not be null.");
        }
        Session session = null;
        try {
            session = connection.createSession(transacted, acknowledgeMode);
            if (startConnection) {
                connection.start();
            }
        } catch (JMSException e) {
            if (session != null) {
                try {
                    session.close();
                } catch (Throwable ignored) { // NOSONAR
                    // ignored
                }
            }
            try {
                connection.close();
            } catch (Throwable ignored) { // NOSONAR
                // ignored
            }
            throw e;
        }
        return session;
    }

    /**
     * Create a message producer.
     *
     * @param session     the JMS session
     * @param destination the JMS destination
     * @return the message producer
     * @throws JMSException if creation of the message producer fails
     */
    public static MessageProducer createProducer(Session session,
                                                 Destination destination) throws JMSException {
        return session.createProducer(destination);
    }

    /**
     * Creates a message consumer.
     *
     * @param session         the JMS session
     * @param destination     the JMS destination
     * @param messageSelector the JMS message selector (may be {@code null})
     * @return the message consumer
     * @throws JMSException if creation of the message consumer fails
     */
    public static MessageConsumer createConsumer(Session session,
                                                 Destination destination, String messageSelector)
            throws JMSException {
        return createConsumer(session, destination, messageSelector, false);
    }

    /**
     * Creates a message consumer.
     *
     * @param session         the JMS session
     * @param destination     the JMS destination
     * @param messageSelector the JMS message selector (may be {@code null})
     * @param isPubSubNoLocal no local flag
     * @return the message consumer
     * @throws JMSException if creation of the message consumer fails
     */
    @SuppressWarnings("SameParameterValue")
    public static MessageConsumer createConsumer(Session session,
                                                 Destination destination, String messageSelector,
                                                 boolean isPubSubNoLocal) throws JMSException {

        // Only pass in the NoLocal flag in case of a Topic:
        // Some JMS providers, such as WebSphere MQ 6.0, throw
        // IllegalStateException
        // in case of the NoLocal flag being specified for a Queue.
        if (destination instanceof Topic) {
            return session.createConsumer(destination, messageSelector,
                    isPubSubNoLocal);
        } else {
            return session.createConsumer(destination, messageSelector);
        }
    }

    /**
     * Close the given JMS Session and ignore any thrown exception. This is
     * useful for typical {@code finally} blocks in manual JMS code.
     *
     * @param session the JMS Session to close (may be {@code null})
     */
    public static void closeSession(Session session) {
        if (session != null) {
            try {
                session.close();
            } catch (JMSException ex) {
                log.debug("Could not close JMS Session", ex);
            } catch (Throwable ex) { // NOSONAR
                // We don't trust the JMS provider: It might throw
                // RuntimeException or Error.
                log.debug("Unexpected exception on closing JMS Session", ex);
            }
        }
    }

    /**
     * Close the given JMS MessageProducer and ignore any thrown exception. This
     * is useful for typical {@code finally} blocks in manual JMS code.
     *
     * @param producer the JMS MessageProducer to close (may be {@code null})
     */
    public static void closeMessageProducer(MessageProducer producer) {
        if (producer != null) {
            try {
                producer.close();
            } catch (JMSException ex) {
                log.debug("Could not close JMS MessageProducer", ex);
            } catch (Throwable ex) { // NOSONAR
                // We don't trust the JMS provider: It might throw
                // RuntimeException or Error.
                log.debug("Unexpected exception on closing JMS MessageProducer",
                        ex);
            }
        }
    }

    /**
     * Close the given JMS MessageConsumer and ignore any thrown exception. This
     * is useful for typical {@code finally} blocks in manual JMS code.
     *
     * @param consumer the JMS MessageConsumer to close (may be {@code null})
     */
    public static void closeMessageConsumer(MessageConsumer consumer) {
        if (consumer != null) {
            // Clear interruptions to ensure that the consumer closes
            // successfully...
            // (working around misbehaving JMS providers such as ActiveMQ)
            boolean wasInterrupted = Thread.interrupted();
            try {
                consumer.close();
            } catch (JMSException ex) {
                log.debug("Could not close JMS MessageConsumer", ex);
            } catch (Throwable ex) { // NOSONAR
                // We don't trust the JMS provider: It might throw
                // RuntimeException or Error.
                log.debug("Unexpected exception on closing JMS MessageConsumer",
                        ex);
            } finally {
                if (wasInterrupted) {
                    // Reset the interrupted flag as it was before.
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Release the given Connection, stopping it (if necessary) and eventually
     * closing it.
     *
     * @param con     the Connection to release (if this is {@code null}, the call
     *                will be ignored)
     * @param started whether the Connection might have been started by the
     *                application
     */
    public static void releaseConnection(Connection con, boolean started) {
        if (con == null) {
            return;
        }
        if (started) {
            try {
                con.stop();
            } catch (Throwable ex) { // NOSONAR
                log.debug("Could not stop JMS Connection before closing it",
                        ex);
            }
        }
        try {
            con.close();
        } catch (Throwable ex) { // NOSONAR
            log.debug("Could not close JMS Connection", ex);
        }
    }

}
