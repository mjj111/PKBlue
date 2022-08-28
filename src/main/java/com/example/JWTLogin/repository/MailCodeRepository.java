package com.example.JWTLogin.repository;

import com.example.JWTLogin.domain.MailCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailCodeRepository  extends JpaRepository<MailCode, Long> {
    MailCode findByEmail(String email);
}
