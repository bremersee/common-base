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
 * <p>
 * Enumeration of default exceptions that will be available in the @{link {@link ExceptionRegistry}.
 * </p>
 *
 * @author Christian Bremer
 */
enum Exceptions {

    /**
     * The server successfully processed the request, but is not returning any
     * content.
     */
    NO_CONTENT(NoContentException.class, 20204, 204, "No Content"),

    /**
     * The server cannot or will not process the request due to something that
     * is perceived to be a client error (e.g., malformed request syntax,
     * invalid request message framing, or deceptive request routing) [RFC7231
     * on code 400].
     */
    BAD_REQUEST(BadRequestException.class, 40400, 400, "Bad Request"),

    /**
     * Similar to 403 Forbidden, but specifically for use when authentication is
     * required and has failed or has not yet been provided. The response must
     * include a WWW-Authenticate header field containing a challenge applicable
     * to the requested resource. 401 semantically means "unauthenticated", i.e.
     * "you don't have necessary credentials".
     */
    UNAUTHORIZED(UnauthorizedException.class, 40401, 401, "Unauthorized"),

    /**
     * Reserved for future use. The original intention was that this code might
     * be used as part of some form of digital cash or micropayment scheme, but
     * that has not happened, and this code is not usually used. Google
     * Developers API uses this status if a particular developer has exceeded
     * the daily limit on requests.
     */
    PAYMENT_REQUIRED(PaymentRequiredException.class, 40402, 402, "Payment Required"),

    /**
     * The request was a valid request, but the server is refusing to respond to
     * it. Unlike a 401 Unauthorized response, authenticating will make no
     * difference. 403 error semantically means "unauthorized", i.e.
     * "you don't have necessary permissions for the resource".
     */
    FORBIDDEN(ForbiddenException.class, 40403, 403, "Forbidden"),

    /**
     * The requested resource could not be found but may be available again in
     * the future. Subsequent requests by the client are permissible.
     */
    NOT_FOUND(NotFoundException.class, 40404, 404, "Not Found."),

    /**
     * A request was made of a resource using a request method not supported by
     * that resource; for example, using GET on a form which requires data to be
     * presented via POST, or using PUT on a read-only resource.
     */
    METHOD_NOT_ALLOWED(MethodNotAllowedException.class, 40405, 405, "Method Not Allowed"),

    /**
     * The requested resource is only capable of generating content not
     * acceptable according to the Accept headers sent in the request.
     */
    NOT_ACCEPTABLE(NotAcceptableException.class, 40406, 406, "Not Acceptable"),

    /**
     * The client must first authenticate itself with the proxy.
     */
    PROXY_AUTHENTICATION_REQUIRED(ProxyAuthenticationRequiredException.class, 40407, 407, "Proxy Authentication Required"),

    /**
     * The server timed out waiting for the request. According to HTTP
     * specifications: The client did not produce a request within the time that
     * the server was prepared to wait. The client MAY repeat the request
     * without modifications at any later time.
     */
    REQUEST_TIMED_OUT(RequestTimeoutException.class, 40408, 408, "Request Timeout"),

    /**
     * Indicates that the request could not be processed because of conflict in
     * the request, such as an edit conflict in the case of multiple updates.
     */
    CONFLICT(ConflictException.class, 40409, 409, "Conflict"),

    /**
     * Indicates that the resource requested is no longer available and will not
     * be available again. This should be used when a resource has been
     * intentionally removed and the resource should be purged. Upon receiving a
     * 410 status code, the client should not request the resource again in the
     * future. Clients such as search engines should remove the resource from
     * their indices. Most use cases do not require clients and search engines
     * to purge the resource, and a "404 Not Found" may be used instead.
     */
    GONE(GoneException.class, 40410, 410, "Gone"),

    /**
     * The request did not specify the length of its content, which is required
     * by the requested resource.
     */
    LENGTH_REQUIRED(LengthRequiredException.class, 40411, 411, "Length Required"),

    /**
     * The server does not meet one of the preconditions that the requester put
     * on the request.
     */
    PRECONDITION_FAILED(PreconditionFailedException.class, 40412, 412, "Precondition Failed"),

    /**
     * The request is larger than the server is willing or able to process.
     * Previously called "Request Entity Too Large".
     */
    REQUEST_ENTITY_TOO_LARGE(RequestEntityTooLargeException.class, 40413, 413, "Request Entity Too Large"),

    /**
     * The 414 (URI Too Long) status code indicates that the server is
     * refusing to service the request because the request-target
     * is longer than the server is willing to interpret.
     * This rare condition is only likely to occur when a client has
     * improperly converted a POST request to a GET request with long query
     * information, when the client has descended into a "black hole" of
     * redirection (e.g., a redirected URI prefix that points to a suffix of
     * itself) or when the server is under attack by a client attempting to
     * exploit potential security holes.
     */
    URI_TOO_LONG(UriTooLongException.class, 40414, 414, "URI Too Long"),

    /**
     * The request entity has a media type which the server or resource does not
     * support. For example, the client uploads an image as image/svg+xml, but
     * the server requires that images use a different format.
     */
    UNSUPPORTED_MEDIA_TYPE(UnsupportedMediaTypeException.class, 40415, 415, "Unsupported Media Type"),

    REQUESTED_RANGE_NOT_SATISFIABLE(RequestedRangeNotSatisfiableException.class, 40416, 416, "Requested Range Not Satisfiable"),

    EXPECTATION_FAILED(ExpectationFailedException.class, 40417, 417, "Expectation Failed"),

    I_AM_A_TEAPOT(TeapotException.class, 40418, 418, "I'm a teapot"),


    PASSWORD_ALREADY_USED(PasswordAlreadyUsedException.class, 40495, 400, "Password was already used."),

    PASSWORD_TOO_WEAK(PasswordTooWeakException.class, 40496, 400, "Password is too weak."),

    PASSWORDS_NOT_MATCH(PasswordsNotMatchException.class, 40497, 400, "Passwords not match."),

    BAD_USER_NAME(BadUserNameException.class, 40600, 400, "Illegal user name."),

    BAD_EMAIL_ADDRESS(BadEmailAddressException.class, 40610, 400, "Illegal email address."),

    BAD_PHONE_NUMBER(BadPhoneNumberException.class, 40620, 400, "Illegal phone number."),

    ALREADY_EXISTS(AlreadyExistsException.class, 40630, 400, "Object already exists."),

    USER_ALREADY_EXISTS(UserAlreadyExistsException.class, 40631, 400, "User already exists."),

    EMAIL_ALREADY_EXISTS(EmailAlreadyExistsException.class, 40632, 400, "Email already exists."),


    INTERNAL_SERVER_ERROR(InternalServerError.class, 50500, 500, "Internal Server Error"),

    NOT_IMPLEMENTED(NotImplementedException.class, 50501, 501, "Not Implemented"),

    BAD_GATEWAY(BadGatewayException.class, 50502, 502, "Bad Gateway"),

    SERVICE_UNAVAILABLE(ServiceUnavailableException.class, 50503, 503, "Service Unavailable"),

    GATEWAY_TIMEOUT(GatewayTimeoutException.class, 50504, 504, "Gateway Timeout"),

    HTTP_VERSION_NOT_SUPPORTED(HttpVersionNotSupportedException.class, 50505, 505, "HTTP Version not supported");

    /**
     * The exception class.
     */
    private Class<? extends RuntimeException> runtimeExceptionClass;

    /**
     * The custom status code (should be unique).
     */
    private int customStatusCode;

    /**
     * The HTTP status code.
     */
    private int httpStatusCode;

    /**
     * The default exception message.
     */
    private String defaultMessage;

    /**
     * Default constructor.
     *
     * @param runtimeExceptionClass the exception class
     * @param customStatusCode      the custom status code
     * @param httpStatusCode        the HTTP status code
     * @param defaultMessage        the default message
     */
    Exceptions(Class<? extends RuntimeException> runtimeExceptionClass, int customStatusCode, int httpStatusCode, String defaultMessage) {
        this.customStatusCode = customStatusCode;
        this.httpStatusCode = httpStatusCode;
        this.runtimeExceptionClass = runtimeExceptionClass;
        this.defaultMessage = defaultMessage;
    }

    /**
     * @return the custom status code
     */
    public int getCustomStatusCode() {
        return customStatusCode;
    }

    /**
     * @return the HTTP status code
     */
    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    /**
     * @return the default message
     */
    public String getDefaultMessage() {
        return defaultMessage;
    }

    /**
     * @return the class of the exception
     */
    public Class<? extends RuntimeException> getRuntimeExceptionClass() {
        return runtimeExceptionClass;
    }

}
