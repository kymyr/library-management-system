package librarymanagement.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MenuDisplayTest {
    // Main menu contains the system title and main choices.
    @Test
    void showsMainMenu() {
        String menu = MenuDisplay.mainMenu();

        assertTrue(menu.contains("Console Library Management System"));
        assertTrue(menu.contains("1 - Books"));
        assertTrue(menu.contains("2 - Members"));
        assertTrue(menu.contains("3 - Loans"));
    }

    // Books menu uses clear borrow and return wording.
    @Test
    void showsBooksMenu() {
        String menu = MenuDisplay.booksMenu();

        assertTrue(menu.contains("Borrow Book (Check out)"));
        assertTrue(menu.contains("Return Book (Check in)"));
    }

    // Action headers explain how to cancel a prompt.
    @Test
    void showsActionHeader() {
        String header = MenuDisplay.header("Check Out Book");

        assertTrue(header.contains("Check Out Book"));
        assertTrue(header.contains("Enter 0 at any prompt to go back"));
        assertFalse(header.contains("null"));
    }
}
