package com.gestionbibliotheque.loanservice.service;

import com.gestionbibliotheque.loanservice.dto.LoanResponse;
import com.gestionbibliotheque.loanservice.model.Loan;

public final class LoanMapper {

    private LoanMapper() {}

    public static LoanResponse toResponse(Loan loan) {
        return new LoanResponse(loan.getId(), loan.getMemberName(), loan.getBookId(), loan.getBookTitle(),
                loan.getLoanDate(), loan.getDueDate(), loan.getReturnDate(), loan.getStatus());
    }
}