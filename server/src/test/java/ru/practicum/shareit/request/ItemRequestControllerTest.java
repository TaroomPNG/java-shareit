package ru.practicum.shareit.request;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String USER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void create() throws Exception {
        when(itemRequestService.create(eq(1L), any())).thenReturn(request());

        mockMvc.perform(post("/requests")
                        .header(USER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Need a drill\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.description").value("Need a drill"));
    }

    @Test
    void getOwn() throws Exception {
        when(itemRequestService.getOwn(1L)).thenReturn(List.of(request()));

        mockMvc.perform(get("/requests").header(USER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    void getOthers() throws Exception {
        when(itemRequestService.getOthers(2L, 0, 10)).thenReturn(List.of(request()));

        mockMvc.perform(get("/requests/all").header(USER, 2).param("from", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Need a drill"));
    }

    @Test
    void getById() throws Exception {
        when(itemRequestService.getById(2L, 3L)).thenReturn(request());

        mockMvc.perform(get("/requests/3").header(USER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    private ItemRequestDto request() {
        return ItemRequestDto.builder()
                .id(3L)
                .description("Need a drill")
                .created(LocalDateTime.of(2026, 11, 22, 10, 0))
                .build();
    }
}
