package librarymanagement.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import librarymanagement.model.Loan;
import librarymanagement.model.LoanStatus;
import librarymanagement.repository.LoanRepository;

public class LoanCsvReader {
    public int load(Path path, LoanRepository loanRepository) throws IOException {
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
}