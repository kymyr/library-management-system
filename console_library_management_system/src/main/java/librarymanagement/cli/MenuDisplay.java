package librarymanagement.cli;

public final class MenuDisplay {
    static final String BORDER = "-----------------------------------------------------------";
    private static final int WIDTH = BORDER.length();

    private MenuDisplay() {
    }

    static String centerText(String text, int width) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= width) {
            return text;
        }
        int totalPadding = width - text.length();
        int leftPadding = totalPadding / 2;
        return " ".repeat(leftPadding) + text + " ".repeat(totalPadding - leftPadding);
    }

    static String buildMenu(String title, String... options) {
        StringBuilder sb = new StringBuilder(BORDER)
                .append("\n").append(centerText(title, WIDTH))
                .append("\n").append(BORDER)
                .append("\n").append(centerText("Select from the following options:", WIDTH));
        for (String option : options) {
            sb.append("\n").append(centerText(option, WIDTH));
        }
        return sb.append("\n").append(BORDER).append("\n").append("Enter choice:").toString();
    }

    /** Banner for an action screen, with a reminder that 0 backs out. */
    public static String header(String title) {
        return "\n" + BORDER
                + "\n" + centerText(title, WIDTH)
                + "\n" + BORDER
                + "\n" + centerText("Enter 0 at any prompt to go back", WIDTH)
                + "\n" + BORDER;
    }

    public static String mainMenu() {        return BORDER
                + "\n" + centerText("Console Library Management System", WIDTH)
                + "\n" + buildMenu("Main Menu",
                        "0 - Exit",
                        "1 - Books",
                        "2 - Members",
                        "3 - Loans");
    }

    public static String booksMenu() {
        return buildMenu("Books",
                "0 - Back to Main Menu",
                "1 - Show All Books",
                "2 - Search Book",
                "3 - Check Out Book",
                "4 - Check In Book");
    }

    public static String searchBookMenu() {
        return buildMenu("Search Books",
                "0 - Back to Books Menu",
                "1 - Search by Title",
                "2 - Search by Author",
                "3 - Search by ISBN");
    }

    public static String membersMenu() {
        return buildMenu("Members",
                "0 - Back to Main Menu",
                "1 - Show All Members",
                "2 - Register a Member");
    }

    public static String loansMenu() {
        return buildMenu("Loans",
                "0 - Back to Main Menu",
                "1 - Show Borrowed Books",
                "2 - Show Overdue Books",
                "3 - Search Loan History by Member ID",
                "4 - Search Loan History by Book ID");
    }
}
