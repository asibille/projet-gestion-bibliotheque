package com.gestionbibliotheque.bookservice;

import com.gestionbibliotheque.bookservice.dto.BookResponse;
import com.gestionbibliotheque.bookservice.exception.BookNotFoundException;
import com.gestionbibliotheque.bookservice.exception.StockUnavailableException;
import com.gestionbibliotheque.bookservice.model.Book;
import com.gestionbibliotheque.bookservice.repository.BookRepository;
import com.gestionbibliotheque.bookservice.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book book;

    @BeforeEach
    void setUp() throws Exception {
        book = new Book("Clean Code", "Robert C. Martin", "978-0132350884", 3, 0);
        setId(book, 1L);
    }

    @Test
    void decrementStock_shouldThrow409_whenNoAvailableCopies() {
        // book a availableCopies = 0
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        StockUnavailableException exception = assertThrows(
                StockUnavailableException.class,
                () -> bookService.decrementStock(1L)
        );

        assertTrue(exception.getMessage().contains("1"));
        verify(bookRepository, never()).save(any());
    }

    @Test
    void decrementStock_shouldSucceed_whenCopiesAvailable() {
        book.setAvailableCopies(2);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookResponse response = bookService.decrementStock(1L);

        assertEquals(1, response.getAvailableCopies());
    }

    @Test
    void decrementStock_shouldThrow404_whenBookDoesNotExist() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.decrementStock(99L));
    }

    @Test
    void incrementStock_shouldNeverExceedTotalCopies() {
        book.setTotalCopies(3);
        book.setAvailableCopies(3); // déjà au max
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookResponse response = bookService.incrementStock(1L);

        assertEquals(3, response.getAvailableCopies());
    }

    private static void setId(Book book, Long id) throws Exception {
        Field idField = Book.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(book, id);
    }
}
