package librarymanagement.repository;

import java.util.ArrayList;
import java.util.List;

import librarymanagement.model.Loan;
import librarymanagement.model.LoanStatus;

public class LoanRepository {
    private final List<Loan> loans = new ArrayList<>();

    public boolean addLoan(Loan loan) {
        if (loans.stream().anyMatch(existing -> existing.loanId() == loan.loanId())) {
            return false;
        }
        return loans.add(loan);
    }

    public List<Loan> getAllLoans() {
        return new ArrayList<>(loans);
    }

    public List<Loan> getBorrowedLoans() {
        return loans.stream()
                .filter(loan -> loan.status() == LoanStatus.BORROWED)
                .sorted(java.util.Comparator.comparingInt(Loan::loanId))
                .toList();
    }

    public List<Loan> getOverdueBorrowedLoans() {
        return loans.stream()
                .filter(loan -> loan.status() == LoanStatus.BORROWED && loan.overdue())
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