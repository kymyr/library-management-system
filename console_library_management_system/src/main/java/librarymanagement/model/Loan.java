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

    public boolean isActive() {
        return status == LoanStatus.BORROWED;
    }

    public Loan withReturn(String checkinDate, int overdueDays, double penaltyAmount) {
        return new Loan(loanId, bookId, memberId, checkoutDate, dueDate,
                checkinDate, LoanStatus.RETURNED, overdueDays, overdueDays > 0, penaltyAmount);
    }
}