package com.gestionbibliotheque.bookservice.repository;

import com.gestionbibliotheque.bookservice.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    // Bonus : recherche par auteur/titre
    // List<Book> findByAuthorContainingIgnoreCase(String author);
    // List<Book> findByTitleContainingIgnoreCase(String title);
}
