package com.travel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.travel.entity.User;

public interface UserRepository  extends JpaRepository<User, Long>{
	User findByUsername(String username);
}
