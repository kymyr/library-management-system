package librarymanagement.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BookTest {
    @Test
    void newBookMakesAllCopiesAvailable() {
        Book book = new Book(101, "Example Book", "Example Author", "9780000000001", 3);

        assertEquals(3, book.getAvailable());
        assertEquals("9780000000001", book.getIsbn());
    }

    @Test
    void bookAvailabilityChangesWhenCopiesAreIssuedAndReturned() {
        Book book = new Book(102, "Example Book", "Example Author", "9780000000002", 1);

        book.decrementAvailable();
        assertEquals(0, book.getAvailable());
        assertFalse(book.isAvailable());

        book.incrementAvailable();
        assertEquals(1, book.getAvailable());
        assertTrue(book.isAvailable());
    }

    @Test
    void bookSettersUpdateBookFields() {
        Book book = new Book(103, "Old Title", "Old Author", "9780000000003", 1);

        book.setId(104);
        book.setTitle("New Title");
        book.setAuthor("New Author");
        book.setIsbn("9780000000004");
        book.setTotalQuantity(4);
        book.setAvailable(2);

        assertEquals(104, book.getId());
        assertEquals("New Title", book.getTitle());
        assertEquals("New Author", book.getAuthor());
        assertEquals("9780000000004", book.getIsbn());
        assertEquals(4, book.getTotalQuantity());
        assertEquals(2, book.getAvailable());
    }
}
