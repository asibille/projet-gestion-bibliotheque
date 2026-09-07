package com.gestionbibliotheque.bookservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestionbibliotheque.bookservice.dto.BookRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBook_thenDecrementStockUntilEmpty_thenReturns409() throws Exception {
        BookRequest request = new BookRequest();
        request.setTitle("1984");
        request.setAuthor("George Orwell");
        request.setIsbn("978-0451524935");
        request.setTotalCopies(1);

        // Création : availableCopies initialisé à totalCopies = 1
        String response = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.availableCopies").value(1))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        // Premier decrement : passe à 0, OK
        mockMvc.perform(patch("/api/books/{id}/decrement-stock", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(0));

        // Deuxième decrement : stock épuisé, doit renvoyer 409
        mockMvc.perform(patch("/api/books/{id}/decrement-stock", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void findById_shouldReturn404_whenBookDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/books/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBook_shouldReturn400_whenTitleMissing() throws Exception {
        BookRequest request = new BookRequest();
        request.setAuthor("Auteur sans titre");
        request.setIsbn("000");
        request.setTotalCopies(1);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
