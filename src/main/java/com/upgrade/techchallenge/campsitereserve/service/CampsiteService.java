package com.upgrade.techchallenge.campsitereserve.service;

import com.upgrade.techchallenge.campsitereserve.domain.CampsiteStatus;
import com.upgrade.techchallenge.campsitereserve.domain.DateAvailability;
import com.upgrade.techchallenge.campsitereserve.domain.User;
import com.upgrade.techchallenge.campsitereserve.dto.*;
import com.upgrade.techchallenge.campsitereserve.error.ServiceError400;
import com.upgrade.techchallenge.campsitereserve.exception.InternalServerException;
import com.upgrade.techchallenge.campsitereserve.repository.DateAvailabilityRepository;
import com.upgrade.techchallenge.campsitereserve.repository.UserRepository;
import com.upgrade.techchallenge.campsitereserve.utils.TrackIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Service for campsite reserve endpoints mapped functions
 */
@Service
public class CampsiteService {

    private Logger logger = LoggerFactory.getLogger(CampsiteService.class);

    @Autowired
    private DateAvailabilityRepository dateAvailabilityRepository;

    @Autowired
    private UserRepository userRepository;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Get available dates against start and end date
     * @param request includes start and end date
     * @return List of local date format available dates
     * @throws InternalServerException when timeout or interrupted waiting lock or other internal error happens
     */
    public List<LocalDate> getAvailable(DateAvailabilityRequest request)
            throws InternalServerException {
        List<DateAvailability> campsiteAvailabilities;
        try {
            boolean acquired = lock.readLock().tryLock(2000, TimeUnit.MILLISECONDS);
            if (!acquired) {
                String errorMessage = "Timeout acquiring available dates";
                logger.error(errorMessage);
                throw new InternalServerException(errorMessage);
            }
            LocalDate startDate = request.getStartDate();
            LocalDate endDate = request.getEndDate();
            campsiteAvailabilities =
                    dateAvailabilityRepository.findDatesBetween(CampsiteStatus.AVAILABLE, startDate, endDate);
        } catch (InterruptedException ex) {
            String errorMessage = "Interrupted acquiring available dates";
            logger.error(errorMessage, ex);
            throw new InternalServerException(errorMessage);
        } finally {
            lock.readLock().unlock();
        }

        return campsiteAvailabilities.stream().map(DateAvailability::getDate).collect(Collectors.toList());
    }

    /**
     * Reserve campsite against provided user info and start/end date
     * @param reserveRequest includes user info and start/end date
     * @return  Reserve response including booking id if succeeded, or list of errors if failed
     * @throws InternalServerException throw when interrupted or timeout waiting for lock, or
     * other internal errors happened
     */
    @Transactional
    public ReserveResponse reserve(ReserveRequest reserveRequest)
            throws InternalServerException {
        try {
            boolean acquired = lock.writeLock().tryLock(5000, TimeUnit.MILLISECONDS);
            if (!acquired) {
                String innerMessage = String.format("Timeout reserving campsite - [%s]", reserveRequest);
                throw new InternalServerException("Timeout reserving campsite", innerMessage);
            }
            LocalDate startDate = reserveRequest.getStartDate();
            LocalDate endDate = reserveRequest.getEndDate();
            List<DateAvailability> dateAvailabilities = dateAvailabilityRepository.findDatesBetween(
                    CampsiteStatus.AVAILABLE, startDate, endDate);
            if (dateAvailabilities.size() - 1 != DAYS.between(startDate, endDate)) {
                String errorMessage = "Date has already booked";
                return new ReserveResponse(
                        ProcessingStatus.FAILED, List.of(new ServiceError400(errorMessage)), null);
            } else {
                User user = userRepository.findOneByEmail(reserveRequest.getEmail());
                if (user == null) {
                    user = new User(reserveRequest.getFirstName(),
                                    reserveRequest.getLastName(),
                                    reserveRequest.getEmail());
                    userRepository.save(user);
                }
                String trackId = reserve(dateAvailabilities, user);
                return new ReserveResponse(
                        ProcessingStatus.SUCCEEDED, null, trackId);
            }
        } catch (InterruptedException ex) {
            String innerErrorMessage = String.format("Interrupted reserving campsite - [%s]", reserveRequest);
            throw new InternalServerException("Interrupted reserving campsite", innerErrorMessage);
        } catch (Exception ex) {
            String errorMessage = "Encountered internal error";
            throw new InternalServerException(errorMessage, errorMessage);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private String reserve(List<DateAvailability> dateAvailabilities, User user) {
        String bookingId = TrackIdGenerator.generateTrackId();

        for (DateAvailability dateAvailability : dateAvailabilities) {
            dateAvailability.setCampsiteStatus(CampsiteStatus.RESERVED);
            dateAvailability.setUser(user);
            dateAvailability.setBookingId(bookingId);
        }
        dateAvailabilityRepository.saveAll(dateAvailabilities);

        logger.info("User info found with findByAvailability()");
        logger.info("-----------------------------------------");
        for (DateAvailability dateAvailabilityFetched : dateAvailabilityRepository.findAll()) {
            logger.info(dateAvailabilityFetched.toString());
        }
        logger.info("");
        return bookingId;
    }

    /**
     * Change previous reservation (either cancel or update)
     * @param changeRequest including booking id and change operation and start/end date if update
     * @return ChangeResponse including processing status and errors if failed, new booking id if succeeded
     * @throws InternalServerException when timeout or interrupted waiting for lock or other internal error happens
     */
    @Transactional
    public ChangeResponse change(ChangeRequest changeRequest)
            throws InternalServerException {
        try {
            boolean acquired = lock.writeLock().tryLock(5000, TimeUnit.MILLISECONDS);
            if (!acquired) {
                String errorMessage = String.format("Timeout changing reservation - [%s]", changeRequest);
                logger.error(errorMessage);
                throw new InternalServerException("Timeout changing reservation");
            }

            // Look for reservation by booking id
            List<DateAvailability> dateAvailabilities =
                    dateAvailabilityRepository.findDatesByBookingId(changeRequest.getBookingId());
            if (CollectionUtils.isEmpty(dateAvailabilities)) {
                String errorMessage = String.format("Can't find reservation by booking id %s", changeRequest.getBookingId());
                return new ChangeResponse(
                        ProcessingStatus.FAILED, List.of(new ServiceError400(errorMessage)), null);
            }

            // Cancel previous reservation
            User user = dateAvailabilities.get(0).getUser();
            for (DateAvailability dateAvailability : dateAvailabilities) {
                dateAvailability.setUser(null);
                dateAvailability.setCampsiteStatus(CampsiteStatus.AVAILABLE);
                dateAvailability.setBookingId(null);
            }
            dateAvailabilityRepository.saveAll(dateAvailabilities);

            // Reserve for new schedule
            if (changeRequest.getChangeReserveOperation() == ChangeReserveOperation.UPDATE) {
                List<DateAvailability> newDateAvailabilities = dateAvailabilityRepository.findDatesBetween(
                        CampsiteStatus.AVAILABLE, changeRequest.getStartDate(), changeRequest.getEndDate());
                if (newDateAvailabilities.size() - 1 != DAYS.between(changeRequest.getStartDate(), changeRequest.getEndDate())) {
                    String errorMessage = "Date has already booked";
                    return new ChangeResponse(
                            ProcessingStatus.FAILED, List.of(new ServiceError400(errorMessage)), null);
                } else {
                    String bookingId = reserve(newDateAvailabilities, user);
                    return new ChangeResponse(
                            ProcessingStatus.SUCCEEDED, null, bookingId);
                }
            } else { // Cancel request
                return new ChangeResponse(
                        ProcessingStatus.SUCCEEDED, null, null);
            }
        } catch (InterruptedException ex) {
            String innerErrorMessage = String.format("Interrupted changing reservation - [%s]", changeRequest);
            throw new InternalServerException("Interrupted changing reservation", innerErrorMessage);
        } catch (Exception ex) {
            String errorMessage = "Encountered internal error";
            throw new InternalServerException(errorMessage, errorMessage);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
