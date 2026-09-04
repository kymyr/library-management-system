package librarymanagement.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import librarymanagement.model.Book;
import librarymanagement.model.Loan;
import librarymanagement.model.LoanStatus;
import librarymanagement.model.Member;
import librarymanagement.repository.BookRepository;
import librarymanagement.repository.LoanRepository;

// For check in and check out
public class LoanService {
    public static final int LOAN_PERIOD_DAYS = 14;
    public static final double PENALTY_PER_DAY = 0.50;

    private final BookRepository bookRepo;
    private final LoanRepository loanRepo;

    public LoanService(BookRepository bookRepo, LoanRepository loanRepo) {
        this.bookRepo = bookRepo;
        this.loanRepo = loanRepo;
    }

    public LocalDate dueDateFor(LocalDate checkoutDate) {
        return checkoutDate.plusDays(LOAN_PERIOD_DAYS);
    }

    public Optional<String> rejectCheckOut(Member member, Book book) {
        if (!member.hasActiveMembership()) {
            return Optional.of("Membership is not active. Book can't be checked out.");
        }
        if (!book.isAvailable()) {
            return Optional.of("No copies available for check out.");
        }
        if (loanRepo.findActiveLoan(book.getId(), member.getId()).isPresent()) {
            return Optional.of(member.getName() + " already has this book checked out.");
        }
        if (!member.canIssueMore()) {
            return Optional.of(member.getName() + " has reached the maximum of "
                    + Member.MAX_ISSUED + " borrowed books.");
        }
        return Optional.empty();
    }

    public Optional<String> rejectCheckIn(Member member) {
        if (!member.hasActiveMembership()) {
            return Optional.of("Member " + member.getId()
                    + " membership has expired. Need to renew.");
        }
        return Optional.empty();
    }

    public Loan checkOut(Member member, Book book, LocalDate checkoutDate) {        Loan loan = new Loan(loanRepo.getNextLoanId(), book.getId(), member.getId(),
                checkoutDate.toString(), dueDateFor(checkoutDate).toString(), "",
                LoanStatus.BORROWED, 0, false, 0.0);

        bookRepo.issueBook(book.getId());
        member.addIssuedBook(book.getId());
        loanRepo.addLoan(loan);
        return loan;
    }

    public Loan checkIn(Member member, Book book, Loan loan, LocalDate checkinDate) {
        int overdueDays = overdueDaysFor(loan, checkinDate);
        Loan returned = loan.withReturn(checkinDate.toString(), overdueDays, penaltyFor(overdueDays));

        loanRepo.replaceLoan(loan, returned);
        bookRepo.returnBook(book.getId());
        member.removeIssuedBook(book.getId());
        return returned;
    }

    public int overdueDaysFor(Loan loan, LocalDate checkinDate) {
        long daysLate = ChronoUnit.DAYS.between(LocalDate.parse(loan.dueDate()), checkinDate);
        return (int) Math.max(0, daysLate);
    }

    public double penaltyFor(int overdueDays) {
        return overdueDays * PENALTY_PER_DAY;
    }

    public Optional<Loan> findActiveLoan(int bookId, int memberId) {
        return loanRepo.findActiveLoan(bookId, memberId);
    }

    public List<Loan> activeLoansFor(int memberId) {
        return loanRepo.findByMemberId(memberId).stream()
                .filter(Loan::isActive)
                .toList();
    }

    public List<Loan> borrowedLoans() {
        return loanRepo.getBorrowedLoans();
    }

    public List<Loan> overdueLoans() {
        return loanRepo.getOverdueBorrowedLoans();
    }

    public List<Loan> loansForMember(int memberId) {
        return loanRepo.findByMemberId(memberId);
    }

    public List<Loan> loansForBook(int bookId) {
        return loanRepo.findByBookId(bookId);
    }
}
