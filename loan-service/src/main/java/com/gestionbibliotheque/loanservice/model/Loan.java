package com.gestionbibliotheque.loanservice.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String memberName;

    @Column(nullable = false)
    private Long bookId;

    @Column(nullable = false)
    private String bookTitle;

    @Column(nullable = false)
    private LocalDate loanDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    public Loan() {}

    public Loan(String memberName, Long bookId, String bookTitle, LocalDate loanDate,
                LocalDate dueDate, LoanStatus status) {
        this.memberName = memberName;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public LocalDate getLoanDate() { return loanDate; }
    public void setLoanDate(LocalDate loanDate) { this.loanDate = loanDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }

    /**
     * Marque l'emprunt comme rendu et enregistre la date de retour.
     * Leve IllegalStateException si l'emprunt est deja termine
     * (defense en profondeur, revérifiee cote service avant appel).
     */
    public void markAsReturned(LocalDate returnDate) {
        if (this.status == LoanStatus.RETURNED) {
            throw new IllegalStateException("Cet emprunt est deja termine");
        }
        this.status = LoanStatus.RETURNED;
        this.returnDate = returnDate;
    }
}