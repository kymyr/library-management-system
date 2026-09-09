package librarymanagement.exception;

public class BookNotFoundException extends LibraryException {
    public BookNotFoundException(int bookId) {
        super("Book ID not found: " + bookId);
    }
}
