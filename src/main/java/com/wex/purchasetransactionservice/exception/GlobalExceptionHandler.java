package com.wex.purchasetransactionservice.exception;

import com.wex.purchasetransactionservice.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String RETRY_AFTER_SECONDS = "30";

    @ExceptionHandler(PurchaseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(PurchaseNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.of(HttpStatus.NOT_FOUND, List.of(ex.getMessage()))
        );
    }

    @ExceptionHandler(ExchangeRateUnAvailableException.class)
    public ResponseEntity<ErrorResponse> handleNoRate(ExchangeRateUnAvailableException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(
                ErrorResponse.of(HttpStatus.UNPROCESSABLE_CONTENT, List.of(ex.getMessage()))
        );
    }

    @ExceptionHandler(CurrencyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCurrencyNotFound(CurrencyNotFoundException ex) {
        return ResponseEntity.badRequest().body(
                ErrorResponse.of(HttpStatus.BAD_REQUEST, List.of(ex.getMessage()))
        );
    }

    @ExceptionHandler(CurrencyDataUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleCurrencyDataUnavailable(CurrencyDataUnavailableException ex) {
        log.warn("Currency data unavailable: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(HttpHeaders.RETRY_AFTER, RETRY_AFTER_SECONDS)
                .body(ErrorResponse.of(HttpStatus.SERVICE_UNAVAILABLE, List.of(ex.getMessage())));
    }

    @ExceptionHandler(TreasuryServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleTreasuryUnavailable(TreasuryServiceUnavailableException ex) {
        log.warn("Treasury API unavailable: {}", ex.getMessage(), ex.getCause());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(HttpHeaders.RETRY_AFTER, RETRY_AFTER_SECONDS)
                .body(ErrorResponse.of(HttpStatus.SERVICE_UNAVAILABLE, List.of(ex.getMessage())));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(HttpStatus.BAD_REQUEST,
                List.of("Bad request body")));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errorMessages = ex.getBindingResult().getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
        return ResponseEntity.badRequest().body(ErrorResponse.of(HttpStatus.BAD_REQUEST, errorMessages));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, List.of("An unexpected error occurred"))
        );
    }
}
