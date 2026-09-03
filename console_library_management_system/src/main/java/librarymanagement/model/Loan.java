package librarymanagement.model;

public record Loan(
    int loanId,
    int bookId,
    int memberId,
    String checkoutDate,
    String dueDate,
    String checkinDate,
    LoanStatus status,
    int overdueDays,
    boolean overdue,
    double penaltyAmount) {
}