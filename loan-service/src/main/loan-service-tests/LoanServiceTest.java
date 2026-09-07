package com.gestionbibliotheque.loanservice;

import com.gestionbibliotheque.loanservice.client.BookClient;
import com.gestionbibliotheque.loanservice.dto.BookDto;
import com.gestionbibliotheque.loanservice.dto.LoanRequest;
import com.gestionbibliotheque.loanservice.dto.LoanResponse;
import com.gestionbibliotheque.loanservice.exception.BookNotFoundForLoanException;
import com.gestionbibliotheque.loanservice.exception.BookServiceUnavailableException;
import com.gestionbibliotheque.loanservice.exception.LoanAlreadyReturnedException;
import com.gestionbibliotheque.loanservice.exception.NoAvailableCopyException;
import com.gestionbibliotheque.loanservice.model.Loan;
import com.gestionbibliotheque.loanservice.model.LoanStatus;
import com.gestionbibliotheque.loanservice.repository.LoanRepository;
import com.gestionbibliotheque.loanservice.service.LoanService;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookClient bookClient;

    @InjectMocks
    private LoanService loanService;

    private LoanRequest request;

    @BeforeEach
    void setUp() {
        request = new LoanRequest("Bob", 3L);
    }

    @Test
    void create_shouldThrowBookNotFound_whenBookDoesNotExist() {
        when(bookClient.getBookById(3L)).thenThrow(notFoundException("/api/books/3"));

        assertThrows(BookNotFoundForLoanException.class, () -> loanService.create(request));
        verify(bookClient, never()).decrementStock(anyLong());
        verify(loanRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowNoAvailableCopy_whenStockAlreadyZeroAtCheck() {
        BookDto book = bookWithStock(3L, "1984", 0);
        when(bookClient.getBookById(3L)).thenReturn(book);

        assertThrows(NoAvailableCopyException.class, () -> loanService.create(request));

        // Regle explicite du cahier des charges : ne pas appeler decrement-stock si deja a 0
        verify(bookClient, never()).decrementStock(anyLong());
        verify(loanRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowNoAvailableCopy_onConcurrentStockExhaustion_TOCTOU() {
        // Etape 1 : le stock semble disponible
        BookDto book = bookWithStock(3L, "1984", 1);
        when(bookClient.getBookById(3L)).thenReturn(book);
        // Etape 2 : book-service refuse (409) car un autre emprunt a consomme le dernier exemplaire entre-temps
        when(bookClient.decrementStock(3L)).thenThrow(conflictException("/api/books/3/decrement-stock"));

        assertThrows(NoAvailableCopyException.class, () -> loanService.create(request));
        verify(loanRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowBookNotFound_whenDecrementStockReturns404() {
        BookDto book = bookWithStock(3L, "1984", 1);
        when(bookClient.getBookById(3L)).thenReturn(book);
        when(bookClient.decrementStock(3L)).thenThrow(notFoundException("/api/books/3/decrement-stock"));

        assertThrows(BookNotFoundForLoanException.class, () -> loanService.create(request));
    }

    @Test
    void create_shouldThrowBookServiceUnavailable_onOtherFeignError() {
        BookDto book = bookWithStock(3L, "1984", 1);
        when(bookClient.getBookById(3L)).thenReturn(book);
        when(bookClient.decrementStock(3L)).thenThrow(internalServerErrorException("/api/books/3/decrement-stock"));

        assertThrows(BookServiceUnavailableException.class, () -> loanService.create(request));
    }

    @Test
    void create_shouldSucceed_whenStockAvailable() {
        BookDto book = bookWithStock(3L, "1984", 2);
        when(bookClient.getBookById(3L)).thenReturn(book);
        when(bookClient.decrementStock(3L)).thenReturn(book);
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan loan = invocation.getArgument(0);
            setId(loan, 100L);
            return loan;
        });

        LoanResponse response = loanService.create(request);

        assertEquals("Bob", response.getMemberName());
        assertEquals("1984", response.getBookTitle());
        assertEquals(LoanStatus.ACTIVE, response.getStatus());
        assertEquals(response.getLoanDate().plusDays(14), response.getDueDate());
    }

    @Test
    void returnLoan_shouldThrowLoanAlreadyReturned_whenAlreadyReturned() throws Exception {
        Loan loan = activeLoan();
        loan.markAsReturned(LocalDate.now());
        setId(loan, 10L);
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));

        assertThrows(LoanAlreadyReturnedException.class, () -> loanService.returnLoan(10L));
        verify(bookClient, never()).incrementStock(anyLong());
    }

    @Test
    void returnLoan_shouldIncrementStockAndMarkReturned() throws Exception {
        Loan loan = activeLoan();
        setId(loan, 10L);
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanResponse response = loanService.returnLoan(10L);

        verify(bookClient, times(1)).incrementStock(5L);
        assertEquals(LoanStatus.RETURNED, response.getStatus());
        assertNotNull(response.getReturnDate());
    }

    private Loan activeLoan() {
        LocalDate today = LocalDate.now();
        return new Loan("Alice", 5L, "Some Book", today, today.plusDays(14), LoanStatus.ACTIVE);
    }

    private BookDto bookWithStock(Long id, String title, int availableCopies) {
        BookDto book = new BookDto();
        book.setId(id);
        book.setTitle(title);
        book.setAvailableCopies(availableCopies);
        return book;
    }

    private FeignException.NotFound notFoundException(String path) {
        Request req = Request.create(Request.HttpMethod.GET, path,
                Collections.emptyMap(), null, StandardCharsets.UTF_8, new RequestTemplate());
        return new FeignException.NotFound("not found", req, null, null);
    }

    private FeignException.Conflict conflictException(String path) {
        Request req = Request.create(Request.HttpMethod.PATCH, path,
                Collections.emptyMap(), null, StandardCharsets.UTF_8, new RequestTemplate());
        return new FeignException.Conflict("conflict", req, null, null);
    }

    private FeignException.InternalServerError internalServerErrorException(String path) {
        Request req = Request.create(Request.HttpMethod.PATCH, path,
                Collections.emptyMap(), null, StandardCharsets.UTF_8, new RequestTemplate());
        return new FeignException.InternalServerError("boom", req, null, null);
    }

    private static void setId(Loan loan, Long id) throws Exception {
        Field idField = Loan.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(loan, id);
    }
}
