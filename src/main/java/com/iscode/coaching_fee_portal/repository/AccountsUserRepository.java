package com.iscode.coaching_fee_portal.repository;

import com.iscode.coaching_fee_portal.entity.AccountsUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountsUserRepository extends JpaRepository<AccountsUser, Long> {

    Optional<AccountsUser> findByEmail(String email);

}