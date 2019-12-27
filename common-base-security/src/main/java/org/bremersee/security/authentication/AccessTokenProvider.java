package org.bremersee.security.authentication;

import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface AccessTokenProvider<T> {

  @NotNull
  T getAccessToken();

}
