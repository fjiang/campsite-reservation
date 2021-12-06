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
    public ServiceError400 methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        final List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        ServiceError400 serviceError400 = new ServiceError400("Validation error");
        for (FieldError fieldError: fieldErrors) {
            serviceError400.addError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        logError(null, serviceError400, ex);
        return serviceError400;
    }

    @ResponseBody
    @ExceptionHandler(ReserveRequestParameterException.class)
    public BaseServiceError reserveRequestParameterException(HttpServletResponse response, ReserveRequestParameterException ex) {
        logError(ex.getInnerMessage(), ex.getServiceError(), ex);
        response.setStatus(ex.getHttpStatus().value());
        return ex.getServiceError();
    }

    @ResponseBody
    @ExceptionHandler(InternalServerException.class)
    public BaseServiceError internalServerException(HttpServletResponse response, InternalServerException ex) {
        logError(ex.getInnerMessage(), ex.getServiceError(), ex);
        response.setStatus(ex.getHttpStatus().value());
        return ex.getServiceError();
    }

    private void logError(String innerMessage, BaseServiceError serviceError, Exception ex) {
        if (innerMessage != null) {
            logger.error(innerMessage);
        }
        logger.error("Error Code {}, Error Message {}, Track Id {}",
                serviceError.getErrorCode(), serviceError.getErrorMessage(), serviceError.getTrackId(), ex);
    }
}
