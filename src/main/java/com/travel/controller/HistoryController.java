package com.travel.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.travel.entity.TourBooking;
import com.travel.repository.TourBookingRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class HistoryController {

    private final TourBookingRepository bookingRepository;

    public HistoryController(
            TourBookingRepository bookingRepository) {

        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/booking-history")
    public String history(
            HttpSession session,
            Model model) {

        String username =
                (String) session.getAttribute("username");

        List<TourBooking> bookings =
                bookingRepository.findByUsername(username);

        model.addAttribute("bookings", bookings);

        return "history";
    }
}