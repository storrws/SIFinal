package com.example.seatreservation.service;

import com.example.seatreservation.entities.SeatReservation;
import com.example.seatreservation.repositories.SeatReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SeatReservationServiceTest {

    @Mock
    private SeatReservationRepository seatReservationRepository;

    @InjectMocks
    private SeatReservationService seatReservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllReservations() {
        // Setup mock repository
        when(seatReservationRepository.findAll()).thenReturn(List.of(
                new SeatReservation(1L, "A1", "John Doe", LocalDate.now(), "TXN-12345"),
                new SeatReservation(2L, "B2", "Jane Doe", LocalDate.now(), "TXN-12346")
        ));

        // Execute service method
        var reservations = seatReservationService.getAllReservations();

        // Assertions
        assertNotNull(reservations);
        assertEquals(2, reservations.size());
        assertEquals("John Doe", reservations.get(0).getCustomerName());
        assertEquals("Jane Doe", reservations.get(1).getCustomerName());

        // Verify repository interaction
        verify(seatReservationRepository, times(1)).findAll();
    }

    @Test
    void testReserveSeat_Success() {
        // Setup mock repository to return an empty result when searching for an existing reservation
        when(seatReservationRepository.findAll()).thenReturn(List.of());

        // Execute service method
        SeatReservation reservation = seatReservationService.reserveSeat("A1", "John Doe");

        // Assertions
        assertNotNull(reservation);
        assertEquals("A1", reservation.getSeatCode());
        assertEquals("John Doe", reservation.getCustomerName());
        assertNotNull(reservation.getTransactionNumber());

        // Verify repository interaction
        verify(seatReservationRepository, times(1)).save(any(SeatReservation.class));
    }

    @Test
    void testReserveSeat_SeatAlreadyReserved() {
        // Setup mock repository to simulate a reserved seat
        when(seatReservationRepository.findAll()).thenReturn(List.of(
                new SeatReservation(1L, "A1", "Jane Doe", LocalDate.now(), "TXN-12345")
        ));

        // Execute service method and expect an exception
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            seatReservationService.reserveSeat("A1", "John Doe");
        });

        // Assertions
        assertEquals("Seat A1 is already reserved.", exception.getMessage());

        // Verify repository interaction
        verify(seatReservationRepository, never()).save(any(SeatReservation.class));
    }

    @Test
    void testUpdateReservation_Success() {
        // Setup mock repository to return an existing reservation
        SeatReservation existingReservation = new SeatReservation(1L, "A1", "Jane Doe", LocalDate.now(), "TXN-12345");
        when(seatReservationRepository.findById(1L)).thenReturn(Optional.of(existingReservation));

        // Execute service method
        seatReservationService.updateReservation(1L, "A1", "John Doe");

        // Assertions
        assertEquals("John Doe", existingReservation.getCustomerName());

        // Verify repository interaction
        verify(seatReservationRepository, times(1)).save(existingReservation);
    }

    @Test
    void testUpdateReservation_SeatAlreadyReserved() {
        // Setup mock repository to simulate a conflict in seat codes during update
        SeatReservation existingReservation = new SeatReservation(1L, "A1", "Jane Doe", LocalDate.now(), "TXN-12345");
        when(seatReservationRepository.findById(1L)).thenReturn(Optional.of(existingReservation));
        when(seatReservationRepository.findAll()).thenReturn(List.of(
                new SeatReservation(2L, "B2", "John Doe", LocalDate.now(), "TXN-12346")
        ));

        // Execute service method and expect an exception
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            seatReservationService.updateReservation(1L, "B2", "Jane Doe");
        });

        // Assertions
        assertEquals("Seat B2 is already reserved.", exception.getMessage());

        // Verify repository interaction
        verify(seatReservationRepository, never()).save(any(SeatReservation.class));
    }

    @Test
    void testDeleteReservation_Success() {
        // Execute service method
        seatReservationService.deleteReservation(1L);

        // Verify repository interaction
        verify(seatReservationRepository, times(1)).deleteById(1L);
    }
}
