package com.iscode.coaching_fee_portal.repository;

import com.iscode.coaching_fee_portal.entity.Centre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CentreRepository extends JpaRepository<Centre, Long> {

    Optional<Centre> findByCode(String code);
}
