package com.upgrade.techchallenge.campsitereserve.exception;

import com.upgrade.techchallenge.campsitereserve.error.BaseServiceError;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class CampsiteReserveException extends Exception {
    private final HttpStatus httpStatus;
    private final String innerMessage;
    private final BaseServiceError serviceError;

    CampsiteReserveException(String message, String innerMessage, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
        this.innerMessage = innerMessage;
        this.serviceError = createServiceError();
    }

    protected abstract BaseServiceError createServiceError();
}
