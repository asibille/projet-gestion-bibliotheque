package com.gestionbibliotheque.bookservice.service;

import com.gestionbibliotheque.bookservice.dto.BookRequest;
import com.gestionbibliotheque.bookservice.dto.BookResponse;
import com.gestionbibliotheque.bookservice.exception.BookNotFoundException;
import com.gestionbibliotheque.bookservice.exception.StockUnavailableException;
import com.gestionbibliotheque.bookservice.model.Book;
import com.gestionbibliotheque.bookservice.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookResponse> findAll() {
        return bookRepository.findAll().stream()
                .map(BookResponse::fromEntity)
                .toList();
    }

    public BookResponse findById(Long id) {
        return BookResponse.fromEntity(getBookOrThrow(id));
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        // availableCopies initialisé à totalCopies à la création
        Book book = new Book(
                request.getTitle(),
                request.getAuthor(),
                request.getIsbn(),
                request.getTotalCopies(),
                request.getTotalCopies()
        );
        Book saved = bookRepository.save(book);
        return BookResponse.fromEntity(saved);
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = getBookOrThrow(id);
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setTotalCopies(request.getTotalCopies());
        // On ne dépasse jamais totalCopies après une mise à jour
        if (book.getAvailableCopies() > book.getTotalCopies()) {
            book.setAvailableCopies(book.getTotalCopies());
        }
        return BookResponse.fromEntity(book);
    }

    @Transactional
    public void delete(Long id) {
        Book book = getBookOrThrow(id);
        bookRepository.delete(book);
    }

    /**
     * Décrémente le stock disponible d'un livre.
     * Revérifie côté serveur que availableCopies > 0 avant de décrémenter,
     * même si loan-service a déjà vérifié en amont (défense en profondeur / TOCTOU).
     */
    @Transactional
    public BookResponse decrementStock(Long id) {
        Book book = getBookOrThrow(id);
        if (book.getAvailableCopies() <= 0) {
            throw new StockUnavailableException(id);
        }
        book.decrementAvailableCopies();
        return BookResponse.fromEntity(book);
    }

    /**
     * Réincrémente le stock disponible d'un livre, sans jamais dépasser totalCopies.
     */
    @Transactional
    public BookResponse incrementStock(Long id) {
        Book book = getBookOrThrow(id);
        book.incrementAvailableCopies();
        return BookResponse.fromEntity(book);
    }

    private Book getBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }
}
