package librarymanagement.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LoanTest {
    // Borrowed loans are active.
    @Test
    void borrowedLoanIsActive() {
        Loan loan = loan(LoanStatus.BORROWED);

        assertTrue(loan.isActive());
    }

    // Returned loans are not active.
    @Test
    void returnedLoanIsNotActive() {
        Loan loan = loan(LoanStatus.RETURNED);

        assertFalse(loan.isActive());
    }

    // Returning a loan creates a returned copy with penalty details.
    @Test
    void createsReturnedLoan() {
        Loan borrowed = loan(LoanStatus.BORROWED);

        Loan returned = borrowed.withReturn("2026-09-12", 2, 1.0);

        assertEquals(LoanStatus.RETURNED, returned.status());
        assertEquals("2026-09-12", returned.checkinDate());
        assertEquals(2, returned.overdueDays());
        assertTrue(returned.overdue());
        assertEquals(1.0, returned.penaltyAmount());
    }

    private Loan loan(LoanStatus status) {
        return new Loan(1, 101, 5, "2026-09-01", "2026-09-15", "",
                status, 0, false, 0.0);
    }
}
