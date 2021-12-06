package com.upgrade.techchallenge.campsitereserve.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
public class DateAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private LocalDate date;
    private String bookingId;
    private CampsiteStatus campsiteStatus;
    @ManyToOne
    @JoinColumn(name="USER_ID")
    private User user;

    public DateAvailability(LocalDate date, CampsiteStatus campsiteStatus, User user) {
        this.date = date;
        this.campsiteStatus = campsiteStatus;
        this.user = user;
    }
}
