package com.upgrade.techchallenge.campsitereserve.error;

import lombok.Data;

@Data
class FieldError {
    private String field;
    private String message;

    FieldError(String field, String message) {
        this.field = field;
        this.message = message;
    }
}
