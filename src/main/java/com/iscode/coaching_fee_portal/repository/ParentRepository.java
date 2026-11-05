package com.iscode.coaching_fee_portal.repository;

import com.iscode.coaching_fee_portal.entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {
    Optional<Parent> findByMobileNo(String mobileNo);
}
