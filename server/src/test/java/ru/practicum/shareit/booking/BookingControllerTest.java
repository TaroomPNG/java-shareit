package ru.practicum.shareit.booking;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String USER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Test
    void create() throws Exception {
        when(bookingService.create(eq(2L), any())).thenReturn(booking());

        mockMvc.perform(post("/bookings")
                        .header(USER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":5,\"start\":\"2026-12-01T10:00:00\",\"end\":\"2026-12-02T10:00:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void approve() throws Exception {
        when(bookingService.approve(1L, 7L, true)).thenReturn(booking());

        mockMvc.perform(patch("/bookings/7")
                        .header(USER, 1)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void getAllByBooker() throws Exception {
        when(bookingService.getAllByBooker(2L, BookingState.ALL)).thenReturn(List.of(booking()));

        mockMvc.perform(get("/bookings").header(USER, 2).param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7));
    }

    @Test
    void getAllByOwner() throws Exception {
        when(bookingService.getAllByOwner(1L, BookingState.FUTURE)).thenReturn(List.of(booking()));

        mockMvc.perform(get("/bookings/owner").header(USER, 1).param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7));
    }

    @Test
    void getById() throws Exception {
        when(bookingService.getById(2L, 7L)).thenReturn(booking());

        mockMvc.perform(get("/bookings/7").header(USER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    private BookingDto booking() {
        return BookingDto.builder().id(7L).status(BookingStatus.WAITING).build();
    }
}
