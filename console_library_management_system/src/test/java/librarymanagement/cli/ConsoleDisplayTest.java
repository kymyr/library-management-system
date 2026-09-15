package librarymanagement.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import librarymanagement.repository.BookRepository;
import librarymanagement.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsoleDisplayTest {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private PrintStream originalOutput;
    private ConsoleDisplay display;

    @BeforeEach
    void setUp() {
        originalOutput = System.out;
        System.setOut(new PrintStream(output));
        display = new ConsoleDisplay(new BookRepository(), new MemberRepository());
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOutput);
    }

    // Empty pages print a simple empty result.
    @Test
    void showsEmptyPage() {
        assertTrue(display.displayPaged(List.of(), new Scanner("")));
        assertTrue(output.toString().contains("None"));
    }

    // Entering zero leaves a paged display.
    @Test
    void leavesPagedDisplay() {
        assertFalse(display.displayPaged(List.of("one"), new Scanner("0\n")));
    }

    // Rejections are printed and reported as handled.
    @Test
    void showsRejection() {
        assertTrue(display.showRejection(Optional.of("Book unavailable")));
        assertTrue(output.toString().contains("Book unavailable"));
    }

    // Empty rejection results are not handled.
    @Test
    void ignoresEmptyRejection() {
        assertFalse(display.showRejection(Optional.empty()));
        assertTrue(output.toString().isEmpty());
    }
}
