package com.example.seatreservation.service;

import com.example.seatreservation.entities.SeatReservation;
import com.example.seatreservation.repositories.SeatReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SeatReservationService {

    @Autowired
    private SeatReservationRepository repository;

    private static final char[] COLUMNS = {'A', 'B', 'C', 'D'};
    private static final int ROWS = 4;

    public SeatReservation reserveSeat(String seatCode, String customerName) throws IllegalStateException {
        // Check if the seat is available
        Optional<SeatReservation> existingReservation = repository.findAll().stream()
                .filter(reservation -> reservation.getSeatCode().equals(seatCode))
                .findFirst();
        if (existingReservation.isPresent()) {
            throw new IllegalStateException("Seat " + seatCode + " is already reserved.");
        }

        // Generate a transaction number (for example, using UUID or based on ID)
        String transactionNumber = "TXN-" + java.util.UUID.randomUUID().toString();

        // Proceed with reservation
        SeatReservation reservation = new SeatReservation();
        reservation.setSeatCode(seatCode);
        reservation.setCustomerName(customerName);
        reservation.setTransactionDate(LocalDate.now());
        reservation.setTransactionNumber(transactionNumber);
        return repository.save(reservation);
    }

    public List<SeatReservation> getAllReservations() {
        return repository.findAll();
    }

    public Map<String, String> getSeatMatrix() {
        Map<String, String> seatMap = new HashMap<>();

        // Initialize all seats as available
        for (char col : COLUMNS) {
            for (int row = 1; row <= ROWS; row++) {
                String seatCode = col + String.valueOf(row);
                seatMap.put(seatCode, "Available");
            }
        }

        // Update reserved seats with the customer's name
        List<SeatReservation> reservations = repository.findAll();
        for (SeatReservation reservation : reservations) {
            seatMap.put(reservation.getSeatCode(), reservation.getCustomerName());
        }

        return seatMap;
    }

    public long countAvailableSeats() {
        return getSeatMatrix().values().stream()
                .filter(value -> value.equals("Available"))
                .count();
    }

    public Optional<SeatReservation> findReservationById(Long id) {
        return repository.findById(id);
    }

    public void updateReservation(Long id, String seatCode, String customerName) throws IllegalStateException {
        // Check if the new seat code is available if it has been changed
        Optional<SeatReservation> existingReservation = repository.findAll().stream()
                .filter(reservation -> reservation.getSeatCode().equals(seatCode) && !reservation.getId().equals(id))
                .findFirst();
        if (existingReservation.isPresent()) {
            throw new IllegalStateException("Seat " + seatCode + " is already reserved.");
        }

        // Update reservation
        Optional<SeatReservation> reservationOptional = repository.findById(id);
        if (reservationOptional.isPresent()) {
            SeatReservation reservation = reservationOptional.get();
            reservation.setSeatCode(seatCode);
            reservation.setCustomerName(customerName);
            repository.save(reservation);
        }
    }

    public void deleteReservation(Long id) {
        repository.deleteById(id);
    }
}
