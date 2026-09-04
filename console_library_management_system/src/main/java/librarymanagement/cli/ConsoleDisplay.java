package librarymanagement.cli;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import librarymanagement.model.Book;
import librarymanagement.model.Loan;
import librarymanagement.model.Member;
import librarymanagement.repository.BookRepository;
import librarymanagement.repository.MemberRepository;

/** All console rendering for books, members and loans. */
public class ConsoleDisplay {
    private static final int PAGE_SIZE = 50;

    private final BookRepository bookRepo;
    private final MemberRepository memberRepo;

    public ConsoleDisplay(BookRepository bookRepo, MemberRepository memberRepo) {
        this.bookRepo = bookRepo;
        this.memberRepo = memberRepo;
    }

    /**
     * Displays lines in fixed-size pages so large lists (e.g. hundreds of books/members)
     * don't flood the console at once. Returns false when the user backs out.
     */
    public boolean displayPaged(List<String> lines, Scanner sc) {
        if (lines.isEmpty()) {
            System.out.println("None");
            return true;
        }

        int total = lines.size();
        int totalPages = (total + PAGE_SIZE - 1) / PAGE_SIZE;
        int currentPage = 1;
        boolean showPage = true;

        while (true) {
            int start = (currentPage - 1) * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, total);
            if (showPage) {
                for (int i = start; i < end; i++) {
                    System.out.println(lines.get(i));
                }
            }

            System.out.println();
            System.out.print("-- Showing " + (start + 1) + "-" + end + " of " + total
                    + " (Page " + currentPage + " of " + totalPages
                    + "). Enter n for next, p for previous, 0 to go back, or a page number: ");
            String input = sc.nextLine().trim().toLowerCase();
            showPage = true;

            if (input.equals("0")) {
                return false;
            }

            if (input.equals("n")) {
                currentPage = currentPage == totalPages ? 1 : currentPage + 1;
                continue;
            }

            if (input.equals("p")) {
                currentPage = currentPage == 1 ? totalPages : currentPage - 1;
                continue;
            }

            try {
                int requestedPage = Integer.parseInt(input);
                if (requestedPage < 1 || requestedPage > totalPages) {
                    System.out.println("Invalid page number. Please choose a page between 1 and "
                            + totalPages + ".");
                    showPage = false;
                } else {
                    currentPage = requestedPage;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice. Enter n, p, 0, or a valid page number.");
                showPage = false;
            }
        }
    }

    public void showAllBooks(Scanner sc) {
        displayPaged(toLines(bookRepo.getAllBooks()), sc);
    }

    public void showAllMembers(Scanner sc) {
        displayPaged(toLines(memberRepo.getAllMembers()), sc);
    }

    /** Prints the rejection reason if there is one; true means the caller should stop. */
    public boolean showRejection(Optional<String> rejection) {
        rejection.ifPresent(System.out::println);
        return rejection.isPresent();
    }

    public void showBookInfo(Book book) {
        System.out.println("\nBook Information:");
        System.out.println("ID: " + book.getId() + " | Title: " + book.getTitle()
                + " | Author: " + book.getAuthor() + " | ISBN: " + book.getIsbn()
                + " | Total copies: " + book.getTotalQuantity()
                + " | Available copies: " + book.getAvailable());
    }

    public void showMemberInfo(Member member) {
        System.out.println("Member Information:");
        System.out.println(member);
    }

    public void showCheckOutSummary(Member member, Book book, LocalDate checkoutDate,
            LocalDate dueDate) {
        System.out.println("\nCheck Out Summary:");
        showMemberInfo(member);
        showBookInfo(book);
        System.out.println("\nAvailable copies after check out: " + (book.getAvailable() - 1));
        System.out.println("Checkout date: " + checkoutDate + " | Due date: " + dueDate);
    }

    public void showCheckInSummary(Member member, Book book, Loan loan, LocalDate checkinDate,
            int overdueDays, double penalty) {
        System.out.println("\nCheck In Summary:");
        showMemberInfo(member);
        showBookInfo(book);
        System.out.println("\nAvailable copies after check in: " + (book.getAvailable() + 1));
        System.out.println("Checkout date: " + loan.checkoutDate() + " | Due date: " + loan.dueDate()
                + " | Check-in date: " + checkinDate);
        if (overdueDays > 0) {
            System.out.printf("OVERDUE by %d day(s). Penalty due: $%.2f%n", overdueDays, penalty);
        } else {
            System.out.println("Returned on time. No penalty due.");
        }
    }

    public void showLoanOutcome(Book book, Member member, String action) {
        showBookInfo(book);
        System.out.println("Book " + book.getId() + " successfully " + action + " member "
                + member.getId() + ".");
    }

    public void showLoans(List<Loan> loans, Scanner sc) {
        if (loans.isEmpty()) {
            System.out.println("No loan history found.");
            return;
        }

        System.out.println("Loan History:");
        List<String> lines = new ArrayList<>();
        for (Loan loan : loans) {
            lines.add(formatLoan(loan));
        }
        displayPaged(lines, sc);
    }

    /** Lists the member's open loans so the check-in prompt has the right book IDs on screen. */
    public void showBorrowedBooks(Member member, List<Loan> activeLoans) {
        System.out.println("\nBooks currently borrowed by " + member.getName() + ":");
        for (Loan loan : activeLoans) {
            StringBuilder details = new StringBuilder("Book ID: ").append(loan.bookId());
            if (bookRepo.containsId(loan.bookId())) {
                Book borrowed = bookRepo.findById(loan.bookId());
                details.append(" | Title: ").append(borrowed.getTitle())
                        .append(" | Author: ").append(borrowed.getAuthor())
                        .append(" | ISBN: ").append(borrowed.getIsbn());
            }
            System.out.println(details + " | Checkout: " + loan.checkoutDate()
                    + " | Due: " + loan.dueDate());
        }
        System.out.println();
    }

    private String formatLoan(Loan loan) {
        Book book = bookRepo.findById(loan.bookId());
        Member member = memberRepo.findById(loan.memberId());
        String title = book == null ? "Unknown book" : book.getTitle();
        String memberName = member == null ? "Unknown member" : member.getName();
        String checkinDate = loan.checkinDate().isEmpty() ? "N/A" : loan.checkinDate();
        return "Loan ID: " + loan.loanId() + " | Book ID: " + loan.bookId()
                + " | Title: " + title + " | Member ID: " + loan.memberId()
                + " | Member: " + memberName + " | Checkout: " + loan.checkoutDate()
                + " | Due: " + loan.dueDate() + " | Check-in: " + checkinDate
                + " | Status: " + loan.status() + " | Overdue days: " + loan.overdueDays()
                + " | Penalty: $" + loan.penaltyAmount();
    }

    private List<String> toLines(List<?> items) {
        List<String> lines = new ArrayList<>();
        for (Object item : items) {
            lines.add(item.toString());
        }
        return lines;
    }
}
