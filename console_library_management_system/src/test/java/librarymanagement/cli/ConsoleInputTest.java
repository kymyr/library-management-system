package librarymanagement.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import librarymanagement.model.Book;
import librarymanagement.model.Member;
import librarymanagement.repository.BookRepository;
import librarymanagement.repository.MemberRepository;
import librarymanagement.util.ErrorHandling;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsoleInputTest {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private PrintStream originalOutput;

    private BookRepository bookRepo;
    private MemberRepository memberRepo;
    private ConsoleInput input;

    @BeforeEach
    void setUp() {
        originalOutput = System.out;
        System.setOut(new PrintStream(output));

        bookRepo = new BookRepository();
        memberRepo = new MemberRepository();
        ConsoleDisplay display = new ConsoleDisplay(bookRepo, memberRepo);
        input = new ConsoleInput(bookRepo, memberRepo, display, new ErrorHandling());
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOutput);
    }

    // Zero cancels the member prompt.
    @Test
    void memberPromptCancels() {
        assertNull(input.member(new Scanner("0\n")));
    }

    // An unknown member ID retries before finding the real one.
    @Test
    void memberPromptRetriesUntilFound() {
        memberRepo.registerMemberQuietly(new Member(1, "Test Member"));

        Member member = input.member(new Scanner("999\n1\n"));

        assertEquals(1, member.getId());
        assertTrue(output.toString().contains("Member ID not found: 999"));
    }

    // Zero cancels the book prompt.
    @Test
    void bookPromptCancels() {
        assertNull(input.book(new Scanner("0\n")));
    }

    // An unknown book ID retries before finding the real one.
    @Test
    void bookPromptRetriesUntilFound() {
        bookRepo.addBook(new Book(101, "Title", "Author", "9780000000001", 1));

        Book book = input.book(new Scanner("999\n101\n"));

        assertEquals(101, book.getId());
        assertTrue(output.toString().contains("Book ID not found: 999"));
    }

    // Cancelling at any registration prompt returns null.
    @Test
    void newMemberDetailsCancels() {
        assertNull(input.newMemberDetails(new Scanner("0\n")));
    }

    // Completed registration details are captured in order.
    @Test
    void newMemberDetailsCollectsAnswers() {
        ConsoleInput.MemberDetails details =
                input.newMemberDetails(new Scanner("Jane\nDoe\njane@example.com\n"));

        assertEquals("Jane", details.firstName());
        assertEquals("Doe", details.lastName());
        assertEquals("jane@example.com", details.email());
    }

    // Searching by title displays the matching book before backing all the way out.
    @Test
    void searchBookFindsByTitle() {
        bookRepo.addBook(new Book(102, "Searchable Book", "Author", "9780000000002", 1));

        Book book = input.searchBook(new Scanner("1\nSearchable\n0\n0\n"));

        assertNull(book);
        assertTrue(output.toString().contains("Searchable Book"));
    }

    // Backing out of the search menu returns no book.
    @Test
    void searchBookCancels() {
        assertNull(input.searchBook(new Scanner("0\n")));
    }

    // A confirmation prompt without a cancel message returns the answer.
    @Test
    void confirmReturnsAnswer() {
        assertTrue(input.confirm("Confirm:", new Scanner("y\n")));
    }

    // A declined confirmation prints its cancel message and returns false.
    @Test
    void confirmPrintsCancelMessage() {
        assertFalse(input.confirm("Confirm:", "Cancelled.", new Scanner("n\n")));
        assertTrue(output.toString().contains("Cancelled."));
    }
}
