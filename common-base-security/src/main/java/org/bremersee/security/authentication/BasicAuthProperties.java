package org.bremersee.security.authentication;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * The basic auth properties.
 */
public interface BasicAuthProperties {

  /**
   * Gets username.
   *
   * @return the username
   */
  String getUsername();

  /**
   * Gets password.
   *
   * @return the password
   */
  String getPassword();

  /**
   * Returns a new basic auth properties builder.
   *
   * @return the builder
   */
  static Builder builder() {
    return new Builder();
  }

  /**
   * The basic auth properties builder.
   */
  @ToString(exclude = {"password"})
  @EqualsAndHashCode(exclude = {"password"})
  class Builder {

    private String username;

    private String password;

    /**
     * Sets username on builder.
     *
     * @param username the username
     * @return the builder
     */
    public Builder username(String username) {
      this.username = username;
      return this;
    }

    /**
     * Sets password on builder.
     *
     * @param password the password
     * @return the builder
     */
    public Builder password(String password) {
      this.password = password;
      return this;
    }

    /**
     * Build basic auth properties.
     *
     * @return the basic auth properties
     */
    public BasicAuthProperties build() {
      return new Impl(username, password);
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @ToString(exclude = {"password"})
    @EqualsAndHashCode(exclude = {"password"})
    private static class Impl implements BasicAuthProperties {

      private final String username;

      private final String password;

    }
  }

}
