package librarymanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import librarymanagement.model.Book;
import librarymanagement.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvServiceConcurrencyTest {
    @TempDir
    Path temporaryDirectory;

        // Sequential and concurrent imports produce equivalent book records.
    @Test
        void importsMatch() throws IOException {
        Path cataloguePath = temporaryDirectory.resolve("books_catalogue.csv");
        Path inventoryPath = temporaryDirectory.resolve("books_inventory.csv");
        writeTestData(cataloguePath, inventoryPath);

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

        List<Book> sequentialBooks = sequentialRepository.getAllBooks();
        List<Book> concurrentBooks = concurrentRepository.getAllBooks();

        assertEquals(sequentialCount, concurrentCount);
        assertEquals(sequentialBooks.size(), concurrentBooks.size());
        assertIterableEquals(
                sequentialBooks.stream().map(Book::getId).toList(),
                concurrentBooks.stream().map(Book::getId).toList());
        for (int index = 0; index < sequentialBooks.size(); index++) {
            Book expected = sequentialBooks.get(index);
            Book actual = concurrentBooks.get(index);
            assertEquals(expected.getTitle(), actual.getTitle());
            assertEquals(expected.getAuthor(), actual.getAuthor());
            assertEquals(expected.getIsbn(), actual.getIsbn());
            assertEquals(expected.getTotalQuantity(), actual.getTotalQuantity());
            assertEquals(expected.getAvailable(), actual.getAvailable());
        }

        System.out.printf("Sequential CSV import: %d ms%n", sequentialTime / 1_000_000);
        System.out.printf("Concurrent CSV import: %d ms%n", concurrentTime / 1_000_000);
        System.out.printf("Imported CSV rows: %d%n", concurrentCount);
    }

    private void writeTestData(Path cataloguePath, Path inventoryPath) throws IOException {
        Files.write(cataloguePath, List.of(
                "bookId,title,author,isbn",
                "101,First Book,First Author,9780000000001",
                "102,\"Book With A Comma, Volume 2\",Second Author,9780000000002",
                "103,Third Book,Third Author,9780000000003"));
        Files.write(inventoryPath, List.of(
                "bookId,total_copies,available_copies",
                "101,2,2",
                "102,3,1",
                "103,1,0"));
    }
}
