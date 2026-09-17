package librarymanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import librarymanagement.model.Book;
import librarymanagement.repository.BookRepository;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvServiceConcurrencyTest {
    @TempDir
    Path temporaryDirectory;

    // Uses catalogue csv sequential/concurrent import comparison.
    @Test
    void comparesRealCatalogue() throws IOException {
        Path sourceCatalogue = findDataFile("books_catalogue.csv");
        Path sourceInventory = findDataFile("books_inventory.csv");
        Assumptions.assumeTrue(Files.exists(sourceCatalogue) && Files.exists(sourceInventory));

        Path cataloguePath = temporaryDirectory.resolve("copy_books_catalogue.csv");
        Path inventoryPath = temporaryDirectory.resolve("copy_books_inventory.csv");
        Files.copy(sourceCatalogue, cataloguePath);
        Files.copy(sourceInventory, inventoryPath);

        CsvService csvService = new CsvService();
        BookRepository sequentialRepository = new BookRepository();
        BookRepository concurrentRepository = new BookRepository();

        long sequentialStart = System.nanoTime();
        int sequentialCount = csvService.loadBooks(
                cataloguePath, inventoryPath, sequentialRepository);
        long sequentialTime = System.nanoTime() - sequentialStart;

        long concurrentStart = System.nanoTime();
        int concurrentCount = csvService.loadBooksConcurrently(
                cataloguePath, inventoryPath, concurrentRepository);
        long concurrentTime = System.nanoTime() - concurrentStart;

        assertEquals(sequentialCount, concurrentCount);
        assertIterableEquals(
                sequentialRepository.getAllBooks().stream().map(Book::getId).toList(),
                concurrentRepository.getAllBooks().stream().map(Book::getId).toList());

        System.out.printf("Sequential import: %d ms%n", sequentialTime / 1_000_000);
        System.out.printf("Concurrent import: %d ms%n", concurrentTime / 1_000_000);
        System.out.printf("Imported rows: %d%n", concurrentCount);
    }

    private Path findDataFile(String fileName) {
        Path moduleData = Path.of("data", fileName);
        if (Files.exists(moduleData)) {
            return moduleData;
        }
        return Path.of("..", "data", fileName);
    }

}
