package librarymanagement.cli;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import librarymanagement.model.Book;
import librarymanagement.model.Loan;
import librarymanagement.model.Member;
import librarymanagement.repository.BookRepository;
import librarymanagement.repository.MemberRepository;
import librarymanagement.repository.LoanRepository;
import librarymanagement.service.LibraryLoader;
import librarymanagement.service.LoanService;
import librarymanagement.service.MemberService;
import librarymanagement.util.ErrorHandling;

public class Library {
    private final LoanService loanService;
    private final MemberService memberService;
    private final ConsoleInput input;
    private final ConsoleDisplay display;

    public Library(LoanService loanService, MemberService memberService, ConsoleInput input,
            ConsoleDisplay display) {
        this.loanService = loanService;
        this.memberService = memberService;
        this.input = input;
        this.display = display;
    }

    public void registerMember(Scanner sc) {
        System.out.println(MenuDisplay.header("Register a Member"));

        while (true) {
            ConsoleInput.MemberDetails details = input.newMemberDetails(sc);
            if (details == null) {
                return;
            }

            Member member = memberService.buildMember(details.firstName(), details.lastName(),
                    details.email(), LocalDate.now());

            System.out.println("\nNew Member Details:");
            System.out.println(member);
            if (input.confirm("Confirm member registration? (y/n):", sc)) {
                memberService.register(member);
                return;
            }
            System.out.println("Let's edit the member details.\n");
        }
    }

    public void checkOutBook(Scanner sc) {
        System.out.println(MenuDisplay.header("Check Out Book"));

        Member member = input.member(sc);
        if (member == null) {
            return;
        }

        Book book = input.book(sc);
        if (book == null) {
            return;
        }

        if (display.showRejection(loanService.rejectCheckOut(member, book))) {
            return;
        }

        LocalDate checkoutDate = LocalDate.now();
        display.showCheckOutSummary(member, book, checkoutDate, loanService.dueDateFor(checkoutDate));

        if (!input.confirm("Confirm check out? (y/n):", "Check out cancelled.", sc)) {
            return;
        }

        loanService.checkOut(member, book, checkoutDate);
        display.showLoanOutcome(book, member, "borrowed by member");
    }

    public void checkInBook(Scanner sc) {
        System.out.println(MenuDisplay.header("Check In Book"));

        Member member = input.member(sc);
        if (member == null) {
            return;
        }

        if (display.showRejection(loanService.rejectCheckIn(member))) {
            return;
        }

        List<Loan> activeLoans = loanService.activeLoansFor(member.getId());
        if (activeLoans.isEmpty()) {
            System.out.println(member.getId() + " " + member.getName() + " has no books currently borrowed.");
            return;
        }
        display.showBorrowedBooks(member, activeLoans);

        Book book = input.book(sc);
        if (book == null) {
            return;
        }

        Optional<Loan> activeLoan = loanService.findActiveLoan(book.getId(), member.getId());
        if (activeLoan.isEmpty()) {
            System.out.println(member.getId() + " " + member.getName() + " has no active loan for this book.");
            return;
        }

        Loan loan = activeLoan.get();
        LocalDate checkinDate = LocalDate.now();
        int overdueDays = loanService.overdueDaysFor(loan, checkinDate);
        display.showCheckInSummary(member, book, loan, checkinDate, overdueDays,
                loanService.penaltyFor(overdueDays));

        if (!input.confirm("Confirm check in? (y/n):", "Check in cancelled.", sc)) {
            return;
        }

        loanService.checkIn(member, book, loan, checkinDate);
        display.showLoanOutcome(book, member, "returned to inventory by member");
    }

    public void booksMenu(Scanner sc) {
        while (true) {
            int option = input.menuChoice(MenuDisplay.booksMenu(), sc);

            switch (option) {
                case 0:
                    return;
                case 1:
                    display.showAllBooks(sc);
                    break;
                case 2:
                    input.searchBook(sc);
                    break;
                case 3:
                    checkOutBook(sc);
                    break;
                case 4:
                    checkInBook(sc);
                    break;
                default:
                    System.out.println("Invalid input");
            }
        }
    }

    public void membersMenu(Scanner sc) {
        while (true) {
            int option = input.menuChoice(MenuDisplay.membersMenu(), sc);

            switch (option) {
                case 0:
                    return;
                case 1:
                    display.showAllMembers(sc);
                    break;
                case 2:
                    registerMember(sc);
                    break;
                default:
                    System.out.println("Invalid input");
            }
        }
    }

    public void loansMenu(Scanner sc) {
        while (true) {
            int option = input.menuChoice(MenuDisplay.loansMenu(), sc);

            switch (option) {
                case 0:
                    return;
                case 1:
                    display.showLoans(loanService.borrowedLoans(), sc);
                    break;
                case 2:
                    display.showLoans(loanService.overdueLoans(), sc);
                    break;
                case 3: {
                    Member member = input.member(sc);
                    if (member == null) {
                        break;
                    }
                    display.showMemberInfo(member);
                    display.showLoans(loanService.loansForMember(member.getId()), sc);
                    break;
                }
                case 4: {
                    Book book = input.book(sc);
                    if (book == null) {
                        break;
                    }
                    display.showBookInfo(book);
                    display.showLoans(loanService.loansForBook(book.getId()), sc);
                    break;
                }
                default:
                    System.out.println("Invalid input");
            }
        }
    }

    public void run(Scanner sc) {
        while (true) {
            int option = input.menuChoice(MenuDisplay.mainMenu(), sc);

            switch (option) {
                case 0:
                    System.out.println("Exiting System...");
                    return;
                case 1:
                    booksMenu(sc);
                    break;
                case 2:
                    membersMenu(sc);
                    break;
                case 3:
                    loansMenu(sc);
                    break;
                default:
                    System.out.println("Invalid input");
            }
        }
    }

    public static void main(String[] args) {
        BookRepository bookRepo = new BookRepository();
        MemberRepository memberRepo = new MemberRepository();
        LoanRepository loanRepo = new LoanRepository();

        try {
            new LibraryLoader().loadAll(bookRepo, memberRepo, loanRepo);
        } catch (IOException e) {
            System.out.println("Library data could not be loaded.");
        }

        ErrorHandling err = new ErrorHandling();
        ConsoleDisplay display = new ConsoleDisplay(bookRepo, memberRepo);

        Library library = new Library(
                new LoanService(bookRepo, loanRepo), new MemberService(memberRepo),
                new ConsoleInput(bookRepo, memberRepo, display, err), display);

        try (Scanner sc = new Scanner(System.in)) {
            library.run(sc);
        }
    }
}
