package librarymanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import librarymanagement.model.Book;
import librarymanagement.model.Loan;
import librarymanagement.model.LoanStatus;
import librarymanagement.model.Member;
import librarymanagement.repository.BookRepository;
import librarymanagement.repository.LoanRepository;
import librarymanagement.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvServiceTest {
    @TempDir
    Path temporaryDirectory;

    // Book import loads matching inventory and skips incomplete rows.
    @Test
    void loadsBooks() throws IOException {
        Path cataloguePath = temporaryDirectory.resolve("books_catalogue.csv");
        Path inventoryPath = temporaryDirectory.resolve("books_inventory.csv");
        Files.write(cataloguePath, List.of(
                "bookId,title,author,isbn",
                "101,First Book,First Author,9780000000001",
                "102,Missing Inventory,Author,9780000000002",
                "invalid,Bad Book,Author,9780000000003"));
        Files.write(inventoryPath, List.of(
                "bookId,total_copies,available_copies",
                "101,3,2"));

        BookRepository bookRepository = new BookRepository();
        int loaded = new CsvService().loadBooks(cataloguePath, inventoryPath, bookRepository);

        assertEquals(1, loaded);
        assertEquals(2, bookRepository.findById(101).getAvailable());
    }

    // Inventory written to a temporary file can be loaded again.
    @Test
    void savesInventory() throws IOException {
        BookRepository original = new BookRepository();
        Book book = new Book(101, "First Book", "First Author", "9780000000001", 3);
        book.setAvailable(1);
        original.importBook(book);
        Path inventoryPath = temporaryDirectory.resolve("books_inventory.csv");

        CsvService csvService = new CsvService();
        assertEquals(1, csvService.saveInventory(inventoryPath, original));
        assertEquals("bookId,total_copies,available_copies", Files.readAllLines(inventoryPath).get(0));

        Path cataloguePath = temporaryDirectory.resolve("books_catalogue.csv");
        Files.write(cataloguePath, List.of(
                "bookId,title,author,isbn",
                "101,First Book,First Author,9780000000001"));
        BookRepository reloaded = new BookRepository();
        csvService.loadBooks(cataloguePath, inventoryPath, reloaded);

        assertEquals(1, reloaded.findById(101).getAvailable());
    }

    // Member details written to a temporary file can be loaded again.
    @Test
    void savesMembers() throws IOException {
        MemberRepository original = new MemberRepository();
        original.registerMemberQuietly(new Member(101, "Jane Doe", "jane@example.com",
                "2026-01-01", "2027-01-01", "Active"));
        Path membersPath = temporaryDirectory.resolve("library_members.csv");

        CsvService csvService = new CsvService();
        assertEquals(1, csvService.saveMembers(membersPath, original));
        MemberRepository reloaded = new MemberRepository();
        assertEquals(1, csvService.loadMembers(membersPath, reloaded));

        assertEquals("Jane Doe", reloaded.findById(101).getName());
    }

    // Loan history written to a temporary file can be loaded again.
    @Test
    void savesLoans() throws IOException {
        LoanRepository original = new LoanRepository();
        original.addLoan(new Loan(1, 101, 5, "2026-01-01", "2026-01-15", "",
                LoanStatus.BORROWED, 0, false, 0.0));
        Path loansPath = temporaryDirectory.resolve("books_loans.csv");

        CsvService csvService = new CsvService();
        assertEquals(1, csvService.saveLoans(loansPath, original));
        LoanRepository reloaded = new LoanRepository();
        assertEquals(1, csvService.loadLoans(loansPath, reloaded));

        assertTrue(reloaded.findActiveLoan(101, 5).isPresent());
    }
}
