package com.example.seatreservation.repositories;

import com.example.seatreservation.entities.SeatReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {
    // Custom query methods can be added here if needed
}
