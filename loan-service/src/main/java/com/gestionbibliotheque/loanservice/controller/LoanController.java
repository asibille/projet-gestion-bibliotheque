package com.gestionbibliotheque.loanservice.controller;

import com.gestionbibliotheque.loanservice.dto.LoanRequest;
import com.gestionbibliotheque.loanservice.dto.LoanResponse;
import com.gestionbibliotheque.loanservice.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    public List<LoanResponse> getAll() {
        return loanService.findAll();
    }

    @GetMapping("/{id}")
    public LoanResponse getById(@PathVariable Long id) {
        return loanService.findById(id);
    }

    @PostMapping
    public ResponseEntity<LoanResponse> create(@Valid @RequestBody LoanRequest request) {
        LoanResponse created = loanService.create(request);
        return ResponseEntity.created(URI.create("/api/loans/" + created.getId())).body(created);
    }

    @PatchMapping("/{id}/return")
    public LoanResponse returnLoan(@PathVariable Long id) {
        return loanService.returnLoan(id);
    }
}