package com.travel.controller;

import com.travel.service.TourBookingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminController {

    private final TourBookingService bookingService;

    public AdminController(TourBookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/admin")
    public String adminDashboard(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");

        if (username == null) {
            return "redirect:/login";
        }

        if (!username.equalsIgnoreCase("admin")) {
            return "redirect:/dashboard";
        }

        model.addAttribute("bookings", bookingService.getAllBookings());
        model.addAttribute("totalBookings", bookingService.getAllBookings().size());

        return "admin-dashboard";
    }

    @GetMapping("/admin/delete/{id}")
    public String deleteBooking(@PathVariable Long id, HttpSession session) {
        String username = (String) session.getAttribute("username");

        if (username == null) {
            return "redirect:/login";
        }

        String role = (String) session.getAttribute("role");

        if (!"ADMIN".equalsIgnoreCase(role)) {
            return "redirect:/dashboard";
        }

        bookingService.deleteBooking(id);
        return "redirect:/admin";
    }
}