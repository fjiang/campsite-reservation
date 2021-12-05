package com.upgrade.techchallenge.campsitereserve.service;

import com.upgrade.techchallenge.campsitereserve.repository.DateAvailabilityRepository;
import com.upgrade.techchallenge.campsitereserve.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CampsiteService {
    @Autowired
    DateAvailabilityRepository dateAvailabilityRepository;

    @Autowired
    UserRepository userRepository;

    public List<LocalDate> getAvailableDates(LocalDate startDate, LocalDate endDate) {

        return null;
    }
}
