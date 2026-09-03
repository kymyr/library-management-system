package librarymanagement.cli;

import java.util.*;

import librarymanagement.model.Book;
import librarymanagement.model.Member;
import librarymanagement.repository.BookRepository;
import librarymanagement.repository.MemberRepository;
import librarymanagement.util.ErrorHandling;

public class Library {
    private static final String MENU_BORDER = "-----------------------------------------------------------";
    private static final int MENU_WIDTH = MENU_BORDER.length();
    private static final int PAGE_SIZE = 20;

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

    private static String buildMenu(String... options) {
        StringBuilder sb = new StringBuilder(MENU_BORDER);
        for (String option : options) {
            sb.append("\n").append(centerText(option, MENU_WIDTH));
        }
        sb.append("\n").append(MENU_BORDER).append("\n").append("Enter choice:");
        return sb.toString();
    }

    public String showMainMenu() {
        return MENU_BORDER +
            "\n" + centerText("Console Library Management System", MENU_WIDTH) +
            "\n" + MENU_BORDER +
            "\n" + centerText("Select from the following options:", MENU_WIDTH) +
            "\n\n" + buildMenu(
                "0 - Exit",
                "1 - Books",
                "2 - Members"
        );
    }

    public String showBooksMenu() {
        return buildMenu(
                "0 - Back to Main Menu",
                "1 - Show All Books",
                "2 - Search Book",
                "3 - Check In Book",
                "4 - Check Out Book"
        );
    }

    public String showSearchBookMenu() {
        return buildMenu(
                "0 - Back to Books Menu",
                "1 - Search by Title",
                "2 - Search by Author",
                "3 - Search by ISBN"
        );
    }

    public String showMembersMenu() {
        return buildMenu(
                "0 - Back to Main Menu",
                "1 - Show All Members",
                "2 - Register a Member"
        );
    }

    /**
     * Displays lines in fixed-size pages so large lists (e.g. hundreds of books/members)
     * don't flood the console at once.
     */
    private void displayPaged(List<String> lines, Scanner sc) {
        if (lines.isEmpty()) {
            System.out.println("None");
            return;
        }

        int total = lines.size();
        for (int start = 0; start < total; start += PAGE_SIZE) {
            int end = Math.min(start + PAGE_SIZE, total);
            for (int i = start; i < end; i++) {
                System.out.println(lines.get(i));
            }

            boolean isLastPage = end >= total;
            if (isLastPage) {
                break;
            }

            System.out.println();
            System.out.print("-- Showing " + start + "-" + (end - 1) + " of " + total
                    + ". Press Enter for more, or 0 to go back: ");
            String input = sc.nextLine();
            if (input.trim().equals("0")) {
                return;
            }
        }
    }

    public void registerMember(Scanner sc, MemberRepository memberRepo, ErrorHandling err) {
        int memberId = err.validateID(-1, "Enter Member Id:", sc);
        String memberName = err.validateStringInput("", "Enter Member Name:", sc);

        Member member = new Member(memberId, memberName);
        memberRepo.registerMember(member);
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
            displayPaged(lines, sc);

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
        Library library = new Library();
        ErrorHandling err = new ErrorHandling();

        Scanner sc = new Scanner(System.in);

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
                default:
                    System.out.println("Invalid input");
            }
        }
    }
}
