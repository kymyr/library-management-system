package librarymanagement.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Scanner;

import org.junit.jupiter.api.Test;

class ErrorHandlingTest {
    private final ErrorHandling errorHandling = new ErrorHandling();

    // Invalid menu values are retried before returning a valid choice.
    @Test
    void validatesMenuChoice() {
        assertEquals(2, errorHandling.validateMenuChoice(-1, "Menu:",
                new Scanner("bad\n10\n2\n")));
    }

    // Invalid text is retried before returning a valid value.
    @Test
    void validatesText() {
        assertEquals("Jane Doe", errorHandling.validateStringInput("", "Name:",
                new Scanner("123\n\nJane Doe\n")));
    }

    // Invalid ISBN input is retried.
    @Test
    void validatesIsbn() {
        assertEquals("9780000000001", errorHandling.validateIsbn("ISBN:",
                new Scanner("isbn\n9780000000001\n")));
    }

    // Invalid IDs are retried and zero remains a valid cancellation.
    @Test
    void validatesIds() {
        assertEquals(123, errorHandling.validateLookupId("ID:",
                new Scanner("-1\nabc\n123\n")));
        assertEquals(ErrorHandling.CANCEL, errorHandling.validateCancellableId("ID:",
                new Scanner("0\n")));
    }

    // Invalid email input is retried.
    @Test
    void validatesEmail() {
        assertEquals("jane@example.com", errorHandling.validateEmail("Email:",
                new Scanner("jane.example.com\njane@example.com\n")));
    }

    // Cancellable names and emails return null when the user enters zero.
    @Test
    void handlesCancellableDetails() {
        assertNull(errorHandling.validateCancellableString("Name:", new Scanner("0\n")));
        assertNull(errorHandling.validateCancellableEmail("Email:", new Scanner("0\n")));
    }

    // Invalid confirmation input is retried before returning the answer.
    @Test
    void validatesConfirmation() {
        assertTrue(errorHandling.validateConfirmation("Confirm:", new Scanner("maybe\nY\n")));
        assertFalse(errorHandling.validateConfirmation("Confirm:", new Scanner("maybe\nN\n")));
    }
}
