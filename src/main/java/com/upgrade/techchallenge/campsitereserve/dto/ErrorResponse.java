package com.upgrade.techchallenge.campsitereserve.dto;

import lombok.Data;

import java.util.List;

@Data
public class ErrorResponse {
    private List<Error> errors;

}
