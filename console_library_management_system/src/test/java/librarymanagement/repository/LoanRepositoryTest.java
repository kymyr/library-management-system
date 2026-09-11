package librarymanagement.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import librarymanagement.model.Loan;
import librarymanagement.model.LoanStatus;
import org.junit.jupiter.api.Test;

class LoanRepositoryTest {
    // Loan IDs reject duplicates and generate the next ID.
    @Test
    void managesIds() {
        LoanRepository loanRepository = new LoanRepository();
        Loan loan = loan(10, 101, 5, LoanStatus.BORROWED, false);

        assertTrue(loanRepository.addLoan(loan));
        assertFalse(loanRepository.addLoan(loan(10, 102, 6, LoanStatus.BORROWED, false)));
        assertEquals(11, loanRepository.getNextLoanId());
    }

    // Active lookup returns only the matching borrowed loan.
    @Test
    void findsActiveLoan() {
        LoanRepository loanRepository = new LoanRepository();
        Loan borrowed = loan(1, 101, 5, LoanStatus.BORROWED, false);
        loanRepository.addLoan(borrowed);
        loanRepository.addLoan(loan(2, 101, 6, LoanStatus.RETURNED, false));

        assertEquals(borrowed, loanRepository.findActiveLoan(101, 5).orElseThrow());
        assertTrue(loanRepository.findActiveLoan(101, 6).isEmpty());
    }

    // Replacing a loan stores the returned version.
    @Test
    void replacesLoan() {
        LoanRepository loanRepository = new LoanRepository();
        Loan borrowed = loan(1, 101, 5, LoanStatus.BORROWED, false);
        Loan returned = new Loan(1, 101, 5, "2026-09-01", "2026-09-15",
                "2026-09-10", LoanStatus.RETURNED, 0, false, 0.0);
        loanRepository.addLoan(borrowed);

        assertTrue(loanRepository.replaceLoan(borrowed, returned));
        assertTrue(loanRepository.getBorrowedLoans().isEmpty());
        assertEquals(returned, loanRepository.getAllLoans().get(0));
    }

    // Overdue filtering ignores returned loans.
    @Test
    void filtersOverdueLoans() {
        LoanRepository loanRepository = new LoanRepository();
        loanRepository.addLoan(loan(1, 101, 5, LoanStatus.BORROWED, true));
        loanRepository.addLoan(loan(2, 102, 5, LoanStatus.BORROWED, false));
        loanRepository.addLoan(loan(3, 103, 5, LoanStatus.RETURNED, true));

        assertEquals(1, loanRepository.getOverdueBorrowedLoans().size());
        assertEquals(3, loanRepository.findByMemberId(5).size());
    }

    private Loan loan(int loanId, int bookId, int memberId, LoanStatus status, boolean overdue) {
        return new Loan(loanId, bookId, memberId, "2026-09-01", "2026-09-15", "",
                status, overdue ? 1 : 0, overdue, overdue ? 0.50 : 0.0);
    }
}
