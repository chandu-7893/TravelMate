package com.travel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.travel.entity.TourBooking;

public interface TourRepository extends JpaRepository<TourBooking, Long> {

}