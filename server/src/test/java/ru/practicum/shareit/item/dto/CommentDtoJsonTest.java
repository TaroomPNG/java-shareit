package ru.practicum.shareit.item.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void writesCreatedAsIsoString() throws Exception {
        CommentDto dto = CommentDto.builder()
                .id(1L)
                .text("Good")
                .authorName("Ann")
                .created(LocalDateTime.of(2026, 11, 22, 10, 15, 0))
                .build();

        assertThat(json.write(dto)).extractingJsonPathStringValue("$.created")
                .isEqualTo("2026-11-22T10:15:00");
    }

    @Test
    void rejectsBlankText() {
        CommentDto dto = CommentDto.builder().text("  ").build();

        assertThat(validator.validate(dto)).isNotEmpty();
    }
}
