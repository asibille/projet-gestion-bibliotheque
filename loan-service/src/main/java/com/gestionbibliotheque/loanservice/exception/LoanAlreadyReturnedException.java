package com.gestionbibliotheque.loanservice.exception;

public class LoanAlreadyReturnedException extends RuntimeException {
    public LoanAlreadyReturnedException(Long loanId) {
        super("L'emprunt " + loanId + " est deja termine");
    }
}