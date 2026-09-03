package librarymanagement.model;


public class Book {
    private int id;
    private String title;
    private String author;
    private String isbn;
    private int totalQuantity;
    private int available;

    public Book (int id, String title, String author, String isbn, int totalQuantity) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.totalQuantity = totalQuantity;
        this.available = totalQuantity;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return this.isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getTotalQuantity() {
        return this.totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public int getAvailable() {
        return this.available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    @java.lang.Override
    public java.lang.String toString() {
        String avail = isAvailable() ? "Available" : "Not Available";
        return "ID: " + getId() + " | Title: " + getTitle() + " | Author: " + getAuthor()
            + " | ISBN: " + getIsbn() + " | Availability: " + avail;
    }

    public boolean isAvailable() {
        if (getAvailable() > 0) return true;

//        System.out.println("Book is out of stock!");
        return false;
    }

    public void incrementAvailable() {
        setAvailable(getAvailable() + 1);
    }

    public void decrementAvailable() {
        setAvailable(getAvailable() - 1);
    }
}