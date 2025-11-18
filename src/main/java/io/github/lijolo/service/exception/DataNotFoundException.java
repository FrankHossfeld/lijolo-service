package io.github.lijolo.service.exception;

import java.util.Objects;

public final class DataNotFoundException
    extends RuntimeException {

  public DataNotFoundException(String message) {
    super(message);
  }

  public DataNotFoundException(String message,
                               Throwable throwable) {
    super(message +
          (!Objects.isNull(throwable) && !Objects.isNull(throwable.getMessage()) ?
           " - " + throwable.getMessage() :
           ""));
  }
}
