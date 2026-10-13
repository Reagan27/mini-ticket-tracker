package com.lodwar.tickets;

import com.lodwar.tickets.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TicketApiIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired TicketRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private ResultActions create(String json) throws Exception {
        return mvc.perform(post("/tickets").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String createAndGetId(String title) throws Exception {
        String body = create("{\"title\":\"" + title + "\"}").andReturn().getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.read(body, "$.id");
    }

    @Test
    void createTicket_returns201_withDefaults() throws Exception {
        create("{\"title\":\"Printer on fire\",\"description\":\"3rd floor\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Printer on fire"))
                .andExpect(jsonPath("$.status").value("open"))
                .andExpect(jsonPath("$.priority").value("medium"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void createTicket_withoutTitle_returns400() throws Exception {
        create("{\"description\":\"no title\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void createTicket_withInvalidPriority_returns400() throws Exception {
        create("{\"title\":\"x\",\"priority\":\"urgent\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void listTickets_supportsStatusFilterAndPagination() throws Exception {
        String id = createAndGetId("A");
        createAndGetId("B");
        createAndGetId("C");
        mvc.perform(patch("/tickets/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"closed\"}")).andExpect(status().isOk());

        mvc.perform(get("/tickets").param("status", "open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.total").value(2));

        mvc.perform(get("/tickets").param("page", "2").param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void listTickets_withInvalidStatusOrLimit_returns400() throws Exception {
        mvc.perform(get("/tickets").param("status", "bogus")).andExpect(status().isBadRequest());
        mvc.perform(get("/tickets").param("limit", "0")).andExpect(status().isBadRequest());
    }

    @Test
    void patchTicket_updatesStatusAndPriority() throws Exception {
        String id = createAndGetId("Fix login");
        mvc.perform(patch("/tickets/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"in_progress\",\"priority\":\"high\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("in_progress"))
                .andExpect(jsonPath("$.priority").value("high"));
    }

    @Test
    void patchTicket_unknownId_returns404_andEmptyBody_returns400() throws Exception {
        mvc.perform(patch("/tickets/" + UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"closed\"}")).andExpect(status().isNotFound());

        String id = createAndGetId("x");
        mvc.perform(patch("/tickets/" + id).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stats_groupsByStatus_includingZeroCounts() throws Exception {
        String a = createAndGetId("A");
        createAndGetId("B");
        createAndGetId("C");
        mvc.perform(patch("/tickets/" + a).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"in_progress\"}")).andExpect(status().isOk());

        mvc.perform(get("/tickets/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.open").value(2))
                .andExpect(jsonPath("$.in_progress").value(1))
                .andExpect(jsonPath("$.closed").value(0));
    }
}
