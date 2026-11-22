package ru.practicum.shareit.booking.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

@JsonTest
class BookingCreateDtoJsonTest {

    @Autowired
    private JacksonTester<BookingCreateDto> json;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void writesAndReadsDatesAsIsoStrings() throws Exception {
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(1L)
                .start(LocalDateTime.of(2026, 11, 22, 10, 15, 0))
                .end(LocalDateTime.of(2026, 11, 23, 10, 15, 0))
                .build();

        assertThat(json.write(dto)).extractingJsonPathStringValue("$.start")
                .isEqualTo("2026-11-22T10:15:00");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.end")
                .isEqualTo("2026-11-23T10:15:00");
        assertThat(json.parse("""
                {"itemId":1,"start":"2026-11-22T10:15:00","end":"2026-11-23T10:15:00"}
                """)).usingRecursiveComparison().isEqualTo(dto);
    }

    @Test
    void rejectsMissingDates() {
        BookingCreateDto dto = BookingCreateDto.builder().itemId(1L).build();

        assertThat(validator.validate(dto)).isNotEmpty();
    }
}
