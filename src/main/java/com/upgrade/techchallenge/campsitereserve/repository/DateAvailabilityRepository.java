package com.upgrade.techchallenge.campsitereserve.repository;

import com.upgrade.techchallenge.campsitereserve.domain.DateAvailability;
import com.upgrade.techchallenge.campsitereserve.domain.CampsiteStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface DateAvailabilityRepository extends CrudRepository<DateAvailability, Long> {

    List<DateAvailability> findByAvailability(CampsiteStatus campsiteStatus);

    Optional<DateAvailability> findById(Long id);
}
