package com.gestionbibliotheque.bookservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    @NotBlank
    private String isbn;

    @NotNull
    @Min(1)
    private Integer totalCopies;

    @NotNull
    @Min(0)
    private Integer availableCopies;

    protected Book() {
        // requis par JPA
    }

    public Book(String title, String author, String isbn, Integer totalCopies, Integer availableCopies) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }

    public Integer getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;
    }

    /**
     * Décrémente le stock disponible de 1.
     * Lève IllegalStateException si aucun exemplaire n'est disponible
     * (défense en profondeur, revérifiée côté service avant appel).
     */
    public void decrementAvailableCopies() {
        if (this.availableCopies <= 0) {
            throw new IllegalStateException("Aucun exemplaire disponible pour ce livre");
        }
        this.availableCopies--;
    }

    /**
     * Réincrémente le stock disponible de 1, sans jamais dépasser totalCopies.
     */
    public void incrementAvailableCopies() {
        if (this.availableCopies < this.totalCopies) {
            this.availableCopies++;
        }
    }
}
