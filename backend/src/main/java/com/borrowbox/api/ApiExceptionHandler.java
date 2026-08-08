package com.borrowbox.api;

import com.borrowbox.model.InsufficientCreditsException;
import com.borrowbox.model.LendingNotAllowedException;
import com.borrowbox.model.MemberAlreadyExistsException;
import com.borrowbox.model.NotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Turns the exceptions the model throws into HTTP responses.
 *
 * <p>The model has no idea what a status code is, which is the point: the rules
 * it enforces are the same whether it is being driven by a web request or a
 * test. Deciding that "already registered" means 409 belongs out here.
 *
 * <p>Responses use RFC 7807 problem details, so every error has the same shape.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  /** Asked for something that is not there. */
  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail handleNotFound(NotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, "Not found", e.getMessage());
  }

  /** The email or mobile belongs to somebody else. */
  @ExceptionHandler(MemberAlreadyExistsException.class)
  public ProblemDetail handleConflict(MemberAlreadyExistsException e) {
    return problem(HttpStatus.CONFLICT, "Already registered", e.getMessage());
  }

  /**
   * The request made sense but the rules say no: dates in the past, an item
   * already booked, not enough credits.
   */
  @ExceptionHandler({LendingNotAllowedException.class, InsufficientCreditsException.class})
  public ProblemDetail handleRuleViolation(RuntimeException e) {
    return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Not allowed", e.getMessage());
  }

  /** Something in the request was malformed. */
  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleBadArgument(IllegalArgumentException e) {
    return problem(HttpStatus.BAD_REQUEST, "Bad request", e.getMessage());
  }

  /** The request is fine, but the thing it targets is in the wrong state for it. */
  @ExceptionHandler(IllegalStateException.class)
  public ProblemDetail handleWrongState(IllegalStateException e) {
    return problem(HttpStatus.CONFLICT, "Conflict", e.getMessage());
  }

  /**
   * A request body that failed validation, answered with the offending fields
   * so a form can show the message next to the right input.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
    Map<String, String> errors = new LinkedHashMap<>();
    for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
      errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
    }

    ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Invalid request",
        "Some of the details are missing or malformed.");
    problem.setProperty("errors", errors);
    return problem;
  }

  private static ProblemDetail problem(HttpStatus status, String title, String detail) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(title);
    return problem;
  }
}
