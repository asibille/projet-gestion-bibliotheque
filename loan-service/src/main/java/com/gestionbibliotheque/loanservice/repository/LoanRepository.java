package com.gestionbibliotheque.loanservice.repository;

import com.gestionbibliotheque.loanservice.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {
}