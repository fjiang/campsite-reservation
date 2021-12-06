package com.upgrade.techchallenge.campsitereserve.controller;

import com.upgrade.techchallenge.campsitereserve.dto.CampsiteAvailability;
import com.upgrade.techchallenge.campsitereserve.dto.DateAvailabilityRequest;
import com.upgrade.techchallenge.campsitereserve.dto.ReserveRequest;
import com.upgrade.techchallenge.campsitereserve.dto.ReserveResponse;
import com.upgrade.techchallenge.campsitereserve.exception.InternalServerException;
import com.upgrade.techchallenge.campsitereserve.exception.ReserveRequestParameterException;
import com.upgrade.techchallenge.campsitereserve.service.CampsiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(path="/campsite/")
public class ReserveController {

    @Autowired
    private CampsiteService campsiteService;

    @GetMapping(path = "availability", produces = "application/json")
    public ResponseEntity<CampsiteAvailability> checkCampsiteAvailability(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value="startDate", required=false) LocalDate startDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value="endDate", required=false) LocalDate endDate
    ) throws InternalServerException {
        DateAvailabilityRequest dateAvailabilityRequest = new DateAvailabilityRequest();
        if (startDate != null) dateAvailabilityRequest.setStartDate(startDate);
        if (endDate != null) dateAvailabilityRequest.setEndDate(endDate);
        List<LocalDate> availList = campsiteService.getAvailable(dateAvailabilityRequest);
        return new ResponseEntity<>(new CampsiteAvailability(availList), HttpStatus.OK);
    }

    @PostMapping(path = "reserve", produces = "application/json")
    public ResponseEntity<ReserveResponse> reserve(
            @Valid @RequestBody ReserveRequest reserveRequest
    ) throws ReserveRequestParameterException, InternalServerException {
        reserveRequest.validate();
        return new ResponseEntity<>(campsiteService.reserve(reserveRequest), HttpStatus.OK);
    }
}
