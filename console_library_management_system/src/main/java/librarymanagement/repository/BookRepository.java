package librarymanagement.repository;

import java.util.*;

import librarymanagement.model.Book;

public class BookRepository {
    private Map<Integer, Book> books = new HashMap<>();

    public boolean addBook(Book b) {
        if (books.containsKey(b.getId())) {
            System.out.println("Book already exists.");
            return false;
        }

        books.put(b.getId(), b);
        System.out.println("Book added successfully.");
        System.out.println(b);
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

        return bookList;
    }

    public List<Book> searchByAuthor(String q) {
        List<Book> bookList = new ArrayList<Book>();

        for (Book b : books.values()) {
            if (b.getAuthor().contains(q)) {
                bookList.add(b);
            }
        }

        return bookList;
    }

    public List<Book> searchByIsbn(String isbn) {
        List<Book> bookList = new ArrayList<Book>();

        for (Book b : books.values()) {
            if (b.getIsbn().equals(isbn)) {
                bookList.add(b);
            }
        }

        return bookList;
    }

    public List<Book> getAllBooks() {
        return new ArrayList<Book>(books.values());
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