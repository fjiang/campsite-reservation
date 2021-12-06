package com.upgrade.techchallenge.campsitereserve.exception;

import com.upgrade.techchallenge.campsitereserve.error.BaseServiceError;
import com.upgrade.techchallenge.campsitereserve.error.ServiceError400;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class ReserveRequestParameterException extends CampsiteReserveException {
    private final Map<String, String> fieldErrors;

    public ReserveRequestParameterException(String message, Map<String, String> fieldErrors) {
        this(message, null, fieldErrors);
    }

    private ReserveRequestParameterException(String message, String innerMessage, Map<String, String> fieldErrors) {
        super(message, innerMessage, HttpStatus.BAD_REQUEST);
        this.fieldErrors = fieldErrors;
        constructServiceError();
    }

    @Override
    protected BaseServiceError createServiceError() {
        return new ServiceError400(getMessage());
    }

    private void constructServiceError() {
        ServiceError400 serviceError400 = (ServiceError400)getServiceError();
        for (Map.Entry<String, String> entry : fieldErrors.entrySet()) {
            serviceError400.addError(entry.getKey(), entry.getValue());
        }
    }
}
