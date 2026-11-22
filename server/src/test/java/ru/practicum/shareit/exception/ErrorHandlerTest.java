package ru.practicum.shareit.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;

class ErrorHandlerTest {

    private final ErrorHandler handler = new ErrorHandler();

    @Test
    void handlesValidation() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "createDto");
        bindingResult.addError(new FieldError("createDto", "itemId", "must not be null"));
        bindingResult.addError(new FieldError("createDto", "start", "must not be null"));
        MethodParameter parameter = new MethodParameter(
                BookingController.class.getDeclaredMethod("create", Long.class, BookingCreateDto.class), 1);

        ErrorResponse response = handler.handleValidation(
                new MethodArgumentNotValidException(parameter, bindingResult));

        assertThat(response).isEqualTo(new ErrorResponse(
                "Validation error", "itemId: must not be null; start: must not be null"));
    }

    @Test
    void handlesBadRequest() {
        assertThat(handler.handleBadRequest(new BadRequestException("bad")))
                .isEqualTo(new ErrorResponse("Bad request", "bad"));
    }

    @Test
    void handlesNotFound() {
        assertThat(handler.handleNotFound(new NotFoundException("missing")))
                .isEqualTo(new ErrorResponse("Not found", "missing"));
    }

    @Test
    void handlesForbidden() {
        assertThat(handler.handleForbidden(new ForbiddenException("no")))
                .isEqualTo(new ErrorResponse("Forbidden", "no"));
    }

    @Test
    void handlesConflict() {
        assertThat(handler.handleConflict(new ConflictException("dup")))
                .isEqualTo(new ErrorResponse("Conflict", "dup"));
    }

    @Test
    void handlesUnexpectedError() {
        assertThat(handler.handleThrowable(new IllegalStateException("boom")))
                .isEqualTo(new ErrorResponse("Internal server error", "boom"));
    }
}
