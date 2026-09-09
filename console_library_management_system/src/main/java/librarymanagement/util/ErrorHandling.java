package librarymanagement.util;

import java.util.*;

import librarymanagement.exception.InvalidInputException;

public class ErrorHandling {
    public static final int CANCEL = 0;
    private static final String CANCEL_INPUT = "0";

    public int validateMenuChoice(int val, String msg, Scanner sc) {
        while (true) {
            try {
                System.out.print(msg + " ");
                String input = sc.nextLine();
                val = Integer.parseInt(input);
                if (val < 0 || val > 9) {
                    System.out.println("Input choice. Please choose an option between 0 and 9.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
        return val;
    }

    public int validateID(int val, String msg, Scanner sc) {
        while (true) {
            try {
                System.out.print(msg + " ");
                String input = sc.nextLine();
                val = Integer.parseInt(input);
                if (val < 100 || val > 1000) {
                    System.out.println("Input is out of range. Please enter a ID between 100 and 1000.");
                    continue;
                }
                break;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                sc.nextLine();
            }
        }
        return val;
    }

    public int validateIntegerInput(int val, String msg, Scanner sc) {
        while (true) {
            try {
                System.out.print(msg + " ");
                String input = sc.nextLine();
                val = Integer.parseInt(input);
                if (val < 0 || val > 100) {
                    System.out.println("Input is out of range. Please enter a number between 0 and 100.");
                    continue;
                }
                break;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                sc.nextLine();
            }
        }
        return val;
    }

    public String validateStringInput(String val, String msg, Scanner sc) {
        while (true) {
            System.out.print(msg + " ");
            val = sc.nextLine().trim();

            if (val.isEmpty()) {
                System.out.println("Input cannot be empty. Please enter a valid input.");
                continue;
            }

            if (!val.matches("^[A-Za-z ]+$")) {
                System.out.println("Invalid input. Please use alphabets only (no numbers or special characters).");
                continue;
            }

            break;
        }
        return val;
    }

    public String validateIsbn(String msg, Scanner sc) {
        while (true) {
            System.out.print(msg + " ");
            String isbn = sc.nextLine().trim();

            if (isbn.matches("\\d+")) {
                return isbn;
            }

            System.out.println("Invalid ISBN. Please enter digits only.");
        }
    }

    public int validateLookupId(String msg, Scanner sc) {
        while (true) {
            System.out.print(msg + " ");
            String input = sc.nextLine().trim();
            try {
                int id = Integer.parseInt(input);
                if (id > 0) {
                    return id;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Invalid ID. Please enter a positive whole number.");
        }
    }

    public String validateEmail(String msg, Scanner sc) {
        while (true) {
            System.out.print(msg + " ");
            String email = sc.nextLine().trim();
            if (email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                return email;
            }
            System.out.println("Invalid email. Please enter a valid email address.");
        }
    }

    public int validateCancellableId(String msg, Scanner sc) {
        while (true) {
            System.out.print(msg + " ");
            String input = sc.nextLine().trim();
            try {
                return parseCancellableId(input);
            } catch (InvalidInputException e) {
                System.out.println(e.getMessage()
                        + ". Please enter a positive whole number, or 0 to go back.");
            }
        }
    }

    private int parseCancellableId(String input) {
        try {
            int id = Integer.parseInt(input);
            if (id >= 0) {
                return id;
            }
            throw new InvalidInputException("Invalid ID");
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid ID", e);
        }
    }

    public String validateCancellableString(String msg, Scanner sc) {
        while (true) {
            System.out.print(msg + " ");
            String val = sc.nextLine().trim();

            if (val.equals(CANCEL_INPUT)) {
                return null;
            }

            if (val.isEmpty()) {
                System.out.println("Input cannot be empty. Please enter a valid input.");
                continue;
            }

            if (!val.matches("^[A-Za-z ]+$")) {
                System.out.println("Invalid input. Please use alphabets only (no numbers or special characters).");
                continue;
            }

            return val;
        }
    }

    public String validateCancellableEmail(String msg, Scanner sc) {
        while (true) {
            System.out.print(msg + " ");
            String email = sc.nextLine().trim();

            if (email.equals(CANCEL_INPUT)) {
                return null;
            }

            if (email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                return email;
            }

            System.out.println("Invalid email. Please enter a valid email address, or 0 to go back.");
        }
    }

    public boolean validateConfirmation(String msg, Scanner sc) {
        while (true) {
            System.out.print(msg + " ");
            String input = sc.nextLine().trim().toLowerCase();
            if (input.equals("y")) {
                return true;
            }
            if (input.equals("n")) {
                return false;
            }
            System.out.println("Invalid choice. Please enter y or n.");
        }
    }
}