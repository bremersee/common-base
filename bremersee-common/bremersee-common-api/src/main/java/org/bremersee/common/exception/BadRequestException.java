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

package org.bremersee.common.exception;

/**
 * @author Christian Bremer
 */
@SuppressWarnings("SameParameterValue")
public class BadRequestException extends IllegalArgumentException
        implements StatusCodeAwareException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new runtime exception with {@code null} as its detail
     * message. The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public BadRequestException() {
        this(Exceptions.BAD_REQUEST.getDefaultMessage());
    }

    /**
     * Constructs a new runtime exception with the specified detail message. The
     * cause is not initialized, and may subsequently be initialized by a call
     * to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for later
     *                retrieval by the {@link #getMessage()} method.
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
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method). (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     * @since 1.4
     */
    public BadRequestException(Throwable cause) {
        super(Exceptions.BAD_REQUEST.getDefaultMessage(), cause);
    }

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i>
     * automatically incorporated in this runtime exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the
     *                {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method). (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @since 1.4
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public int getHttpStatusCode() {
        return Exceptions.BAD_REQUEST.getHttpStatusCode();
    }

    @Override
    public int getCustomStatusCode() {
        return Exceptions.BAD_REQUEST.getCustomStatusCode();
    }

    @SuppressWarnings("ConstantConditions")
    public static void validateNotNull(Object object, String message) {
        if (object == null) {
            throwException(message);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void validateNotBlank(CharSequence chars, String message) {
        if (chars == null || chars.length() == 0) {
            throwException(message);
        }
    }

    @SuppressWarnings("ConstantConditions")
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

}
