package com.travel.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.travel.entity.TourBooking;
import com.travel.repository.TourBookingRepository;

@Service
public class TourBookingService {
	private final TourBookingRepository bookingRepository;

	public TourBookingService(TourBookingRepository bookingRepository) {
		this.bookingRepository = bookingRepository;
	}
	
	public TourBooking completePayment(Long id, String paymentMethod) {
	    TourBooking booking = bookingRepository.findById(id).orElse(null);

	    if (booking != null) {
	        booking.setPaymentMethod(paymentMethod);
	        booking.setPaymentStatus("PAID");
	        booking.setTransactionId("TXN" + System.currentTimeMillis());
	        return bookingRepository.save(booking);
	    }

	    return null;
	}

	public TourBooking saveBooking(TourBooking booking) {

		double placePrice = getPlacePrice(booking.getPlace());
		double vehiclePrice = getVehiclePrice(booking.getVehicle());
		double hotelPrice = getHotelPrice(booking.getHotel());

		double total = (placePrice + vehiclePrice + hotelPrice) * booking.getDays();

		booking.setPlacePrice(placePrice);
		booking.setVehiclePrice(vehiclePrice);
		booking.setHotelPrice(hotelPrice);
		booking.setTotalAmount(total);
		booking.setFinalAmount(total - booking.getDiscount());

		return bookingRepository.save(booking);
	}
	public TourBooking applyDiscount(Long id, double discount) {
		TourBooking booking = bookingRepository.findById(id).orElse(null);

		if (booking != null) {
			booking.setDiscount(discount);
			booking.setFinalAmount(booking.getTotalAmount() - discount);
			return bookingRepository.save(booking);
		}

		return null;
	}
	public List<TourBooking> getAllBookings() {
	    return bookingRepository.findAll();
	}

	public void deleteBooking(Long id) {
	    bookingRepository.deleteById(id);
	}	
	public TourBooking getBooking(Long id) {
		return bookingRepository.findById(id).orElse(null);
	}

	public List<TourBooking> getBookingsByUsername(String username) {
		return bookingRepository.findByUsername(username);
	}

	private double getPlacePrice(String place) {
		return switch (place) {
			case "Goa" -> 5000;
			case "Manali" -> 7000;
			case "Hyderabad" -> 3000;
			case "Kerala" -> 6500;
			default -> 4000;
		};
	}

	private double getVehiclePrice(String vehicle) {
		return switch (vehicle) {
			case "Car" -> 2500;
			case "Bus" -> 1200;
			case "Train" -> 1800;
			case "Flight" -> 6000;
			default -> 1000;
		};
	}

	private double getHotelPrice(String hotel) {
		return switch (hotel) {
		case "Normal" -> 1500;
		case "Deluxe" -> 3000;
		case "Premium" -> 5000;
		default -> 1000;
		};
	}
}
