package librarymanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

    // Bulk-imports 5 csv files 
    // Kept self-contained here since only this test uses the test files
    @Test
    void testConcurrentImport() throws IOException {
        List<Path> filePaths = new ArrayList<>();
        for (int fileNumber = 1; fileNumber <= 5; fileNumber++) {
            filePaths.add(findTestDataFile("import-books-" + fileNumber + ".csv"));
        }
        Assumptions.assumeTrue(filePaths.stream().allMatch(Files::exists));

        BookRepository bookRepository = new BookRepository();

        long start = System.nanoTime();
        int imported = bulkImportBooks(filePaths, bookRepository);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertEquals(250_000, imported);
        assertEquals(250_000, bookRepository.getAllBooks().size());
        assertTrue(elapsedMs < 30_000, "Bulk import took too long: " + elapsedMs + " ms");

        System.out.printf("Bulk import of %d books took %d ms%n", imported, elapsedMs);
    }

    private Path findTestDataFile(String fileName) {
        Path repositoryTestData = Path.of("..", "test_data", fileName);
        if (Files.exists(repositoryTestData)) {
            return repositoryTestData;
        }
        return Path.of("test_data", fileName);
    }

    private int bulkImportBooks(List<Path> filePaths, BookRepository bookRepository) throws IOException {
        AtomicInteger nextId = new AtomicInteger(1);
        List<Callable<List<Book>>> tasks = new ArrayList<>();
        for (Path filePath : filePaths) {
            tasks.add(() -> parseBookFile(filePath, nextId));
        }

        int imported = 0;
        ExecutorService pool = Executors.newFixedThreadPool(filePaths.size());
        try {
            List<Future<List<Book>>> futures = pool.invokeAll(tasks);
            for (Future<List<Book>> future : futures) {
                try {
                    for (Book book : future.get()) {
                        if (bookRepository.importBook(book)) {
                            imported++;
                        }
                    }
                } catch (ExecutionException e) {
                    throw new IOException("Failed to parse a bulk-import file.", e.getCause());
                }
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
            throw new IOException("Bulk import was interrupted.", e);
        } finally {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                }
            } catch (InterruptedException e) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
                throw new IOException("Bulk import shutdown was interrupted.", e);
            }
        }
        return imported;
    }

    private List<Book> parseBookFile(Path filePath, AtomicInteger nextId) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        List<Book> books = new ArrayList<>();

        for (int lineNumber = 1; lineNumber < lines.size(); lineNumber++) {
            List<String> fields = parseCsvLine(lines.get(lineNumber));
            if (fields.size() != 4) {
                continue;
            }

            try {
                String isbn = fields.get(0).trim();
                String title = fields.get(1).trim();
                String author = fields.get(2).trim();
                int totalCopies = Integer.parseInt(fields.get(3).trim());
                books.add(new Book(nextId.getAndIncrement(), title, author, isbn, totalCopies));
            } catch (NumberFormatException ignored) {
                // Skip malformed rows while parsing the rest of the file.
            }
        }
        return books;
    }

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
}
