package com.travel.controller;

import com.travel.entity.TourBooking;
import com.travel.repository.TourBookingRepository;
import com.travel.repository.TourRepository;
import com.travel.service.PdfService;
import com.travel.service.TourBookingService;
import com.travel.service.UserService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class TravelController {

    private final TourBookingService bookingService;
    private final PdfService pdfService;
    private final TourRepository tourRepository;
    private final TourBookingRepository bookingRepository;

    public TravelController(
            TourBookingService bookingService,
            PdfService pdfService,
            TourRepository tourRepository,
            TourBookingRepository bookingRepository,
            UserService userService) {

        this.bookingService = bookingService;
        this.pdfService = pdfService;
        this.tourRepository = tourRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/destinations")
    public String destinations() {
        return "destinations";
    }

    @PostMapping("/delete/{id}")
    public String deleteBooking(@PathVariable Long id) {

        bookingRepository.deleteById(id);

        return "redirect:/history";
    }

    @GetMapping("/packages")
    public String packagesPage(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String travelDate,
            @RequestParam(required = false) Integer travelers,
            Model model) {

        model.addAttribute("destination", destination);
        model.addAttribute("travelDate", travelDate);
        model.addAttribute("travelers", travelers);

        return "packages";
    }

    @GetMapping("/tour")
    public String tourPage(
            @RequestParam(required = false) String place,
            HttpSession session,
            Model model) {

        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        TourBooking booking = new TourBooking();

        if (place != null) {
            booking.setPlace(place);
        }

        model.addAttribute("booking", booking);

        return "tour";
    }

    @PostMapping("/tour")
    public String bookTour(
            @ModelAttribute TourBooking booking,
            HttpSession session) {

        String username = (String) session.getAttribute("username");

        if (username == null) {
            return "redirect:/login";
        }

        booking.setUsername(username);
        booking.setPaymentStatus("PENDING");

        TourBooking savedBooking = bookingService.saveBooking(booking);

        session.setAttribute("bookingId", savedBooking.getId());

        return "redirect:/game";
    }

    @GetMapping("/game")
    public String gamePage(HttpSession session) {

        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        if (session.getAttribute("bookingId") == null) {
            return "redirect:/tour";
        }

        return "game";
    }

    @PostMapping("/game")
    public String applyDiscount(
            @RequestParam String game,
            @RequestParam String answer,
            HttpSession session,
            Model model) {

        Long bookingId = (Long) session.getAttribute("bookingId");

        if (bookingId == null) {
            return "redirect:/tour";
        }

        double discount = 0;

        // GAME 1 → Guess Destination

        if (game.equals("destination")) {

            if (answer.equalsIgnoreCase("Goa")) {

                discount = 500;

            } else {

                model.addAttribute(
                        "error",
                        "Wrong answer! Correct place not found.");

                return "game";
            }
        }

        // GAME 2 → Lucky Spin

        else if (game.equals("spin")) {

            if (answer.equalsIgnoreCase("LUCKY")) {

                discount = 700;

            } else {

                model.addAttribute(
                        "error",
                        "Spin failed! Try again.");

                return "game";
            }
        }

        // GAME 3 → Travel Quiz

        else if (game.equals("quiz")) {

            if (answer.equalsIgnoreCase("Delhi")) {

                discount = 900;

            } else {

                model.addAttribute(
                        "error",
                        "Wrong quiz answer!");

                return "game";
            }
        }

        // GAME 4 → Treasure Hunt

        else if (game.equals("treasure")) {

            if (answer.equalsIgnoreCase("TREASURE")) {

                discount = 1200;

            } else {

                model.addAttribute(
                        "error",
                        "Treasure not found!");

                return "game";
            }
        }

        // GAME 5 → Fast Click

        else if (game.equals("click")) {

            if (answer.equalsIgnoreCase("FAST")) {

                discount = 600;

            } else {

                model.addAttribute(
                        "error",
                        "Too slow!");

                return "game";
            }
        }

        // GAME 6 → Memory Cards

        else if (game.equals("memory")) {

            if (answer.equalsIgnoreCase("TRAVEL2026")) {

                discount = 1500;

            } else {

                model.addAttribute(
                        "error",
                        "Memory match failed!");

                return "game";
            }
        }

        bookingService.applyDiscount(
                bookingId,
                discount);

        return "redirect:/payment";
    }
    @GetMapping("/bill")
    public String billPage(
            HttpSession session,
            Model model) {

        Long bookingId = (Long) session.getAttribute("bookingId");

        if (bookingId == null) {
            return "redirect:/tour";
        }

        TourBooking booking = bookingService.getBooking(bookingId);

        model.addAttribute("booking", booking);

        return "bill";
    }

    @GetMapping("/bill/download")
    public ResponseEntity<byte[]> downloadBill(HttpSession session) {

        Long bookingId = (Long) session.getAttribute("bookingId");

        if (bookingId == null) {
            return ResponseEntity.badRequest().build();
        }

        TourBooking booking = bookingService.getBooking(bookingId);

        byte[] pdf = pdfService.generateBillPdf(booking);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=TravelMate-Bill.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/history")
    public String history(
            HttpSession session,
            Model model) {

        String username = (String) session.getAttribute("username");

        if (username == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "bookings",
                bookingService.getBookingsByUsername(username));

        return "history";
    }

    @GetMapping("/payment")
    public String paymentPage(
            HttpSession session,
            Model model) {

        Long bookingId = (Long) session.getAttribute("bookingId");

        if (bookingId == null) {
            return "redirect:/tour";
        }

        TourBooking booking = bookingService.getBooking(bookingId);

        if (booking == null) {
            return "redirect:/tour";
        }

        if ("PAID".equals(booking.getPaymentStatus())) {
            return "redirect:/payment-success";
        }

        model.addAttribute("booking", booking);

        return "payment";
    }

    @GetMapping("/payment/{id}")
    public String paymentPageById(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        TourBooking booking = bookingService.getBooking(id);

        if (booking == null) {
            return "redirect:/history";
        }

        session.setAttribute("bookingId", booking.getId());

        model.addAttribute("booking", booking);

        return "payment";
    }

    @PostMapping("/payment")
    public String completePayment(
            @RequestParam String paymentMethod,
            HttpSession session) {

        Long bookingId = (Long) session.getAttribute("bookingId");

        if (bookingId == null) {
            return "redirect:/tour";
        }

        TourBooking booking = bookingService.getBooking(bookingId);

        if (booking == null) {
            return "redirect:/tour";
        }

        if ("PAID".equals(booking.getPaymentStatus())) {
            return "redirect:/payment-success";
        }

        bookingService.completePayment(bookingId, paymentMethod);

        return "redirect:/payment-success";
    }

    @GetMapping("/payment-success")
    public String paymentSuccess(
            HttpSession session,
            Model model) {

        Long bookingId = (Long) session.getAttribute("bookingId");

        if (bookingId == null) {
            return "redirect:/tour";
        }

        TourBooking booking = bookingService.getBooking(bookingId);

        if (booking == null) {
            return "redirect:/tour";
        }

        model.addAttribute("booking", booking);

        return "payment-success";
    }

    @PostMapping("/book")
    public String quickBook(
            @RequestParam String place,
            @RequestParam String hotel,
            @RequestParam String vehicle,
            @RequestParam int days,
            @RequestParam int members,
            HttpSession session) {

        String username = (String) session.getAttribute("username");

        if (username == null) {
            return "redirect:/login";
        }

        TourBooking booking = new TourBooking();

        booking.setUsername(username);
        booking.setPlace(place);
        booking.setHotel(hotel);
        booking.setVehicle(vehicle);
        booking.setDays(days);
        booking.setMembers(members);
        booking.setPaymentStatus("PENDING");

        TourBooking savedBooking = bookingService.saveBooking(booking);

        session.setAttribute("bookingId", savedBooking.getId());

        return "redirect:/payment";
    }
}