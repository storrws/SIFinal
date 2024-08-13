package com.example.seatreservation.web;

import com.example.seatreservation.entities.SeatReservation;
import com.example.seatreservation.service.SeatReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class SeatReservationController {

    @Autowired
    private SeatReservationService service;

    @GetMapping("/")
    public String showReservationForm(Model model) {
        model.addAttribute("reservations", service.getAllReservations());
        model.addAttribute("seatMatrix", service.getSeatMatrix());
        model.addAttribute("availableSeatsCount", service.countAvailableSeats());
        return "reservation";
    }

    @PostMapping("/reserve")
    public String reserveSeat(@RequestParam String seatCode,
                              @RequestParam String customerName,
                              Model model) {
        // Normalize and validate seat code
        seatCode = seatCode.toUpperCase();
        if (!seatCode.matches("[A-D][1-4]")) {
            model.addAttribute("errorMessage", "Invalid seat code. Please enter a code between A1 and D4.");
            return showReservationForm(model);
        }

        try {
            service.reserveSeat(seatCode, customerName);
            return "redirect:/";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return showReservationForm(model);
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Optional<SeatReservation> reservation = service.findReservationById(id);
        if (reservation.isPresent()) {
            model.addAttribute("reservation", reservation.get());
            return "edit-reservation";
        } else {
            return "redirect:/";
        }
    }


    @PostMapping("/update/{id}")
    public String updateReservation(@PathVariable("id") Long id,
                                    @RequestParam String seatCode,
                                    @RequestParam String customerName,
                                    Model model) {
        try {
            service.updateReservation(id, seatCode, customerName);
            return "redirect:/";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "edit-reservation";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteReservation(@PathVariable("id") Long id) {
        service.deleteReservation(id);
        return "redirect:/";
    }
}
