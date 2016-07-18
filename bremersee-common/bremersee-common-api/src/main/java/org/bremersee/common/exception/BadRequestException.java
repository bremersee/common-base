/**
 * 
 */
package org.bremersee.common.exception;

/**
 * @author Christian Bremer
 *
 */
public class BadRequestException extends IllegalArgumentException
        implements StatusCodeAwareException {

    private static final long serialVersionUID = 1L;
    
    public static void validateNotNull(Object object, String message)
            throws BadRequestException {
        if (object == null) {
            throwException(message);
        }
    }

    public static void validateNotBlank(CharSequence chars, String message)
            throws BadRequestException {
        if (chars == null || chars.length() == 0) {
            throwException(message);
        }
    }

    public static void validateTrue(boolean expression, String message) {
        if (!expression) {
            throwException(message);
        }
    }
    
    private static void throwException(String message) {
        if (message == null) {
            throw new BadRequestException();
        } else {
            throw new BadRequestException(message);
        }
    }
    
    /**
     * Constructs a new runtime exception with {@code null} as its detail
     * message. The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public BadRequestException() {
        this(StatusCode.BAD_REQUEST.getDefaultMessage());
    }

    /**
     * Constructs a new runtime exception with the specified detail message. The
     * cause is not initialized, and may subsequently be initialized by a call
     * to {@link #initCause}.
     *
     * @param message
     *            the detail message. The detail message is saved for later
     *            retrieval by the {@link #getMessage()} method.
     */
    public BadRequestException(String message) {
        super(message);
    }

    /**
     * Constructs a new runtime exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>). This
     * constructor is useful for runtime exceptions that are little more than
     * wrappers for other throwables.
     *
     * @param cause
     *            the cause (which is saved for later retrieval by the
     *            {@link #getCause()} method). (A <tt>null</tt> value is
     *            permitted, and indicates that the cause is nonexistent or
     *            unknown.)
     * @since 1.4
     */
    public BadRequestException(Throwable cause) {
        super(StatusCode.BAD_REQUEST.getDefaultMessage(), cause);
    }

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i>
     * automatically incorporated in this runtime exception's detail message.
     *
     * @param message
     *            the detail message (which is saved for later retrieval by the
     *            {@link #getMessage()} method).
     * @param cause
     *            the cause (which is saved for later retrieval by the
     *            {@link #getCause()} method). (A <tt>null</tt> value is
     *            permitted, and indicates that the cause is nonexistent or
     *            unknown.)
     * @since 1.4
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public int getStatusCode() {
        return StatusCode.BAD_REQUEST.getStatusCode();
    }

}
