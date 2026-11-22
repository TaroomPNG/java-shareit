package ru.practicum.shareit.user.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.validation.OnCreate;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void readsUser() throws Exception {
        assertThat(json.parse("{\"id\":1,\"name\":\"Ann\",\"email\":\"ann@yandex.ru\"}"))
                .usingRecursiveComparison()
                .isEqualTo(UserDto.builder().id(1L).name("Ann").email("ann@yandex.ru").build());
    }

    @Test
    void rejectsInvalidEmailOnCreate() {
        UserDto dto = UserDto.builder().name("Ann").email("not-an-email").build();

        assertThat(validator.validate(dto, OnCreate.class)).isNotEmpty();
    }
}
