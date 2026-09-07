package com.gestionbibliotheque.loanservice.exception;

public class BookNotFoundForLoanException extends RuntimeException {
    public BookNotFoundForLoanException(Long bookId) {
        super("Le livre " + bookId + " demande pour l'emprunt est introuvable");
    }
}