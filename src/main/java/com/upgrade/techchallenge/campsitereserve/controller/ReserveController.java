package com.upgrade.techchallenge.campsitereserve.controller;

import com.upgrade.techchallenge.campsitereserve.dto.CampsiteAvailability;
import com.upgrade.techchallenge.campsitereserve.dto.ReserveRequest;
import com.upgrade.techchallenge.campsitereserve.dto.ReserveResponse;
import com.upgrade.techchallenge.campsitereserve.exception.ReserveRequestParameterException;
import com.upgrade.techchallenge.campsitereserve.service.CampsiteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value="startDate") LocalDate startDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value="endDate") LocalDate endDate
    ) {
        List<LocalDate> availList = campsiteService.getAvailableDates(startDate, endDate);
        return new ResponseEntity<>(new CampsiteAvailability(availList), HttpStatus.OK);
    }

    @PostMapping(path = "reserve", produces = "application/json")
    public ResponseEntity<ReserveResponse> reserve(
            @Valid @RequestBody ReserveRequest reserveRequest
    ) throws ReserveRequestParameterException {
        reserveRequest.validate();
        return null;
    }
}