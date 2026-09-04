package librarymanagement.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import librarymanagement.model.Loan;
import librarymanagement.repository.BookRepository;
import librarymanagement.repository.LoanRepository;
import librarymanagement.repository.MemberRepository;

// Loads the CSV datasets
public class LibraryLoader {

    public void loadAll(BookRepository bookRepo, MemberRepository memberRepo,
            LoanRepository loanRepo) throws IOException {
        new BookCsvReader().load(
                findDataFile("books_catalogue.csv"),
                findDataFile("books_inventory.csv"), bookRepo);
        new MemberCsvReader().load(findDataFile("library_members.csv"), memberRepo);
        new LoanCsvReader().load(findDataFile("books_loans.csv"), loanRepo);
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

    private static void syncIssuedBooks(LoanRepository loanRepo, MemberRepository memberRepo) {
        for (Loan loan : loanRepo.getBorrowedLoans()) {
            if (memberRepo.containsId(loan.memberId())) {
                memberRepo.findById(loan.memberId()).addIssuedBook(loan.bookId());
            }
        }
    }
}
