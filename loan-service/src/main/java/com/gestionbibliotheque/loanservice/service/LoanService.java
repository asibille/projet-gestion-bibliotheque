package com.gestionbibliotheque.loanservice.service;

import com.gestionbibliotheque.loanservice.client.BookClient;
import com.gestionbibliotheque.loanservice.dto.BookDto;
import com.gestionbibliotheque.loanservice.dto.LoanRequest;
import com.gestionbibliotheque.loanservice.dto.LoanResponse;
import com.gestionbibliotheque.loanservice.exception.*;
import com.gestionbibliotheque.loanservice.model.Loan;
import com.gestionbibliotheque.loanservice.model.LoanStatus;
import com.gestionbibliotheque.loanservice.repository.LoanRepository;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoanService {

    private static final int LOAN_DURATION_DAYS = 14;

    private final LoanRepository loanRepository;
    private final BookClient bookClient;

    public LoanService(LoanRepository loanRepository, BookClient bookClient) {
        this.loanRepository = loanRepository;
        this.bookClient = bookClient;
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> findAll() {
        return loanRepository.findAll().stream().map(LoanMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public LoanResponse findById(Long id) {
        return LoanMapper.toResponse(getLoanOrThrow(id));
    }

    @Transactional
    public LoanResponse create(LoanRequest request) {
        // Etape 1 : verifier que le livre existe et a un exemplaire disponible
        BookDto book = fetchBook(request.getBookId());

        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            // NE PAS appeler decrement-stock dans ce cas (regle explicite du cahier des charges)
            throw new NoAvailableCopyException(request.getBookId());
        }

        // Etape 2 : decrementer le stock chez book-service.
        // Meme si on vient de verifier availableCopies > 0, book-service revérifie
        // lui-meme avant de decrementer (TOCTOU) : un autre emprunt a pu consommer
        // le dernier exemplaire entre l'etape 1 et l'etape 2.
        try {
            bookClient.decrementStock(request.getBookId());
        } catch (FeignException.Conflict ex) {
            throw new NoAvailableCopyException(request.getBookId());
        } catch (FeignException.NotFound ex) {
            throw new BookNotFoundForLoanException(request.getBookId());
        } catch (FeignException ex) {
            throw new BookServiceUnavailableException(ex);
        }

        // Etape 3 : creer l'emprunt (snapshot du titre, comme OrderItem au module 7)
        LocalDate today = LocalDate.now();
        Loan loan = new Loan(request.getMemberName(), book.getId(), book.getTitle(),
                today, today.plusDays(LOAN_DURATION_DAYS), LoanStatus.ACTIVE);

        return LoanMapper.toResponse(loanRepository.save(loan));
    }

    @Transactional
    public LoanResponse returnLoan(Long id) {
        Loan loan = getLoanOrThrow(id);

        try {
            loan.markAsReturned(LocalDate.now());
        } catch (IllegalStateException ex) {
            throw new LoanAlreadyReturnedException(id);
        }

        try {
            bookClient.incrementStock(loan.getBookId());
        } catch (FeignException.NotFound ex) {
            throw new BookNotFoundForLoanException(loan.getBookId());
        } catch (FeignException ex) {
            throw new BookServiceUnavailableException(ex);
        }

        return LoanMapper.toResponse(loanRepository.save(loan));
    }

    private BookDto fetchBook(Long bookId) {
        try {
            return bookClient.getBookById(bookId);
        } catch (FeignException.NotFound ex) {
            throw new BookNotFoundForLoanException(bookId);
        } catch (FeignException ex) {
            throw new BookServiceUnavailableException(ex);
        }
    }

    private Loan getLoanOrThrow(Long id) {
        return loanRepository.findById(id).orElseThrow(() -> new LoanNotFoundException(id));
    }
}