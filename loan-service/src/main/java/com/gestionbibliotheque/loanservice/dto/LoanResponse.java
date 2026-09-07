package com.gestionbibliotheque.loanservice.dto;

import com.gestionbibliotheque.loanservice.model.LoanStatus;
import java.time.LocalDate;

public class LoanResponse {
    private Long id;
    private String memberName;
    private Long bookId;
    private String bookTitle;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private LoanStatus status;

    public LoanResponse() {}

    public LoanResponse(Long id, String memberName, Long bookId, String bookTitle,
                        LocalDate loanDate, LocalDate dueDate, LocalDate returnDate, LoanStatus status) {
        this.id = id;
        this.memberName = memberName;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getMemberName() { return memberName; }
    public Long getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public LocalDate getLoanDate() { return loanDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public LoanStatus getStatus() { return status; }
}