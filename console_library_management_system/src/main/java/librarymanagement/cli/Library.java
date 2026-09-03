package librarymanagement.cli;

import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import librarymanagement.model.Book;
import librarymanagement.model.Member;
import librarymanagement.model.Loan;
import librarymanagement.repository.BookRepository;
import librarymanagement.repository.MemberRepository;
import librarymanagement.service.BookCsvReader;
import librarymanagement.service.LoanCsvReader;
import librarymanagement.service.MemberCsvReader;
import librarymanagement.repository.LoanRepository;
import librarymanagement.util.ErrorHandling;

public class Library {
    private static final String MENU_BORDER = "-----------------------------------------------------------";
    private static final int MENU_WIDTH = MENU_BORDER.length();
    private static final int PAGE_SIZE = 50;

    private static Path findDataFile(String fileName) {
        Path repositoryPath = Path.of("data", fileName);
        if (Files.exists(repositoryPath)) {
            return repositoryPath;
        }
        return Path.of("..", "data", fileName);
    }

    private static String centerText(String text, int width) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= width) {
            return text;
        }
        int totalPadding = width - text.length();
        int leftPadding = totalPadding / 2;
        int rightPadding = totalPadding - leftPadding;
        return " ".repeat(leftPadding) + text + " ".repeat(rightPadding);
    }

    private static String buildMenu(String title, String... options) {
        StringBuilder sb = new StringBuilder(MENU_BORDER)
            .append("\n")
            .append(centerText(title, MENU_WIDTH))
                .append("\n")
                .append(MENU_BORDER)
                .append("\n")
                .append(centerText("Select from the following options:", MENU_WIDTH));
        for (String option : options) {
            sb.append("\n").append(centerText(option, MENU_WIDTH));
        }
        sb.append("\n").append(MENU_BORDER).append("\n").append("Enter choice:");
        return sb.toString();
    }

    public String showMainMenu() {
        return MENU_BORDER +
            "\n" + centerText("Console Library Management System", MENU_WIDTH) +
            "\n" + buildMenu(
                "Main Menu",
                "0 - Exit",
                "1 - Books",
                "2 - Members",
                "3 - Loans"
        );
    }

    public String showBooksMenu() {
        return buildMenu("Books",
                "0 - Back to Main Menu",
                "1 - Show All Books",
                "2 - Search Book",
                "3 - Check In Book",
                "4 - Check Out Book"
        );
    }

    public String showSearchBookMenu() {
        return buildMenu("Search Books",
                "0 - Back to Books Menu",
                "1 - Search by Title",
                "2 - Search by Author",
                "3 - Search by ISBN"
        );
    }

    public String showMembersMenu() {
        return buildMenu("Members",
                "0 - Back to Main Menu",
                "1 - Show All Members",
                "2 - Register a Member"
        );
    }

    public String showLoansMenu() {
        return buildMenu("Loans",
                "0 - Back to Main Menu",
                "1 - Show Borrowed Books",
            "2 - Show Overdue Books",
            "3 - Search Loan History by Member ID",
            "4 - Search Loan History by Book ID"
        );
    }

    /**
     * Displays lines in fixed-size pages so large lists (e.g. hundreds of books/members)
     * don't flood the console at once.
     */
    private boolean displayPaged(List<String> lines, Scanner sc) {
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
                    System.out.println("Invalid page number. Please choose a page between 1 and " + totalPages + ".");
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

    public void registerMember(Scanner sc, MemberRepository memberRepo, ErrorHandling err) {
        while (true) {
            String firstName = err.validateStringInput("", "Enter First Name:", sc);
            String lastName = err.validateStringInput("", "Enter Last Name:", sc);
            String email = err.validateEmail("Enter Email:", sc);
            LocalDate joinDate = LocalDate.now();
            LocalDate expiryDate = joinDate.plusYears(1);
            Member member = new Member(memberRepo.getNextMemberId(), firstName + " " + lastName,
                    email, joinDate.toString(), expiryDate.toString(), "Active");

            System.out.println("\nNew Member Details:");
            System.out.println(member);
            if (err.validateConfirmation("Confirm member registration? (y/n):", sc)) {
                memberRepo.registerMember(member);
                return;
            }
            System.out.println("Let's edit the member details.\n");
        }
    }

    public void showAllBooksPaged(BookRepository bookRepo, Scanner sc) {
        List<Book> allBooks = bookRepo.getAllBooks();
        List<String> lines = new ArrayList<>();
        for (Book b : allBooks) {
            lines.add(b.toString());
        }
        displayPaged(lines, sc);
    }

    public void showAllMembersPaged(MemberRepository memberRepo, Scanner sc) {
        List<Member> allMembers = memberRepo.getAllMembers();
        List<String> lines = new ArrayList<>();
        for (Member member : allMembers) {
            lines.add(member.toString());
        }
        displayPaged(lines, sc);
    }

    private void showBookInfo(Book book) {
        System.out.println("Book Information:");
        System.out.println("ID: " + book.getId() + " | Title: " + book.getTitle()
                + " | Author: " + book.getAuthor() + " | ISBN: " + book.getIsbn()
                + " | Total copies: " + book.getTotalQuantity()
                + " | Available copies: " + book.getAvailable());
    }

    private void showMemberInfo(Member member) {
        System.out.println("Member Information:");
        System.out.println(member);
    }

    private String formatLoan(Loan loan, BookRepository bookRepo, MemberRepository memberRepo) {
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

    private void showLoans(List<Loan> loans, BookRepository bookRepo,
            MemberRepository memberRepo, Scanner sc) {
        if (loans.isEmpty()) {
            System.out.println("No loan history found.");
            return;
        }
        System.out.println("Loan History:");
        List<String> lines = new ArrayList<>();
        for (Loan loan : loans) {
            lines.add(formatLoan(loan, bookRepo, memberRepo));
        }
        displayPaged(lines, sc);
    }

    public void loansMenu(Scanner sc, BookRepository bookRepo,
            MemberRepository memberRepo, LoanRepository loanRepo, ErrorHandling err) {
        while (true) {
            int option = err.validateMenuChoice(-1, showLoansMenu(), sc);

            switch (option) {
                case 0:
                    return;
                case 1:
                    showLoans(loanRepo.getBorrowedLoans(), bookRepo, memberRepo, sc);
                    break;
                case 2: {
                    showLoans(loanRepo.getOverdueBorrowedLoans(), bookRepo, memberRepo, sc);
                    break;
                }
                case 3: {
                    int memberId = err.validateLookupId("Enter Member Id:", sc);
                    if (!memberRepo.containsId(memberId)) {
                        System.out.println("Member ID not found.");
                        break;
                    }
                    showMemberInfo(memberRepo.findById(memberId));
                    showLoans(loanRepo.findByMemberId(memberId), bookRepo, memberRepo, sc);
                    break;
                }
                case 4: {
                    int bookId = err.validateLookupId("Enter Book Id:", sc);
                    if (!bookRepo.containsId(bookId)) {
                        System.out.println("Book ID not found.");
                        break;
                    }
                    showBookInfo(bookRepo.findById(bookId));
                    showLoans(loanRepo.findByBookId(bookId), bookRepo, memberRepo, sc);
                    break;
                }
                default:
                    System.out.println("Invalid input");
            }
        }
    }

    /**
     * Walks the user through the Search Book submenu, letting them pick a matching
     * book by ID. Returns null if the user backs out without selecting one.
     */
    public Book searchBookFlow(Scanner sc, BookRepository bookRepo, ErrorHandling err) {
        while (true) {
            int option = err.validateMenuChoice(-1, showSearchBookMenu(), sc);
            List<Book> results;

            switch (option) {
                case 0:
                    return null;
                case 1: {
                    String query = err.validateStringInput("", "Enter Book Title:", sc);
                    results = bookRepo.searchByTitle(query);
                    break;
                }
                case 2: {
                    String query = err.validateStringInput("", "Enter Author Name:", sc);
                    results = bookRepo.searchByAuthor(query);
                    break;
                }
                case 3: {
                    String isbn = err.validateIsbn("Enter ISBN:", sc);
                    results = bookRepo.searchByIsbn(isbn);
                    break;
                }
                default:
                    System.out.println("Invalid input");
                    continue;
            }

            if (results.isEmpty()) {
                System.out.println("No books found.");
                continue;
            }

            List<String> lines = new ArrayList<>();
            for (Book b : results) {
                lines.add(b.toString());
            }
            if (!displayPaged(lines, sc)) {
                continue;
            }

            System.out.print("Enter Book Id to select, or 0 to search again: ");
            String input = sc.nextLine().trim();
            if (input.equals("0")) {
                continue;
            }

            try {
                int selectedId = Integer.parseInt(input);
                for (Book b : results) {
                    if (b.getId() == selectedId) {
                        return b;
                    }
                }
                System.out.println("That Id isn't in the results shown.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }
    }

    public void checkOutBook(Scanner sc, BookRepository bookRepo, MemberRepository memberRepo, ErrorHandling err) {
        Book b = searchBookFlow(sc, bookRepo, err);
        if (b == null) {
            return;
        }

        int memberId = err.validateID(-1, "Enter Member Id:", sc);
        Member member = memberRepo.findById(memberId);

        if (member == null || !b.isAvailable() || !member.canIssueMore()) {
            System.out.println("Book can't be issued!");
            return;
        }

        if (member.getIssuedBookIds().contains(b.getId())) {
            System.out.println("Book already issued!");
            return;
        }

        bookRepo.issueBook(b.getId());
        member.addIssuedBook(b.getId(), bookRepo);

        System.out.println("Book " + "'" + b.getTitle() + "' issued to " + member.getName() + ".");
    }

    public void checkInBook(Scanner sc, BookRepository bookRepo, MemberRepository memberRepo, ErrorHandling err) {
        Book b = searchBookFlow(sc, bookRepo, err);
        if (b == null) {
            return;
        }

        int memberId = err.validateID(-1, "Enter Member Id:", sc);
        Member member = memberRepo.findById(memberId);

        if (member == null) {
            System.out.println("Book can't be returned.");
            return;
        }

        if (!member.getIssuedBookIds().contains(b.getId())) {
            System.out.println(member.getName() + " didn't issue this book!");
            return;
        }

        bookRepo.returnBook(b.getId());
        member.removeIssuedBook(b.getId(), bookRepo);

        System.out.println("Book " + "'" + b.getTitle() + "' returned by " + member.getName() + ".");
    }

    public void booksMenu(Scanner sc, BookRepository bookRepo, MemberRepository memberRepo, ErrorHandling err) {
        while (true) {
            int option = err.validateMenuChoice(-1, showBooksMenu(), sc);

            switch (option) {
                case 0:
                    return;
                case 1:
                    showAllBooksPaged(bookRepo, sc);
                    break;
                case 2:
                    searchBookFlow(sc, bookRepo, err);
                    break;
                case 3:
                    checkInBook(sc, bookRepo, memberRepo, err);
                    break;
                case 4:
                    checkOutBook(sc, bookRepo, memberRepo, err);
                    break;
                default:
                    System.out.println("Invalid input");
            }
        }
    }

    public void membersMenu(Scanner sc, MemberRepository memberRepo, ErrorHandling err) {
        while (true) {
            int option = err.validateMenuChoice(-1, showMembersMenu(), sc);

            switch (option) {
                case 0:
                    return;
                case 1:
                    showAllMembersPaged(memberRepo, sc);
                    break;
                case 2:
                    registerMember(sc, memberRepo, err);
                    break;
                default:
                    System.out.println("Invalid input");
            }
        }
    }

    public static void main (String[] args) {
        BookRepository bookRepo = new BookRepository();
        MemberRepository memberRepo = new MemberRepository();
        LoanRepository loanRepo = new LoanRepository();
        Library library = new Library();
        ErrorHandling err = new ErrorHandling();

        Scanner sc = new Scanner(System.in);

        try {
            new BookCsvReader().load(
                findDataFile("books_catalogue.csv"),
                findDataFile("books_inventory.csv"), bookRepo);

            new MemberCsvReader().load(
                    findDataFile("library_members.csv"), memberRepo);
            new LoanCsvReader().load(
                    findDataFile("books_loans.csv"), loanRepo);
        } catch (IOException e) {
            System.out.println("Library data could not be loaded.");
        }

        while (true) {
            int option = err.validateMenuChoice(-1, library.showMainMenu(), sc);

            switch (option) {
                case 0:
                    System.out.println("Exiting System...");
                    System.exit(0);
                case 1:
                    library.booksMenu(sc, bookRepo, memberRepo, err);
                    break;
                case 2:
                    library.membersMenu(sc, memberRepo, err);
                    break;
                case 3:
                    library.loansMenu(sc, bookRepo, memberRepo, loanRepo, err);
                    break;
                default:
                    System.out.println("Invalid input");
            }
        }
    }
}
