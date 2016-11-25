/**
 * 
 */
package org.bremersee.common.exception;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Christian Bremer
 *
 */
public enum StatusCode {
    
    /**
     * The server successfully processed the request, but is not returning any
     * content.
     */
    NO_CONTENT(204, NoContentException.class, "No Content"),
    
    /**
     * The server cannot or will not process the request due to something that
     * is perceived to be a client error (e.g., malformed request syntax,
     * invalid request message framing, or deceptive request routing) [RFC7231
     * on code 400].
     */
    BAD_REQUEST(400, BadRequestException.class, "Bad Request"),
    
    /**
     * Similar to 403 Forbidden, but specifically for use when authentication is
     * required and has failed or has not yet been provided. The response must
     * include a WWW-Authenticate header field containing a challenge applicable
     * to the requested resource. 401 semantically means "unauthenticated", i.e.
     * "you don't have necessary credentials".
     */
    UNAUTHORIZED(401, UnauthorizedException.class, "Unauthorized"),
    
    /**
     * Reserved for future use. The original intention was that this code might
     * be used as part of some form of digital cash or micropayment scheme, but
     * that has not happened, and this code is not usually used. Google
     * Developers API uses this status if a particular developer has exceeded
     * the daily limit on requests.
     */
    PAYMENT_REQUIRED(402, PaymentRequiredException.class, "Payment Required"),
    
    /**
     * The request was a valid request, but the server is refusing to respond to
     * it. Unlike a 401 Unauthorized response, authenticating will make no
     * difference. 403 error semantically means "unauthorized", i.e.
     * "you don't have necessary permissions for the resource".
     */
    FORBIDDEN(403, ForbiddenException.class, "Forbidden"),
    
    /**
     * The requested resource could not be found but may be available again in
     * the future. Subsequent requests by the client are permissible.
     */
    NOT_FOUND(404, NotFoundException.class, "Not Found."),
    
    /**
     * A request was made of a resource using a request method not supported by
     * that resource; for example, using GET on a form which requires data to be
     * presented via POST, or using PUT on a read-only resource.
     */
    METHOD_NOT_ALLOWED(405, MethodNotAllowedException.class, "Method Not Allowed"),
    
    /**
     * The requested resource is only capable of generating content not
     * acceptable according to the Accept headers sent in the request.
     */
    NOT_ACCEPTABLE(406, NotAcceptableException.class, "Not Acceptable"),
    
    /**
     * The client must first authenticate itself with the proxy.
     */
    PROXY_AUTHENTICATION_REQUIRED(407, ProxyAuthenticationRequiredException.class, "Proxy Authentication Required"),
    
    /**
     * The server timed out waiting for the request. According to HTTP
     * specifications: The client did not produce a request within the time that
     * the server was prepared to wait. The client MAY repeat the request
     * without modifications at any later time.
     */
    REQUEST_TIMED_OUT(408, RequestTimeoutException.class, "Request Timeout"),
    
    /**
     * Indicates that the request could not be processed because of conflict in
     * the request, such as an edit conflict in the case of multiple updates.
     */
    CONFLICT(409, ConflictException.class, "Conflict"),
    
    /**
     * Indicates that the resource requested is no longer available and will not
     * be available again. This should be used when a resource has been
     * intentionally removed and the resource should be purged. Upon receiving a
     * 410 status code, the client should not request the resource again in the
     * future. Clients such as search engines should remove the resource from
     * their indices. Most use cases do not require clients and search engines
     * to purge the resource, and a "404 Not Found" may be used instead.
     */
    GONE(410, GoneException.class, "Gone"),
    
    /**
     * The request did not specify the length of its content, which is required
     * by the requested resource.
     */
    LENGTH_REQUIRED(411, LengthRequiredException.class, "Length Required"),
    
    /**
     * The server does not meet one of the preconditions that the requester put
     * on the request.
     */
    PRECONDITION_FAILED(412, PreconditionFailedException.class, "Precondition Failed"),
    
    /**
     * The request is larger than the server is willing or able to process.
     * Previously called "Request Entity Too Large".
     */
    REQUEST_ENTITY_TOO_LARGE(413, RequestEntityTooLargeException.class, "Request Entity Too Large"),
    
    /**
     * The URI provided was too long for the server to process. Often the result
     * of too much data being encoded as a query-string of a GET request, in
     * which case it should be converted to a POST request. Called
     * "Request-URI Too Long" previously
     */
    REQUEST_URL_TOO_LONG(414, RequestUriTooLongException.class, "Request-URI Too Long"),
    
    /**
     * The request entity has a media type which the server or resource does not
     * support. For example, the client uploads an image as image/svg+xml, but
     * the server requires that images use a different format.
     */
    UNSUPPORTED_MEDIA_TYPE(415, UnsupportedMediaTypeException.class, "Unsupported Media Type"),
    
    REQUESTED_RANGE_NOT_SATISFIABLE(416, UnsupportedMediaTypeException.class, "Requested Range Not Satisfiable"),
    
    EXPECTATION_FAILED(417, UnsupportedMediaTypeException.class, "Expectation Failed"),
    
    PASSWORD_ALREADY_USED(495, PasswordAlreadyUsedException.class, "Password was already used."),
    
    PASSWORD_TOO_WEAK(496, PasswordTooWeakException.class, "Password is too weak."),
    
    PASSWORDS_NOT_MATCH(497, PasswordsNotMatchException.class, "Passwords not match."),
    
    BAD_USER_NAME(498, BadUserNameException.class, "Illegal user name."),
    
    ALREADY_EXISTS(499, AlreadyExistsException.class, "Object already exists.")
    ;
    
    public static StatusCode findByStatusCode(int statusCode) {
        for (StatusCode entry : StatusCode.values()) {
            if (statusCode == entry.getStatusCode()) {
                return entry;
            }
        }
        throw new IllegalArgumentException("Unknown status code [" + statusCode + "]");
    }
    
    public static RuntimeException createRuntimeException(int statusCode, String message) {
        try {
            StatusCode en = findByStatusCode(statusCode);
            return en.getRuntimeException(message);
            
        } catch (IllegalArgumentException e) {
            return new RuntimeException(message);
        }
    }
    
    public static void throwRuntimeException(int statusCode, String message) throws RuntimeException {
        throw createRuntimeException(statusCode, message);
    }

    private int statusCode;
    
    private Class<? extends RuntimeException> runtimeExceptionClass;

    private String defaultMessage;
    
    private StatusCode(int statusCode, Class<? extends RuntimeException> runtimeExceptionClass, String defaultMessage) {
            this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
    
    public Class<? extends RuntimeException> getRuntimeExceptionClass() {
        return runtimeExceptionClass;
    }

    public RuntimeException getRuntimeException() {
        return getRuntimeException(null);
    }
    
    public RuntimeException getRuntimeException(String message) {
        if (message == null) {
            message = defaultMessage;
        }
        try {
            Constructor<? extends RuntimeException> c = runtimeExceptionClass.getConstructor(String.class);
            return c.newInstance(message);
            
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(message);
        }
    }
    
}
