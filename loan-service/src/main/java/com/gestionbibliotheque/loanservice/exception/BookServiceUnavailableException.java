package com.gestionbibliotheque.loanservice.exception;

public class BookServiceUnavailableException extends RuntimeException {
    public BookServiceUnavailableException(Throwable cause) {
        super("book-service est indisponible, impossible de traiter l'emprunt", cause);
    }
}