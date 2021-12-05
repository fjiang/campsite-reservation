package com.upgrade.techchallenge.campsitereserve.exception;

import com.upgrade.techchallenge.campsitereserve.error.BaseServiceError;
import com.upgrade.techchallenge.campsitereserve.error.ServiceError400;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ServiceError400 methodArgumentNotValidException(final MethodArgumentNotValidException ex) {
        final List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        ServiceError400 serviceError400 = new ServiceError400("Validation error");
        for (FieldError fieldError: fieldErrors) {
            serviceError400.addError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        logError(serviceError400, ex);
        return serviceError400;
    }

    @ResponseBody
    @ExceptionHandler(ReserveRequestParameterException.class)
    public BaseServiceError reserveRequestParameterException(HttpServletResponse response,
                                                            final ReserveRequestParameterException ex) {
        logError(ex.getServiceError(), ex);
        response.setStatus(ex.getHttpStatus().value());
        return ex.getServiceError();
    }

    private void logError(BaseServiceError serviceError, Exception ex) {
        logger.error("Error Code {}, Error Message {}",
                serviceError.getErrorCode(), serviceError.getErrorMessage(), ex);
    }
}
