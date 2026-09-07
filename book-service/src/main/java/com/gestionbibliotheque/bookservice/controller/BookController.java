package com.gestionbibliotheque.bookservice.controller;

import com.gestionbibliotheque.bookservice.dto.BookRequest;
import com.gestionbibliotheque.bookservice.dto.BookResponse;
import com.gestionbibliotheque.bookservice.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> findAll() {
        return ResponseEntity.ok(bookService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.findById(id));
    }

    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        BookResponse created = bookService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return ResponseEntity.ok(bookService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Appelé par loan-service lors de la création d'un emprunt.
     * Revérifie le stock côté serveur (409 si épuisé) - ne fait pas confiance
     * à la vérification déjà faite par l'appelant.
     */
    @PatchMapping("/{id}/decrement-stock")
    public ResponseEntity<BookResponse> decrementStock(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.decrementStock(id));
    }

    /**
     * Appelé par loan-service lors du retour d'un emprunt.
     */
    @PatchMapping("/{id}/increment-stock")
    public ResponseEntity<BookResponse> incrementStock(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.incrementStock(id));
    }
}
