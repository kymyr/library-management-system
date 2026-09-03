package librarymanagement.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import librarymanagement.model.Book;
import librarymanagement.repository.BookRepository;

public class BookCsvReader {
    public int load(Path cataloguePath, Path inventoryPath, BookRepository bookRepository) throws IOException {
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

    private List<String> parseCsvLine(String line) {
        List<String> fields = new java.util.ArrayList<>();
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
}