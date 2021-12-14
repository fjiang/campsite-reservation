package com.upgrade.techchallenge.campsitereserve.service;

import com.upgrade.techchallenge.campsitereserve.domain.CampsiteStatus;
import com.upgrade.techchallenge.campsitereserve.domain.DateAvailability;
import com.upgrade.techchallenge.campsitereserve.domain.User;
import com.upgrade.techchallenge.campsitereserve.dto.*;
import com.upgrade.techchallenge.campsitereserve.error.ServiceError400;
import com.upgrade.techchallenge.campsitereserve.error.ServiceError500;
import com.upgrade.techchallenge.campsitereserve.exception.InternalServerException;
import com.upgrade.techchallenge.campsitereserve.repository.DateAvailabilityRepository;
import com.upgrade.techchallenge.campsitereserve.repository.UserRepository;
import com.upgrade.techchallenge.campsitereserve.utils.TrackIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.LinkedList;
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

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    private TransactionTemplate transactionTemplate;

    private final ReadWriteLock[] locks = new ReadWriteLock[30];

    @PostConstruct
    public void initialize() {
        transactionTemplate = new TransactionTemplate(platformTransactionManager);
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new ReentrantReadWriteLock();
        }
    }

    /**
     * Get available dates against start and end date
     * @param request includes start and end date
     * @return List of local date format available dates
     * @throws InternalServerException when timeout or interrupted waiting lock or other internal error happens
     */
    public List<LocalDate> getAvailable(DateAvailabilityRequest request)
            throws InternalServerException {
        List<DateAvailability> campsiteAvailabilities;
        List<ReadWriteLock> acquiredLocks = new LinkedList<>();
        try {
            LocalDate startDate = request.getStartDate();
            LocalDate endDate = request.getEndDate();
            int firstLockIdx = Long.valueOf(DAYS.between(LocalDate.now().plusDays(1), startDate)).intValue();
            int lastLockIdx = Long.valueOf(DAYS.between(LocalDate.now().plusDays(1), endDate)).intValue();

            for (int i = firstLockIdx; i <= lastLockIdx; i++) {
                boolean acquired = locks[i].readLock().tryLock(2000, TimeUnit.MILLISECONDS);
                if (!acquired) {
                    String errorMessage = "Timeout acquiring available dates";
                    logger.error(errorMessage);
                    throw new InternalServerException(errorMessage);
                } else {
                    acquiredLocks.add(locks[i]);
                }
            }
            campsiteAvailabilities =
                    dateAvailabilityRepository.findDatesBetween(CampsiteStatus.AVAILABLE, startDate, endDate);
        } catch (InterruptedException ex) {
            String errorMessage = "Interrupted acquiring available dates";
            logger.error(errorMessage, ex);
            throw new InternalServerException(errorMessage);
        } finally {
            acquiredLocks.forEach(lock -> lock.readLock().unlock());
        }

        return campsiteAvailabilities.stream().map(DateAvailability::getDate).collect(Collectors.toList());
    }

    /**
     * Reserve campsite against provided user info and start/end date.
     * Can't use transaction annotation because then it is possible to only flush after write lock unlocked.
     * Use transactionTemplate to programmatically control transaction instead.
     * @param reserveRequest includes user info and start/end date
     * @return  Reserve response including booking id if succeeded, or list of errors if failed
     * @throws InternalServerException throw when interrupted or timeout waiting for lock, or
     * other internal errors happened
     */
    public ReserveResponse reserve(ReserveRequest reserveRequest)
            throws InternalServerException {
        List<ReadWriteLock> acquiredLocks = new LinkedList<>();
        try {
            LocalDate startDate = reserveRequest.getStartDate();
            LocalDate endDate = reserveRequest.getEndDate();
            int firstLockIdx = Long.valueOf(DAYS.between( LocalDate.now().plusDays(1), startDate)).intValue();
            int lastLockIdx = Long.valueOf(DAYS.between(LocalDate.now().plusDays(1), endDate)).intValue();

            for (int i = firstLockIdx; i <= lastLockIdx; i++) {
                boolean acquired = locks[i].writeLock().tryLock(5000, TimeUnit.MILLISECONDS);
                if (!acquired) {
                    String innerMessage = String.format("Timeout reserving campsite - [%s]", reserveRequest);
                    throw new InternalServerException("Timeout reserving campsite", innerMessage);
                } else {
                    acquiredLocks.add(locks[i]);
                }
            }

            List<DateAvailability> dateAvailabilities = dateAvailabilityRepository.findDatesBetween(
                    CampsiteStatus.AVAILABLE, startDate, endDate);

            if (dateAvailabilities.size() - 1 != DAYS.between(startDate, endDate)) {
                String errorMessage = "Date has already booked";
                return new ReserveResponse(
                        ProcessingStatus.FAILED, List.of(new ServiceError400(errorMessage)), null);
            } else {
                return transactionTemplate.execute( status -> {
                        User user = userRepository.findOneByEmail(reserveRequest.getEmail());
                        if (user == null) {
                            user = new User(reserveRequest.getFirstName(),
                                    reserveRequest.getLastName(),
                                    reserveRequest.getEmail());
                            userRepository.save(user);
                        }
                        return reserve(dateAvailabilities, user);

                });
            }
        } catch (InterruptedException ex) {
            String innerErrorMessage = String.format("Interrupted reserving campsite - [%s]", reserveRequest);
            throw new InternalServerException("Interrupted reserving campsite", innerErrorMessage);
        } catch (Exception ex) {
            String errorMessage = "Encountered internal error";
            throw new InternalServerException(errorMessage, ex.getMessage());
        } finally {
            acquiredLocks.forEach(lock -> lock.writeLock().unlock());
        }
    }

    private ReserveResponse reserve(List<DateAvailability> dateAvailabilities, User user) {
        String bookingId = TrackIdGenerator.generateTrackId();
        List<ReadWriteLock> acquiredLocks = new LinkedList<>();
        LocalDate startDate = dateAvailabilities.get(0).getDate();
        LocalDate endDate = dateAvailabilities.get(dateAvailabilities.size() - 1).getDate();
        int firstLockIdx = Long.valueOf(DAYS.between(LocalDate.now().plusDays(1), startDate)).intValue();
        int lastLockIdx = Long.valueOf(DAYS.between(LocalDate.now().plusDays(1), endDate)).intValue();
        try {
            for (int i = firstLockIdx; i <= lastLockIdx; i++) {
                boolean acquired = locks[i].writeLock().tryLock(5000, TimeUnit.MILLISECONDS);
                if (!acquired) {
                    String errorMessage = String.format("Timeout reserve campsite - between [%s] and [%s]",startDate, endDate);
                    return new ReserveResponse(ProcessingStatus.FAILED, List.of(new ServiceError500(errorMessage)), null);
                } else {
                    acquiredLocks.add(locks[i]);
                }
            }
            for (DateAvailability dateAvailability : dateAvailabilities) {
                dateAvailability.setCampsiteStatus(CampsiteStatus.RESERVED);
                dateAvailability.setUser(user);
                dateAvailability.setBookingId(bookingId);
            }
            dateAvailabilityRepository.saveAll(dateAvailabilities);
            return new ReserveResponse(ProcessingStatus.SUCCEEDED, null, bookingId);
        } catch (InterruptedException ex) {
            String errorMessage = String.format("Timeout reserve campsite - between [%s] and [%s]",startDate, endDate);
            return new ReserveResponse(ProcessingStatus.FAILED, List.of(new ServiceError500(errorMessage)), null);
        } finally {
            acquiredLocks.forEach(lock -> lock.writeLock().unlock());
        }
    }

    /**
     * Change previous reservation (either cancel or change)
     * Can't use transaction annotation because then it is possible to only flush after write lock unlocked.
     * Use transactionTemplate to programmatically control transaction instead.
     * @param changeRequest including booking id and change operation and start/end date if update
     * @return ChangeResponse including processing status and errors if failed, new booking id if succeeded
     * @throws InternalServerException when timeout or interrupted waiting for lock or other internal error happens
     */
    public ChangeResponse change(ChangeRequest changeRequest) {
        try {
            // Look for reservation by booking id
            List<DateAvailability> dateAvailabilities =
                    dateAvailabilityRepository.findDatesByBookingId(changeRequest.getBookingId());
            if (CollectionUtils.isEmpty(dateAvailabilities)) {
                String errorMessage = String.format("Can't find reservation by booking id %s", changeRequest.getBookingId());
                return new ChangeResponse(
                        ProcessingStatus.FAILED, List.of(new ServiceError400(errorMessage)), null);
            }

            if (changeRequest.getChangeReserveOperation() == ChangeReserveOperation.CANCEL) {
                cancelReservation(dateAvailabilities);
                return new ChangeResponse(
                        ProcessingStatus.SUCCEEDED, null, null);
            } else {
                return transactionTemplate.execute( status -> {
                    try {
                        cancelReservation(dateAvailabilities);
                        List<DateAvailability> newDateAvailabilities = dateAvailabilityRepository.findDatesBetween(
                                CampsiteStatus.AVAILABLE, changeRequest.getStartDate(), changeRequest.getEndDate());
                        if (newDateAvailabilities.size() - 1 != DAYS.between(changeRequest.getStartDate(), changeRequest.getEndDate())) {
                            // Roll back cancel reservation
                            status.setRollbackOnly();
                            String errorMessage = "Date has already booked";
                            return new ChangeResponse(
                                    ProcessingStatus.FAILED, List.of(new ServiceError400(errorMessage)), null);
                        } else {
                            User user = dateAvailabilities.get(0).getUser();
                            ReserveResponse reserveResponse = reserve(newDateAvailabilities, user);
                            if (reserveResponse.getProcessingStatus() == ProcessingStatus.SUCCEEDED) {
                                return new ChangeResponse(
                                        ProcessingStatus.SUCCEEDED, null, reserveResponse.getBookingId());
                            } else {
                                return new ChangeResponse(ProcessingStatus.FAILED, reserveResponse.getErrors(), null);
                            }
                        }
                    } catch (Exception ex) {
                        status.setRollbackOnly();
                        return new ChangeResponse(
                                ProcessingStatus.FAILED, List.of(new ServiceError500("Encountered internal error")), null);
                    }
                });
            }
        } catch (Exception ex) {
            return new ChangeResponse(
                    ProcessingStatus.FAILED, List.of(new ServiceError500("Encountered internal error")), null);
        }
    }

    private void cancelReservation(List<DateAvailability> dateAvailabilities) throws InternalServerException {
        List<ReadWriteLock> acquiredLocks = new LinkedList<>();
        LocalDate startDate = dateAvailabilities.get(0).getDate();
        LocalDate endDate = dateAvailabilities.get(dateAvailabilities.size() - 1).getDate();
        int firstLockIdx = Long.valueOf(DAYS.between(LocalDate.now().plusDays(1), startDate)).intValue();
        int lastLockIdx = Long.valueOf(DAYS.between(LocalDate.now().plusDays(1), endDate)).intValue();
        try {
            for (int i = firstLockIdx; i <= lastLockIdx; i++) {
                boolean acquired = locks[i].writeLock().tryLock(5000, TimeUnit.MILLISECONDS);
                if (!acquired) {
                    String innerErrorMessage = String.format("Timeout cancel reservation - between [%s] and [%s]",startDate, endDate);
                    throw new InternalServerException("Timeout cancel reservation", innerErrorMessage);
                } else {
                    acquiredLocks.add(locks[i]);
                }
            }
            // Cancel previous reservation
            for (DateAvailability dateAvailability : dateAvailabilities) {
                dateAvailability.setUser(null);
                dateAvailability.setCampsiteStatus(CampsiteStatus.AVAILABLE);
                dateAvailability.setBookingId(null);
            }
            dateAvailabilityRepository.saveAll(dateAvailabilities);
        } catch (InterruptedException ex) {
            String innerErrorMessage = String.format("Timeout cancel reservation - between [%s] and [%s]",startDate, endDate);
            throw new InternalServerException("Timeout cancel reservation", innerErrorMessage);
        } finally {
            acquiredLocks.forEach(lock -> lock.writeLock().unlock());
        }
    }

    /**
     * Retrieve reservation dates by booking id
     * @param bookingId
     * @return Response contains start and end dates together with user info
     */
    public RetrieveResponse retrieve(String bookingId) throws InternalServerException {
        try {
            List<DateAvailability> dateAvailabilities =
                    dateAvailabilityRepository.findDatesByBookingId(bookingId);
            if (CollectionUtils.isEmpty(dateAvailabilities)) {
                String errorMessage = String.format("Can't find reservation by booking id %s", bookingId);
                return new RetrieveResponse(
                        ProcessingStatus.FAILED, List.of(new ServiceError400(errorMessage)));
            } else {
                LocalDate startDate = dateAvailabilities.get(0).getDate();
                LocalDate endDate = dateAvailabilities.get(dateAvailabilities.size() - 1).getDate();
                User user = dateAvailabilities.get(0).getUser();
                return new RetrieveResponse(ProcessingStatus.SUCCEEDED, null,
                        startDate, endDate, user.getFirstName(), user.getLastName(), user.getEmail());
            }
        } catch (Exception ex) {
            throw new InternalServerException("Encountered internal server error", ex.getMessage());

        }

    }
}
