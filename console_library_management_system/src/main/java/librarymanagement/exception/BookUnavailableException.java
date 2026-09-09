package librarymanagement.exception;

public class BookUnavailableException extends LibraryException {
    public BookUnavailableException(int bookId) {
        super("Book has no available copies: " + bookId);
    }
}
