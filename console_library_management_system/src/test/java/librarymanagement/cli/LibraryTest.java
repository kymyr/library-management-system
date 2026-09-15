package librarymanagement.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import librarymanagement.model.Book;
import librarymanagement.model.Member;
import librarymanagement.repository.BookRepository;
import librarymanagement.repository.LoanRepository;
import librarymanagement.repository.MemberRepository;
import librarymanagement.service.CsvService;
import librarymanagement.service.LoanService;
import librarymanagement.service.MemberService;
import librarymanagement.util.ErrorHandling;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LibraryTest {
    // CsvService writes to the real data/ folder, so tests use a stand-in that skips the file writes.
    private static class NoOpCsvService extends CsvService {
        @Override
        public int saveMembers(MemberRepository memberRepository) {
            return 0;
        }

        @Override
        public int saveLoans(LoanRepository loanRepository) {
            return 0;
        }

        @Override
        public int saveInventory(BookRepository bookRepository) {
            return 0;
        }
    }

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private PrintStream originalOutput;

    private BookRepository bookRepo;
    private MemberRepository memberRepo;
    private LoanRepository loanRepo;
    private Library library;

    @BeforeEach
    void setUp() {
        originalOutput = System.out;
        System.setOut(new PrintStream(output));

        bookRepo = new BookRepository();
        memberRepo = new MemberRepository();
        loanRepo = new LoanRepository();
        CsvService csvService = new NoOpCsvService();
        ErrorHandling err = new ErrorHandling();
        ConsoleDisplay display = new ConsoleDisplay(bookRepo, memberRepo);
        ConsoleInput input = new ConsoleInput(bookRepo, memberRepo, display, err);

        library = new Library(new LoanService(bookRepo, loanRepo, csvService),
                new MemberService(memberRepo, csvService), input, display);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOutput);
    }

    // Completing registration adds the member to the repository.
    @Test
    void registersMember() {
        library.registerMember(new Scanner("Jane\nDoe\njane@example.com\ny\n"));

        assertEquals(1, memberRepo.getAllMembers().size());
    }

    // Cancelling at the first prompt registers nobody.
    @Test
    void cancelsRegistration() {
        library.registerMember(new Scanner("0\n"));

        assertTrue(memberRepo.getAllMembers().isEmpty());
    }

    // A confirmed check out creates a loan and reduces availability.
    @Test
    void checksOutBook() {
        memberRepo.registerMemberQuietly(activeMember());
        bookRepo.addBook(new Book(101, "Title", "Author", "9780000000001", 1));

        library.checkOutBook(new Scanner("1\n101\ny\n"));

        assertEquals(0, bookRepo.findById(101).getAvailable());
        assertEquals(1, loanRepo.getBorrowedLoans().size());
    }

    // Checking out a book with no available copies is rejected before confirmation.
    @Test
    void rejectsUnavailableBook() {
        memberRepo.registerMemberQuietly(activeMember());
        bookRepo.addBook(new Book(102, "Title", "Author", "9780000000002", 1));
        bookRepo.issueBook(102);

        library.checkOutBook(new Scanner("1\n102\n"));

        assertTrue(output.toString().contains("Book has no available copies: 102"));
        assertTrue(loanRepo.getBorrowedLoans().isEmpty());
    }

    // A confirmed check in returns the loan and restores availability.
    @Test
    void checksInBook() {
        memberRepo.registerMemberQuietly(activeMember());
        bookRepo.addBook(new Book(103, "Title", "Author", "9780000000003", 1));
        library.checkOutBook(new Scanner("1\n103\ny\n"));

        library.checkInBook(new Scanner("1\n103\ny\n"));

        assertEquals(1, bookRepo.findById(103).getAvailable());
        assertTrue(loanRepo.getBorrowedLoans().isEmpty());
    }

    // Exiting the main menu prints the exit message and returns.
    @Test
    void exitsMainMenu() {
        library.run(new Scanner("0\n"));

        assertTrue(output.toString().contains("Exiting System..."));
    }

    // Invalid main menu input is reported before exiting.
    @Test
    void rejectsInvalidMenuChoice() {
        library.run(new Scanner("5\n0\n"));

        assertTrue(output.toString().contains("Invalid input"));
    }

    private Member activeMember() {
        return new Member(1, "Active Member", "active@example.com",
                "2026-01-01", "2027-01-01", "Active");
    }
}
