package com.gestionbibliotheque.bookservice.exception;

public class StockUnavailableException extends RuntimeException {

    public StockUnavailableException(Long bookId) {
        super("Aucun exemplaire disponible pour le livre id=" + bookId);
    }
}
