package com.gestionbibliotheque.loanservice.exception;

public class NoAvailableCopyException extends RuntimeException {
    public NoAvailableCopyException(Long bookId) {
        super("Aucun exemplaire disponible pour le livre " + bookId);
    }
}