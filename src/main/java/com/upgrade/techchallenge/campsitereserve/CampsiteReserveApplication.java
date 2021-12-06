package com.upgrade.techchallenge.campsitereserve;

import com.upgrade.techchallenge.campsitereserve.domain.DateAvailability;
import com.upgrade.techchallenge.campsitereserve.domain.CampsiteStatus;
import com.upgrade.techchallenge.campsitereserve.domain.User;
import com.upgrade.techchallenge.campsitereserve.repository.DateAvailabilityRepository;
import com.upgrade.techchallenge.campsitereserve.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.time.LocalDate;

@SpringBootApplication
public class CampsiteReserveApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampsiteReserveApplication.class, args);
	}

}
