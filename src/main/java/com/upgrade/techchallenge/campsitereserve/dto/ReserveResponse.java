package com.upgrade.techchallenge.campsitereserve.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReserveResponse {

    private Long trackId;

    private ReserveStatus status;

    private LocalDate startDate;

    private LocalDate endDate;



}
