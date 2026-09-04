package librarymanagement.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import librarymanagement.model.Loan;

public class LoanRepository {
    private final List<Loan> loans = new ArrayList<>();

    public boolean addLoan(Loan loan) {
        if (loans.stream().anyMatch(existing -> existing.loanId() == loan.loanId())) {
            return false;
        }
        return loans.add(loan);
    }

    public int getNextLoanId() {
        return loans.stream().mapToInt(Loan::loanId).max().orElse(0) + 1;
    }

    public Optional<Loan> findActiveLoan(int bookId, int memberId) {
        return loans.stream()
                .filter(loan -> loan.isActive()
                        && loan.bookId() == bookId
                        && loan.memberId() == memberId)
                .findFirst();
    }

    /** Loan is a record, so a check-in swaps the stored instance for an updated copy. */
    public boolean replaceLoan(Loan existing, Loan updated) {
        int index = loans.indexOf(existing);
        if (index < 0) {
            return false;
        }
        loans.set(index, updated);
        return true;
    }

    public List<Loan> getAllLoans() {
        return new ArrayList<>(loans);
    }

    public List<Loan> getBorrowedLoans() {
        return loans.stream()
                .filter(Loan::isActive)
                .sorted(java.util.Comparator.comparingInt(Loan::loanId))
                .toList();
    }

    public List<Loan> getOverdueBorrowedLoans() {
        return loans.stream()
                .filter(loan -> loan.isActive() && loan.overdue())
            .sorted(java.util.Comparator.comparingInt(Loan::loanId))
                .toList();
    }

    public List<Loan> findByMemberId(int memberId) {
        return loans.stream()
            .filter(loan -> loan.memberId() == memberId)
            .sorted(java.util.Comparator.comparingInt(Loan::loanId))
            .toList();
    }

    public List<Loan> findByBookId(int bookId) {
        return loans.stream()
            .filter(loan -> loan.bookId() == bookId)
            .sorted(java.util.Comparator.comparingInt(Loan::loanId))
            .toList();
    }
}