package com.rhythm.hotelbooking.repository;

import com.rhythm.hotelbooking.model.GuestUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestUserRepository extends JpaRepository<GuestUser, Long> {

    GuestUser findByEmail(String email);
}