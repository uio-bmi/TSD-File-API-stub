package no.uio.ifi.tsd.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
@ControllerAdvice
public class UnauthorizedAdvice {

  @ResponseBody
  @ExceptionHandler(UnauthorizedException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  String unauthorizedHandler(UnauthorizedException ex) {
    return ex.getMessage();
  }
}
