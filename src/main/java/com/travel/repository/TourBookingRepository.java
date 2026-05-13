package com.travel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.travel.entity.TourBooking;

public interface TourBookingRepository extends JpaRepository<TourBooking, Long> {
	List<TourBooking> findByUsername(String username);

}
