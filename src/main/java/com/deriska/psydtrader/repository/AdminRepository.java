package com.deriska.psydtrader.repository;

import com.deriska.psydtrader.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<AdminUser, Long> {
    Optional<AdminUser> findByUserName(String userName);
}
