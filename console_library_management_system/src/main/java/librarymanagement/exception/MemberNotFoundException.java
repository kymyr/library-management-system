package librarymanagement.exception;

public class MemberNotFoundException extends LibraryException {
    public MemberNotFoundException(int memberId) {
        super("Member ID not found: " + memberId);
    }
}
