package librarymanagement.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import librarymanagement.model.Book;
import org.junit.jupiter.api.Test;

class BookRepositoryTest {
    @Test
    void searchByIsbnReturnsTheMatchingBook() {
        BookRepository bookRepository = new BookRepository();
        Book book = new Book(102, "ISBN Test Book", "Test Author", "9780000000002", 1);
        bookRepository.addBook(book);

        assertEquals(1, bookRepository.searchByIsbn("9780000000002").size());
        assertEquals(book, bookRepository.searchByIsbn("9780000000002").get(0));
    }

    @Test
    void repositoryRejectsDuplicateBookIds() {
        BookRepository bookRepository = new BookRepository();
        Book firstBook = new Book(103, "First Book", "Author", "9780000000003", 1);
        Book duplicateBook = new Book(103, "Duplicate Book", "Author", "9780000000004", 1);

        assertTrue(bookRepository.addBook(firstBook));
        assertFalse(bookRepository.addBook(duplicateBook));
        assertEquals(1, bookRepository.getAllBooks().size());
    }

    @Test
    void repositoryFindsBooksAndReturnsNullForMissingIds() {
        BookRepository bookRepository = new BookRepository();
        Book book = new Book(104, "Findable Book", "Author", "9780000000005", 2);
        bookRepository.addBook(book);

        assertEquals(book, bookRepository.findById(104));
        assertNull(bookRepository.findById(999));
    }

    @Test
    void repositorySearchesByTitleAndAuthor() {
        BookRepository bookRepository = new BookRepository();
        Book book = new Book(105, "The Searchable Book", "Searchable Author", "9780000000006", 1);
        bookRepository.addBook(book);

        assertEquals(1, bookRepository.searchByTitle("Searchable").size());
        assertEquals(1, bookRepository.searchByAuthor("Searchable").size());
        assertTrue(bookRepository.searchByTitle("Missing").isEmpty());
    }

    @Test
    void issuingAndReturningBookChangesAvailability() {
        BookRepository bookRepository = new BookRepository();
        Book book = new Book(106, "Loanable Book", "Author", "9780000000007", 1);
        bookRepository.addBook(book);

        assertTrue(bookRepository.issueBook(106));
        assertEquals(0, book.getAvailable());
        assertTrue(bookRepository.returnBook(106));
        assertEquals(1, book.getAvailable());
    }
}
