package librarymanagement.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import librarymanagement.model.Book;
import librarymanagement.model.Loan;
import librarymanagement.model.LoanStatus;
import librarymanagement.model.Member;
import librarymanagement.repository.BookRepository;
import librarymanagement.repository.LoanRepository;
import librarymanagement.repository.MemberRepository;

// All CSV reading and writing for the library data files
public class CsvService {
    private static final String INVENTORY_HEADER = "bookId,total_copies,available_copies";
    private static final String MEMBER_HEADER =
        "memberId,first_name,last_name,email,join_date,membership_expiry_date,membership_status";
    private static final String LOAN_HEADER =
        "loanId,bookId,memberId,checkout_date,due_date,checkin_date,status,"
        + "overdue_days,is_overdue,penalty_amount";

    public void loadAll(BookRepository bookRepo, MemberRepository memberRepo, LoanRepository loanRepo) throws IOException {
        loadBooksConcurrently(findDataFile("books_catalogue.csv"),
            findDataFile("books_inventory.csv"), bookRepo);
        loadMembers(findDataFile("library_members.csv"), memberRepo);
        loadLoans(findDataFile("books_loans.csv"), loanRepo);
        syncIssuedBooks(loanRepo, memberRepo);
    }

    // make sure to reconcile data file path properly
    public static Path findDataFile(String fileName) {
        Path repositoryPath = Path.of("data", fileName);
        if (Files.exists(repositoryPath)) {
            return repositoryPath;
        }
        return Path.of("..", "data", fileName);
    }

    public int loadBooks(Path cataloguePath, Path inventoryPath, BookRepository bookRepository)
            throws IOException {
        Map<Integer, int[]> inventoryByBookId = readInventory(inventoryPath);
        List<String> catalogueLines = Files.readAllLines(cataloguePath);
        int loadedBooks = 0;

        for (int lineNumber = 1; lineNumber < catalogueLines.size(); lineNumber++) {
            List<String> fields = parseCsvLine(catalogueLines.get(lineNumber));
            if (fields.size() != 4) {
                continue;
            }

            try {
                int bookId = Integer.parseInt(fields.get(0).trim());
                int[] copies = inventoryByBookId.get(bookId);
                if (copies == null) {
                    continue;
                }

                Book book = new Book(bookId, fields.get(1).trim(), fields.get(2).trim(),
                        fields.get(3).trim(), copies[0]);
                book.setAvailable(copies[1]);
                if (bookRepository.importBook(book)) {
                    loadedBooks++;
                }
            } catch (NumberFormatException ignored) {
                // Ignore malformed rows while loading the remaining books.
            }
        }

        return loadedBooks;
    }

    public int loadBooksConcurrently(Path cataloguePath, Path inventoryPath, BookRepository bookRepository) throws IOException {
        Map<Integer, int[]> inventoryByBookId = readInventory(inventoryPath);
        List<String> catalogueLines = Files.readAllLines(cataloguePath);
        List<java.util.concurrent.Callable<Book>> tasks = new ArrayList<>();

        for (int lineNumber = 1; lineNumber < catalogueLines.size(); lineNumber++) {
            String line = catalogueLines.get(lineNumber);
            tasks.add(() -> parseBook(line, inventoryByBookId));
        }

        int imported = 0;
        int failed = 0;
        ExecutorService pool = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors()));

        try {
            List<Future<Book>> futures = pool.invokeAll(tasks);
            for (Future<Book> future : futures) {
                try {
                    Book book = future.get();
                    if (book == null || !bookRepository.importBook(book)) {
                        failed++;
                    } else {
                        imported++;
                    }
                } catch (ExecutionException e) {
                    failed++;
                }
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
            throw new IOException("Book import was interrupted.", e);
        } finally {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                }
            } catch (InterruptedException e) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
                throw new IOException("Book import shutdown was interrupted.", e);
            }
        }

        System.out.println("Concurrent CSV book-row import: " + imported + " imported, " + failed + " failed.");
        return imported;
    }

    private Book parseBook(String line, Map<Integer, int[]> inventoryByBookId) {
        List<String> fields = parseCsvLine(line);
        if (fields.size() != 4) {
            return null;
        }

        try {
            int bookId = Integer.parseInt(fields.get(0).trim());
            int[] copies = inventoryByBookId.get(bookId);
            if (copies == null) {
                return null;
            }

            Book book = new Book(bookId, fields.get(1).trim(), fields.get(2).trim(),
                fields.get(3).trim(), copies[0]);
            book.setAvailable(copies[1]);
            return book;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public int loadMembers(Path path, MemberRepository memberRepository) throws IOException {
        List<String> lines = Files.readAllLines(path);
        int loadedMembers = 0;

        for (int lineNumber = 1; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber).trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] fields = line.split(",", -1);
            if (fields.length != 7) {
                continue;
            }

            try {
                int memberId = Integer.parseInt(fields[0].trim());
                String name = fields[1].trim() + " " + fields[2].trim();
                Member member = new Member(memberId, name, fields[3].trim(), fields[4].trim(),
                        fields[5].trim(), fields[6].trim());
                if (memberRepository.registerMemberQuietly(member)) {
                    loadedMembers++;
                }
            } catch (NumberFormatException ignored) {
                // Ignore malformed rows while loading the remaining members.
            }
        }

        return loadedMembers;
    }

    public int loadLoans(Path path, LoanRepository loanRepository) throws IOException {
        List<String> lines = Files.readAllLines(path);
        int loadedLoans = 0;

        for (int lineNumber = 1; lineNumber < lines.size(); lineNumber++) {
            String[] fields = lines.get(lineNumber).split(",", -1);
            if (fields.length != 10) {
                continue;
            }

            try {
                Loan loan = new Loan(
                    Integer.parseInt(fields[0].trim()),
                    Integer.parseInt(fields[1].trim()),
                    Integer.parseInt(fields[2].trim()),
                    fields[3].trim(),
                    fields[4].trim(),
                    fields[5].trim(),
                    LoanStatus.valueOf(fields[6].trim().toUpperCase()),
                    Integer.parseInt(fields[7].trim()),
                    Boolean.parseBoolean(fields[8].trim()),
                    Double.parseDouble(fields[9].trim()));
                if (loanRepository.addLoan(loan)) {
                    loadedLoans++;
                }
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed rows while loading the remaining loans.
            }
        }

        return loadedLoans;
    }

    public int saveInventory(BookRepository bookRepository) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(INVENTORY_HEADER);

        for (Book book : bookRepository.getAllBooks()) {
            lines.add(book.getId() + "," + book.getTotalQuantity() + "," + book.getAvailable());
        }

        Files.write(findDataFile("books_inventory.csv"), lines);
        return lines.size() - 1;
    }

    public int saveMembers(MemberRepository memberRepository) throws IOException {
        List<Member> members = memberRepository.getAllMembers();
        members.sort(Comparator.comparingInt(Member::getId));

        List<String> lines = new ArrayList<>();
        lines.add(MEMBER_HEADER);

        for (Member member : members) {
            String name = member.getName();
            int space = name.indexOf(' ');
            String firstName = space < 0 ? name : name.substring(0, space);
            String lastName = space < 0 ? "" : name.substring(space + 1);

            lines.add(member.getId() + "," + firstName + "," + lastName + ","
                + member.getEmail() + "," + member.getJoinDate() + ","
                + member.getMembershipExpiryDate() + "," + member.getMembershipStatus());
        }

        Files.write(findDataFile("library_members.csv"), lines);
        return lines.size() - 1;
    }

    public int saveLoans(LoanRepository loanRepository) throws IOException {
        List<Loan> loans = loanRepository.getAllLoans();
        loans.sort(Comparator.comparingInt(Loan::loanId));

        List<String> lines = new ArrayList<>();
        lines.add(LOAN_HEADER);

        for (Loan loan : loans) {
            lines.add(loan.loanId() + "," + loan.bookId() + "," + loan.memberId() + ","
                + loan.checkoutDate() + "," + loan.dueDate() + "," + loan.checkinDate() + ","
                + statusLabel(loan) + "," + loan.overdueDays() + "," + loan.overdue() + ","
                + loan.penaltyAmount());
        }

        Files.write(findDataFile("books_loans.csv"), lines);
        return lines.size() - 1;
    }

    private Map<Integer, int[]> readInventory(Path inventoryPath) throws IOException {
        Map<Integer, int[]> inventoryByBookId = new HashMap<>();
        List<String> lines = Files.readAllLines(inventoryPath);

        for (int lineNumber = 1; lineNumber < lines.size(); lineNumber++) {
            List<String> fields = parseCsvLine(lines.get(lineNumber));
            if (fields.size() != 3) {
                continue;
            }

            try {
                int bookId = Integer.parseInt(fields.get(0).trim());
                int totalCopies = Integer.parseInt(fields.get(1).trim());
                int availableCopies = Integer.parseInt(fields.get(2).trim());
                inventoryByBookId.put(bookId, new int[] { totalCopies, availableCopies });
            } catch (NumberFormatException ignored) {
                // Ignore malformed rows while loading the remaining inventory.
            }
        }

        return inventoryByBookId;
    }

    // titles can contain commas inside quotes, so split manually
    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (character == '"') {
                insideQuotes = !insideQuotes;
            } else if (character == ',' && !insideQuotes) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(character);
            }
        }
        fields.add(field.toString());
        return fields;
    }

    private String statusLabel(Loan loan) {
        String name = loan.status().name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    private static void syncIssuedBooks(LoanRepository loanRepo, MemberRepository memberRepo) {
        for (Loan loan : loanRepo.getBorrowedLoans()) {
            if (memberRepo.containsId(loan.memberId())) {
                memberRepo.findById(loan.memberId()).addIssuedBook(loan.bookId());
            }
        }
    }
}
