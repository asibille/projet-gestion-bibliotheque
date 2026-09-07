package com.gestionbibliotheque.bookservice.dto;

import com.gestionbibliotheque.bookservice.model.Book;

public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private Integer totalCopies;
    private Integer availableCopies;

    public BookResponse() {
    }

    public static BookResponse fromEntity(Book book) {
        BookResponse response = new BookResponse();
        response.id = book.getId();
        response.title = book.getTitle();
        response.author = book.getAuthor();
        response.isbn = book.getIsbn();
        response.totalCopies = book.getTotalCopies();
        response.availableCopies = book.getAvailableCopies();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public Integer getTotalCopies() {
        return totalCopies;
    }

    public Integer getAvailableCopies() {
        return availableCopies;
    }
}
