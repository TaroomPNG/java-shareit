package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String USER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Test
    void create() throws Exception {
        when(itemService.create(eq(1L), any())).thenReturn(item());

        mockMvc.perform(post("/items")
                        .header(USER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\",\"description\":\"Cordless\",\"available\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void update() throws Exception {
        when(itemService.update(eq(1L), eq(5L), any())).thenReturn(item());

        mockMvc.perform(patch("/items/5")
                        .header(USER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void getById() throws Exception {
        when(itemService.getById(1L, 5L)).thenReturn(item());

        mockMvc.perform(get("/items/5").header(USER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void getAllByOwner() throws Exception {
        when(itemService.getAllByOwner(1L)).thenReturn(List.of(item()));

        mockMvc.perform(get("/items").header(USER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5));
    }

    @Test
    void search() throws Exception {
        when(itemService.search("drill")).thenReturn(List.of(item()));

        mockMvc.perform(get("/items/search").param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void addComment() throws Exception {
        when(itemService.addComment(eq(2L), eq(5L), any()))
                .thenReturn(CommentDto.builder().id(9L).text("Good").authorName("Ann").build());

        mockMvc.perform(post("/items/5/comment")
                        .header(USER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Good\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.text").value("Good"));
    }

    private ItemDto item() {
        return ItemDto.builder()
                .id(5L)
                .name("Drill")
                .description("Cordless")
                .available(true)
                .build();
    }
}
