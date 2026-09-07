package com.gestionbibliotheque.loanservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestionbibliotheque.loanservice.client.BookClient;
import com.gestionbibliotheque.loanservice.dto.BookDto;
import com.gestionbibliotheque.loanservice.dto.LoanRequest;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LoanControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookClient bookClient;

    @Test
    void create_shouldReturn400_whenBookDoesNotExist() throws Exception {
        when(bookClient.getBookById(99L)).thenThrow(notFoundException("/api/books/99"));

        LoanRequest request = new LoanRequest("Bob", 99L);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn409_whenStockExhausted() throws Exception {
        BookDto book = bookDto(3L, "1984", 0);
        when(bookClient.getBookById(3L)).thenReturn(book);

        LoanRequest request = new LoanRequest("Bob", 3L);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void create_shouldReturn409_onConcurrentStockExhaustion() throws Exception {
        BookDto book = bookDto(3L, "1984", 1);
        when(bookClient.getBookById(3L)).thenReturn(book);
        when(bookClient.decrementStock(3L)).thenThrow(conflictException("/api/books/3/decrement-stock"));

        LoanRequest request = new LoanRequest("Bob", 3L);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void create_shouldReturn201_whenStockAvailable() throws Exception {
        BookDto book = bookDto(4L, "Brave New World", 2);
        when(bookClient.getBookById(4L)).thenReturn(book);
        when(bookClient.decrementStock(4L)).thenReturn(book);

        LoanRequest request = new LoanRequest("Alice", 4L);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberName").value("Alice"))
                .andExpect(jsonPath("$.bookTitle").value("Brave New World"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void returnLoan_thenReturnAgain_shouldReturn409() throws Exception {
        BookDto book = bookDto(5L, "Fahrenheit 451", 1);
        when(bookClient.getBookById(5L)).thenReturn(book);
        when(bookClient.decrementStock(5L)).thenReturn(book);

        LoanRequest request = new LoanRequest("Carol", 5L);

        String response = mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long loanId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/loans/{id}/return", loanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));

        mockMvc.perform(patch("/api/loans/{id}/return", loanId))
                .andExpect(status().isConflict());
    }

    @Test
    void create_shouldReturn400_whenMemberNameMissing() throws Exception {
        LoanRequest request = new LoanRequest(null, 3L);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private BookDto bookDto(Long id, String title, int availableCopies) {
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
}
