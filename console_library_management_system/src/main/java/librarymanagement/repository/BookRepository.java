package librarymanagement.repository;

import java.util.*;

import librarymanagement.model.Book;

public class BookRepository {
    private Map<Integer, Book> books = new HashMap<>();

    public boolean addBook(Book b) {
        if (!importBook(b)) {
            System.out.println("Book already exists.");
            return false;
        }

        System.out.println("Book added successfully.");
        System.out.println(b);
        return true;
    }

    public boolean importBook(Book book) {
        if (books.containsKey(book.getId())) {
            return false;
        }
        books.put(book.getId(), book);
        return true;
    }

    public Book findById(int bookId) {
        if (books.containsKey(bookId)) {
            return books.get(bookId);
        }

        System.out.println("Book isn't present in the library.");
        return null;
    }

    public List<Book> searchByTitle(String q) {
        List<Book> bookList = new ArrayList<Book>();

        for (Book b : books.values()) {
            if (b.getTitle().contains(q)) {
                bookList.add(b);
            }
        }

        return bookList.stream().sorted(Comparator.comparingInt(Book::getId)).toList();
    }

    public List<Book> searchByAuthor(String q) {
        List<Book> bookList = new ArrayList<Book>();

        for (Book b : books.values()) {
            if (b.getAuthor().contains(q)) {
                bookList.add(b);
            }
        }

        return bookList.stream().sorted(Comparator.comparingInt(Book::getId)).toList();
    }

    public List<Book> searchByIsbn(String isbn) {
        List<Book> bookList = new ArrayList<Book>();

        for (Book b : books.values()) {
            if (b.getIsbn().equals(isbn)) {
                bookList.add(b);
            }
        }

        return bookList.stream().sorted(Comparator.comparingInt(Book::getId)).toList();
    }

    public List<Book> getAllBooks() {
        return books.values().stream()
                .sorted(Comparator.comparingInt(Book::getId))
                .toList();
    }

    public boolean containsId(int bookId) {
        return books.containsKey(bookId);
    }

    public boolean issueBook(int bookId) {
        findById(bookId).decrementAvailable();
        System.out.println("Book issued successfully.");
        return true;
    }

    public boolean returnBook(int bookId) {
        findById(bookId).incrementAvailable();
        System.out.println("Book returned successfully.");
        return true;
    }
}