package librarymanagement.cli;

import java.util.List;
import java.util.Scanner;

import librarymanagement.model.Book;
import librarymanagement.model.Member;
import librarymanagement.repository.BookRepository;
import librarymanagement.repository.MemberRepository;
import librarymanagement.util.ErrorHandling;

public class ConsoleInput {
    public record MemberDetails(String firstName, String lastName, String email) {
    }

    private final BookRepository bookRepo;
    private final MemberRepository memberRepo;
    private final ConsoleDisplay display;
    private final ErrorHandling err;

    public ConsoleInput(BookRepository bookRepo, MemberRepository memberRepo,
            ConsoleDisplay display, ErrorHandling err) {
        this.bookRepo = bookRepo;
        this.memberRepo = memberRepo;
        this.display = display;
        this.err = err;
    }

    public int menuChoice(String menu, Scanner sc) {
        return err.validateMenuChoice(-1, menu, sc);
    }

    public boolean confirm(String prompt, Scanner sc) {
        return err.validateConfirmation(prompt, sc);
    }

    public boolean confirm(String prompt, String cancelMessage, Scanner sc) {
        if (err.validateConfirmation(prompt, sc)) {
            return true;
        }
        System.out.println(cancelMessage);
        return false;
    }

    public Member member(Scanner sc) {
        while (true) {
            int memberId = err.validateCancellableId("Enter Member Id:", sc);
            if (memberId == ErrorHandling.CANCEL) {
                return null;
            }
            if (memberRepo.containsId(memberId)) {
                return memberRepo.findById(memberId);
            }
            System.out.println("Member ID not found. Please try again.");
        }
    }

    public Book book(Scanner sc) {
        while (true) {
            int bookId = err.validateCancellableId("Enter Book Id:", sc);
            if (bookId == ErrorHandling.CANCEL) {
                return null;
            }
            if (bookRepo.containsId(bookId)) {
                return bookRepo.findById(bookId);
            }
            System.out.println("Book ID not found. Please try again.");
        }
    }

    public MemberDetails newMemberDetails(Scanner sc) {
        String firstName = err.validateCancellableString("Enter First Name:", sc);
        if (firstName == null) {
            return null;
        }

        String lastName = err.validateCancellableString("Enter Last Name:", sc);
        if (lastName == null) {
            return null;
        }

        String email = err.validateCancellableEmail("Enter Email:", sc);
        if (email == null) {
            return null;
        }

        return new MemberDetails(firstName, lastName, email);
    }

    public Book searchBook(Scanner sc) {
        while (true) {
            int option = menuChoice(MenuDisplay.searchBookMenu(), sc);
            List<Book> results;

            switch (option) {
                case 0:
                    return null;
                case 1:
                    results = bookRepo.searchByTitle(
                            err.validateStringInput("", "Enter Book Title:", sc));
                    break;
                case 2:
                    results = bookRepo.searchByAuthor(
                            err.validateStringInput("", "Enter Author Name:", sc));
                    break;
                case 3:
                    results = bookRepo.searchByIsbn(err.validateIsbn("Enter ISBN:", sc));
                    break;
                default:
                    System.out.println("Invalid input");
                    continue;
            }

            Book selected = selectFromResults(results, sc);
            if (selected != null) {
                return selected;
            }
        }
    }

    private Book selectFromResults(List<Book> results, Scanner sc) {
        if (results.isEmpty()) {
            System.out.println("No books found.");
            return null;
        }

        List<String> lines = results.stream().map(Book::toString).toList();
        if (!display.displayPaged(lines, sc)) {
            return null;
        }

        System.out.print("Enter Book Id to select, or 0 to search again: ");
        String choice = sc.nextLine().trim();
        if (choice.equals("0")) {
            return null;
        }

        try {
            int selectedId = Integer.parseInt(choice);
            for (Book b : results) {
                if (b.getId() == selectedId) {
                    return b;
                }
            }
            System.out.println("Invalid Book Id.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
        return null;
    }
}
