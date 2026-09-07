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

    @Transactional(readOnly = true)
    public List<BookResponse> findAll() {
        return bookRepository.findAll().stream()
                .map(BookResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        return BookResponse.fromEntity(getBookOrThrow(id));
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        // availableCopies initialise a totalCopies a la creation
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
        // On ne depasse jamais totalCopies apres une mise a jour
        if (book.getAvailableCopies() > book.getTotalCopies()) {
            book.setAvailableCopies(book.getTotalCopies());
        }
        Book saved = bookRepository.save(book);
        return BookResponse.fromEntity(saved);
    }

    @Transactional
    public void delete(Long id) {
        Book book = getBookOrThrow(id);
        bookRepository.delete(book);
    }

    /**
     * Decremente le stock disponible d'un livre.
     * La regle "jamais en dessous de 0" est portee par l'entite elle-meme
     * (Book.decrementAvailableCopies()). Le service se contente de traduire
     * l'exception technique en exception metier HTTP.
     * Revérifiee cote serveur meme si loan-service a deja verifie en amont
     * (defense en profondeur / TOCTOU).
     */
    @Transactional
    public BookResponse decrementStock(Long id) {
        Book book = getBookOrThrow(id);
        try {
            book.decrementAvailableCopies();
        } catch (IllegalStateException ex) {
            throw new StockUnavailableException(id);
        }
        Book saved = bookRepository.save(book);
        return BookResponse.fromEntity(saved);
    }

    /**
     * Reincremente le stock disponible d'un livre, sans jamais depasser totalCopies
     * (regle portee par l'entite : Book.incrementAvailableCopies()).
     */
    @Transactional
    public BookResponse incrementStock(Long id) {
        Book book = getBookOrThrow(id);
        book.incrementAvailableCopies();
        Book saved = bookRepository.save(book);
        return BookResponse.fromEntity(saved);
    }

    private Book getBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }
}