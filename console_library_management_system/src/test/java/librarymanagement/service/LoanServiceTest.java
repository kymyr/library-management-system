package librarymanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import librarymanagement.exception.BookUnavailableException;
import librarymanagement.model.Book;
import librarymanagement.model.Loan;
import librarymanagement.model.LoanStatus;
import librarymanagement.model.Member;
import librarymanagement.repository.BookRepository;
import librarymanagement.repository.LoanRepository;
import org.junit.jupiter.api.Test;

class LoanServiceTest {
    // CsvService writes to the real data/ folder, so tests use dummy data.
    private static class NoOpCsvService extends CsvService {
        @Override
        public int saveLoans(LoanRepository loanRepository) {
            return 0;
        }

        @Override
        public int saveInventory(BookRepository bookRepository) {
            return 0;
        }
    }

    private final BookRepository bookRepo = new BookRepository();
    private final LoanRepository loanRepo = new LoanRepository();
    private final LoanService loanService = new LoanService(bookRepo, loanRepo, new NoOpCsvService());

    // Due date is fourteen days after checkout.
    @Test
    void dueDateIsFourteenDaysLater() {
        LocalDate checkoutDate = LocalDate.of(2026, 1, 1);

        assertEquals(LocalDate.of(2026, 1, 15), loanService.dueDateFor(checkoutDate));
    }

    // Expired members cannot check out.
    @Test
    void rejectsExpiredMembership() {
        Member member = expiredMember();
        Book book = new Book(1, "Title", "Author", "9780000000001", 1);

        assertTrue(loanService.rejectCheckOut(member, book).isPresent());
    }

    // Unavailable books throw instead of returning a rejection message.
    @Test
    void throwsWhenBookUnavailable() {
        Member member = activeMember();
        Book book = new Book(2, "Title", "Author", "9780000000002", 1);
        book.decrementAvailable();

        assertThrows(BookUnavailableException.class, () -> loanService.rejectCheckOut(member, book));
    }

    // A member already holding the book cannot check it out again.
    @Test
    void rejectsDuplicateActiveLoan() {
        Member member = activeMember();
        Book book = new Book(3, "Title", "Author", "9780000000003", 2);
        loanRepo.addLoan(new Loan(1, book.getId(), member.getId(), "2026-01-01", "2026-01-15",
                "", LoanStatus.BORROWED, 0, false, 0.0));

        assertTrue(loanService.rejectCheckOut(member, book).isPresent());
    }

    // Members at the issue limit cannot check out more books.
    @Test
    void rejectsMemberAtIssueLimit() {
        Member member = activeMember();
        Book book = new Book(4, "Title", "Author", "9780000000004", 1);
        for (int bookId = 1; bookId <= Member.MAX_ISSUED; bookId++) {
            member.addIssuedBook(bookId);
        }

        assertTrue(loanService.rejectCheckOut(member, book).isPresent());
    }

    // Checking out an available book creates a borrowed loan and reduces availability.
    @Test
    void checkOutCreatesLoan() throws Exception {
        Member member = activeMember();
        Book book = new Book(5, "Title", "Author", "9780000000005", 1);
        bookRepo.importBook(book);

        Loan loan = loanService.checkOut(member, book, LocalDate.of(2026, 1, 1));

        assertEquals(0, book.getAvailable());
        assertEquals(LoanStatus.BORROWED, loan.status());
        assertTrue(member.getIssuedBookIds().contains(book.getId()));
    }

    // Checking in a loan marks it returned and restores availability.
    @Test
    void checkInReturnsLoan() throws Exception {
        Member member = activeMember();
        Book book = new Book(6, "Title", "Author", "9780000000006", 1);
        bookRepo.importBook(book);
        Loan loan = loanService.checkOut(member, book, LocalDate.of(2026, 1, 1));

        Loan returned = loanService.checkIn(member, book, loan, LocalDate.of(2026, 1, 10));

        assertEquals(1, book.getAvailable());
        assertEquals(LoanStatus.RETURNED, returned.status());
        assertTrue(member.getIssuedBookIds().isEmpty());
    }

    // Returning late calculates overdue days and a matching penalty.
    @Test
    void overdueReturnHasPenalty() {
        Loan loan = new Loan(1, 1, 1, "2026-01-01", "2026-01-15", "",
                LoanStatus.BORROWED, 0, false, 0.0);

        int overdueDays = loanService.overdueDaysFor(loan, LocalDate.of(2026, 1, 18));

        assertEquals(3, overdueDays);
        assertEquals(1.5, loanService.penaltyFor(overdueDays));
    }

    private Member activeMember() {
        return new Member(1, "Active Member", "active@example.com",
                "2026-01-01", "2027-01-01", "Active");
    }

    private Member expiredMember() {
        return new Member(2, "Expired Member", "expired@example.com",
                "2025-01-01", "2025-12-31", "Expired");
    }
}
